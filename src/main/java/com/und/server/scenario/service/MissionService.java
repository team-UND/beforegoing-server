package com.und.server.scenario.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.constants.MissionSearchType;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.MissionUpdatePlanDto;
import com.und.server.scenario.dto.request.BasicMissionRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.repository.ScenarioRepository;
import com.und.server.scenario.util.MissionTypeGroupSorter;
import com.und.server.scenario.util.MissionValidator;
import com.und.server.scenario.util.OrderCalculator;
import com.und.server.scenario.util.ScenarioValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MissionService {

	private static final String ZONE_ID = "Asia/Seoul";
	private final MissionCacheService missionCacheService;
	private final MissionRepository missionRepository;
	private final ScenarioRepository scenarioRepository;
	private final MissionTypeGroupSorter missionTypeGroupSorter;
	private final OrderCalculator orderCalculator;
	private final ScenarioValidator scenarioValidator;
	private final MissionValidator missionValidator;
	private final Clock clock;


	@Transactional(readOnly = true)
	@Cacheable(
		value = "missions", key = "#memberId + ':' + #scenarioId + ':' + #date",
		cacheManager = "scenarioCacheManager"
	)
	public MissionGroupResponse findMissionsByScenarioId(
		final Long memberId, final Long scenarioId, final LocalDate date
	) {
		scenarioValidator.validateScenarioExists(scenarioId);

		LocalDate today = LocalDate.now(clock.withZone(ZoneId.of(ZONE_ID)));
		MissionSearchType missionSearchType = MissionSearchType.getMissionSearchType(today, date);

		List<Mission> missions = getMissionsByDate(missionSearchType, memberId, scenarioId, date);

		if (missions == null || missions.isEmpty()) {
			return MissionGroupResponse.from(scenarioId, List.of(), List.of());
		}

		List<Mission> groupedBasicMissions =
			missionTypeGroupSorter.groupAndSortByType(missions, MissionType.BASIC);
		List<Mission> groupedTodayMissions =
			missionTypeGroupSorter.groupAndSortByType(missions, MissionType.TODAY);

		if (missionSearchType == MissionSearchType.FUTURE) {
			return MissionGroupResponse.futureFrom(
				scenarioId, getFutureCheckStatusMissions(groupedBasicMissions), groupedTodayMissions);
		}
		return MissionGroupResponse.from(scenarioId, groupedBasicMissions, groupedTodayMissions);
	}


	@Transactional
	public List<Mission> addBasicMission(final Scenario scenario, final List<BasicMissionRequest> missionRequests) {
		if (missionRequests.isEmpty()) {
			return List.of();
		}

		List<Mission> missions = new ArrayList<>();
		List<Integer> orders = orderCalculator.generateMissionOrders(missionRequests.size());

		for (int i = 0; i < missionRequests.size(); i++) {
			missions.add(missionRequests.get(i).toEntity(scenario, orders.get(i)));
		}
		missionValidator.validateMaxBasicMissionCount(missions);

		return missionRepository.saveAll(missions);
	}


	@Transactional
	public MissionResponse addTodayMission(
		final Long memberId,
		final Long scenarioId,
		final TodayMissionRequest todayMissionRequest,
		final LocalDate date
	) {
		Scenario scenario = scenarioRepository.findTodayScenarioFetchByIdAndMemberId(memberId, scenarioId, date)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		LocalDate today = LocalDate.now(clock.withZone(ZoneId.of(ZONE_ID)));
		missionValidator.validateTodayMissionDateRange(today, date);

		List<Mission> todayMissions = missionTypeGroupSorter.groupAndSortByType(
			scenario.getMissions(), MissionType.TODAY);
		missionValidator.validateMaxTodayMissionCount(todayMissions);

		Mission newMission = todayMissionRequest.toEntity(scenario, date);
		missionRepository.save(newMission);

		missionCacheService.evictUserMissionCache(memberId, scenarioId, date);
		return MissionResponse.from(newMission);
	}


	@Transactional
	public void updateBasicMission(final Scenario scenario, final List<BasicMissionRequest> missionRequests) {
		List<Mission> existingMissions =
			missionTypeGroupSorter.groupAndSortByType(scenario.getMissions(), MissionType.BASIC);

		if (missionRequests.isEmpty()) {
			scenario.getMissions().removeIf(mission -> mission.getMissionType() == MissionType.BASIC);
			return;
		}

		MissionUpdatePlanDto updatePlan = createMissionUpdatePlan(scenario, existingMissions, missionRequests);
		missionValidator.validateMaxBasicMissionCount(updatePlan.missionsToSave());

		List<Mission> missionsToSave = updatePlan.missionsToSave();
		List<Long> missionsToDelete = updatePlan.missionsToDelete();

		scenario.getMissions().removeIf(mission ->
			mission.getMissionType() == MissionType.BASIC
				&& missionsToDelete.contains(mission.getId())
		);

		if (!missionsToDelete.isEmpty()) {
			missionRepository.deleteByParentMissionIdIn(missionsToDelete);
		}
		missionRepository.saveAll(missionsToSave);
	}


	@Transactional
	public void updateMissionCheck(
		final Long memberId,
		final Long missionId,
		final Boolean isChecked,
		final LocalDate date
	) {
		Mission mission = missionRepository.findByIdAndScenarioMemberId(missionId, memberId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_MISSION));

		MissionSearchType missionSearchType = MissionSearchType.getMissionSearchType(
			LocalDate.now(clock.withZone(ZoneId.of(ZONE_ID))), date);

		if (mission.getMissionType() == MissionType.BASIC && missionSearchType == MissionSearchType.FUTURE) {
			updateFutureBasicMission(mission, isChecked, date);
		} else {
			mission.updateCheckStatus(isChecked);
		}

		missionCacheService.evictUserMissionCache(memberId, mission.getScenario().getId(), date);
	}


	@Transactional
	public void deleteTodayMission(final Long memberId, final Long missionId) {
		Mission mission = missionRepository.findByIdAndScenarioMemberId(missionId, memberId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_MISSION));

		missionRepository.delete(mission);

		//오늘만 미션 삭제가 보장된다면 +date로 해도 ㄱㅊ을듯
		missionCacheService.evictUserMissionCache(memberId, mission.getScenario().getId());
	}


	private List<Mission> getMissionsByDate(
		final MissionSearchType missionSearchType,
		final Long memberId,
		final Long scenarioId,
		final LocalDate date
	) {
		switch (missionSearchType) {
			case TODAY, FUTURE -> {
				return missionRepository.findTodayAndFutureMissions(memberId, scenarioId, date);
			}
			case PAST -> {
				return missionRepository.findPastMissionsByDate(memberId, scenarioId, date);
			}
		}
		throw new ServerException(ScenarioErrorResult.INVALID_MISSION_FOUND_DATE);
	}

	private List<MissionResponse> getFutureCheckStatusMissions(List<Mission> groupedBasicMissions) {
		Map<Long, Mission> overlayMap = groupedBasicMissions.stream()
			.filter(m -> m.getParentMissionId() != null)
			.collect(Collectors.toMap(Mission::getParentMissionId, m -> m));

		return groupedBasicMissions.stream()
			.filter(m -> m.getParentMissionId() == null && m.getUseDate() == null)
			.map(tpl -> {
				Mission overlay = overlayMap.get(tpl.getId());
				boolean checked = overlay != null && Boolean.TRUE.equals(overlay.getIsChecked());
				return MissionResponse.fromWithOverride(tpl, checked);
			})
			.toList();
	}

	private void updateFutureBasicMission(
		final Mission mission,
		final Boolean isChecked,
		final LocalDate date
	) {
		missionRepository.findByParentMissionIdAndUseDate(mission.getId(), date)
			.ifPresentOrElse(
				future -> {
					if (isChecked) {
						future.updateCheckStatus(true);
					} else {
						missionRepository.delete(future);
					}
				},
				() -> {
					if (isChecked) {
						missionRepository.save(mission.createFutureChildMission(true, date));
					}
				}
			);
	}

	private MissionUpdatePlanDto createMissionUpdatePlan(
		final Scenario scenario,
		final List<Mission> existingMissions,
		final List<BasicMissionRequest> missionRequests
	) {
		Map<Long, Mission> existingMissionMap = existingMissions.stream()
			.collect(Collectors.toMap(Mission::getId, mission -> mission));

		List<Integer> newOrders = orderCalculator.generateMissionOrders(missionRequests.size());
		List<Mission> missionsToSave = new ArrayList<>();
		Set<Long> requestedMissionIds = new HashSet<>();

		for (int i = 0; i < missionRequests.size(); i++) {
			BasicMissionRequest request = missionRequests.get(i);
			Integer newOrder = newOrders.get(i);
			Long missionId = request.missionId();

			if (missionId == null) {
				missionsToSave.add(request.toEntity(scenario, newOrder));
			} else {
				Mission existingMission = existingMissionMap.get(missionId);
				if (existingMission != null) {
					existingMission.updateMissionOrder(newOrder);
					missionsToSave.add(existingMission);
					requestedMissionIds.add(missionId);
				}
			}
		}

		List<Long> missionsToDelete = existingMissionMap.keySet().stream()
			.filter(id -> !requestedMissionIds.contains(id))
			.toList();

		return MissionUpdatePlanDto.builder()
			.missionsToSave(missionsToSave)
			.missionsToDelete(missionsToDelete)
			.build();
	}

}
