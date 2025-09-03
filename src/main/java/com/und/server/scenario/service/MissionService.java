package com.und.server.scenario.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.constants.MissionSearchType;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.BasicMissionRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.util.MissionTypeGroupSorter;
import com.und.server.scenario.util.MissionValidator;
import com.und.server.scenario.util.OrderCalculator;
import com.und.server.scenario.util.ScenarioValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MissionService {

	private final MissionRepository missionRepository;
	private final MissionTypeGroupSorter missionTypeGroupSorter;
	private final ScenarioValidator scenarioValidator;
	private final MissionValidator missionValidator;
	private final Clock clock;


	@Transactional(readOnly = true)
	public MissionGroupResponse findMissionsByScenarioId(
		final Long memberId, final Long scenarioId, final LocalDate date
	) {
		scenarioValidator.validateScenarioExists(scenarioId);

		LocalDate today = LocalDate.now();
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
			groupedBasicMissions = getFutureCheckStatusMissions(groupedBasicMissions);
		}

		return MissionGroupResponse.from(scenarioId, groupedBasicMissions, groupedTodayMissions);
	}


	@Transactional
	public List<Mission> addBasicMission(final Scenario scenario, final List<BasicMissionRequest> missionRequests) {
		if (missionRequests.isEmpty()) {
			return List.of();
		}

		List<Mission> missions = new ArrayList<>();

		int order = OrderCalculator.START_ORDER;
		for (BasicMissionRequest missionInfo : missionRequests) {
			missions.add(missionInfo.toEntity(scenario, order));
			order += OrderCalculator.DEFAULT_ORDER;
		}
		missionValidator.validateMaxBasicMissionCount(missions);

		return missionRepository.saveAll(missions);
	}


	@Transactional
	public MissionResponse addTodayMission(
		final Scenario scenario,
		final TodayMissionRequest todayMissionRequest,
		final LocalDate date
	) {
		LocalDate today = LocalDate.now(clock.withZone(ZoneId.of("Asia/Seoul")));
		missionValidator.validateTodayMissionDateRange(today, date);

		List<Mission> todayMissions = missionTypeGroupSorter.groupAndSortByType(
			scenario.getMissions(), MissionType.TODAY);
		missionValidator.validateMaxTodayMissionCount(todayMissions);

		Mission newMission = todayMissionRequest.toEntity(scenario, date);
		missionRepository.save(newMission);

		return MissionResponse.from(newMission);
	}


	@Transactional
	public void updateBasicMission(final Scenario oldSCenario, final List<BasicMissionRequest> missionRequests) {
		List<Mission> oldMissions =
			missionTypeGroupSorter.groupAndSortByType(oldSCenario.getMissions(), MissionType.BASIC);

		if (missionRequests.isEmpty()) {
			oldSCenario.getMissions().removeIf(mission ->
				mission.getMissionType() == MissionType.BASIC
			);
			return;
		}

		Map<Long, Mission> existingMissions = oldMissions.stream()
			.collect(Collectors.toMap(Mission::getId, mission -> mission));
		Set<Long> existingMissionIds = existingMissions.keySet();
		List<Long> requestedMissionIds = new ArrayList<>();

		List<Mission> toAdd = new ArrayList<>();

		int order = OrderCalculator.START_ORDER;
		for (BasicMissionRequest missionInfo : missionRequests) {
			Long missionId = missionInfo.missionId();

			if (missionId == null) {
				toAdd.add(missionInfo.toEntity(oldSCenario, order));
			} else {
				Mission existingMission = existingMissions.get(missionId);
				if (existingMission != null) {
					existingMission.updateMissionOrder(order);
					toAdd.add(existingMission);
					requestedMissionIds.add(missionId);
				}
			}
			order += OrderCalculator.DEFAULT_ORDER;
		}
		missionValidator.validateMaxBasicMissionCount(toAdd);

		List<Long> toDeleteId = existingMissionIds.stream()
			.filter(id -> !requestedMissionIds.contains(id))
			.toList();

		oldSCenario.getMissions().removeIf(mission ->
			mission.getMissionType() == MissionType.BASIC
				&& toDeleteId.contains(mission.getId())
		);

		missionRepository.deleteByParentMissionIdIn(toDeleteId);
		missionRepository.saveAll(toAdd);
	}


	@Transactional
	public void updateMissionCheck(
		final Long memberId,
		final Long missionId,
		final Boolean isChecked,
		final LocalDate date
	) {
		Mission mission = missionRepository.findById(missionId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_MISSION));
		missionValidator.validateMissionAccessibleMember(mission, memberId);

		MissionSearchType missionSearchType = MissionSearchType.getMissionSearchType(LocalDate.now(), date);

		if (mission.getMissionType() == MissionType.BASIC && missionSearchType == MissionSearchType.FUTURE) {
			updateFutureBasicMission(mission, missionId, isChecked, date);
			return;
		}

		mission.updateCheckStatus(isChecked);
	}


	@Transactional
	public void deleteMissions(final Long scenarioId) {
		missionRepository.deleteByScenarioId(scenarioId);
	}


	@Transactional
	public void deleteTodayMission(final Long memberId, final Long missionId) {
		Mission mission = missionRepository.findById(missionId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_MISSION));
		missionValidator.validateMissionAccessibleMember(mission, memberId);

		missionRepository.delete(mission);
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

	private List<Mission> getFutureCheckStatusMissions(List<Mission> groupedBasicMissions) {
		Map<Long, Mission> overlayMap = groupedBasicMissions.stream()
			.filter(m -> m.getParentMissionId() != null)
			.collect(Collectors.toMap(Mission::getParentMissionId, m -> m));

		groupedBasicMissions = groupedBasicMissions.stream()
			.filter(m -> m.getParentMissionId() == null && m.getUseDate() == null)
			.peek(tpl -> {
				Mission overlay = overlayMap.get(tpl.getId());
				tpl.updateCheckStatus(overlay != null && Boolean.TRUE.equals(overlay.getIsChecked()));
			})
			.toList();

		return groupedBasicMissions;
	}

	private void updateFutureBasicMission(
		final Mission mission,
		final Long missionId,
		final Boolean isChecked,
		final LocalDate date
	) {
		Optional<Mission> futureMission = missionRepository.findByParentMissionIdAndUseDate(missionId, date);
		if (futureMission.isPresent()) {
			if (!isChecked) {
				missionRepository.delete(futureMission.get());
			} else {
				futureMission.get().updateCheckStatus(isChecked);
			}
			return;
		}
		missionRepository.save(mission.createFutureChildMission(isChecked, date));
	}

}
