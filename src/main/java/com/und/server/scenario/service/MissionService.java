package com.und.server.scenario.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.constants.MissionSearchType;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.MissionRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.util.MissionTypeGrouper;
import com.und.server.scenario.util.OrderCalculator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MissionService {

	private final MissionRepository missionRepository;
	private final MissionTypeGrouper missionTypeGrouper;


	@Transactional(readOnly = true)
	public MissionGroupResponse findMissionsByScenarioId(Long memberId, Long scenarioId, LocalDate date) {
		List<Mission> missionList = getMissionListByDate(memberId, scenarioId, date);

		if (missionList == null || missionList.isEmpty()) {
			return MissionGroupResponse.of(List.of(), List.of());
		}

		List<Mission> groupedBasicMissionList =
			missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC);
		List<Mission> groupedTodayMissionList =
			missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY);

		return MissionGroupResponse.of(groupedBasicMissionList, groupedTodayMissionList);
	}

	private List<Mission> getMissionListByDate(Long memberId, Long scenarioId, LocalDate date) {
		LocalDate today = LocalDate.now();
		MissionSearchType missionSearchType = MissionSearchType.getMissionSearchType(today, date);

		switch (missionSearchType) {
			case TODAY -> {
				return missionRepository.findDefaultMissions(memberId, scenarioId, date);
			}
			case PAST, FUTURE -> {
				return missionRepository.findMissionsByDate(memberId, scenarioId, date);
			}
		}
		throw new ServerException(ScenarioErrorResult.INVALID_MISSION_FOUND_DATE);
	}


	@Transactional
	public void addTodayMission(Scenario scenario, TodayMissionRequest missionAddInfo, LocalDate date) {
		Mission newMission = Mission.builder()
			.scenario(scenario)
			.content(missionAddInfo.content())
			.isChecked(false)
			.useDate(date)
			.missionType(MissionType.TODAY)
			.build();

		missionRepository.save(newMission);
	}


	@Transactional
	public void addBasicMission(Scenario scenario, List<MissionRequest> missionInfoList) {
		if (missionInfoList.isEmpty()) {
			return;
		}

		List<Mission> missionList = new ArrayList<>();

		int order = OrderCalculator.START_ORDER;
		for (MissionRequest missionInfo : missionInfoList) {
			missionList.add(missionInfo.toEntity(scenario, order));
			order += OrderCalculator.DEFAULT_ORDER;
		}

		missionRepository.saveAll(missionList);
	}


	@Transactional
	public void updateBasicMission(Scenario oldSCenario, List<MissionRequest> missionInfoList) {
		List<Mission> oldMissionList =
			missionTypeGrouper.groupAndSortByType(oldSCenario.getMissionList(), MissionType.BASIC);

		if (missionInfoList.isEmpty()) {
			missionRepository.deleteAll(oldMissionList);
			return;
		}

		Map<Long, Mission> existingMissions = oldMissionList.stream()
			.collect(Collectors.toMap(Mission::getId, mission -> mission));
		Set<Long> existingMissionIds = existingMissions.keySet();
		List<Long> requestedMissionIds = new ArrayList<>();

		List<Mission> toAddList = new ArrayList<>();
		List<Mission> toUpdateList = new ArrayList<>();

		int order = OrderCalculator.START_ORDER;
		for (MissionRequest missionInfo : missionInfoList) {
			Long missionId = missionInfo.getMissionId();

			if (missionId == null) {
				toAddList.add(missionInfo.toEntity(oldSCenario, order));
			} else {
				Mission existingMission = existingMissions.get(missionId);
				if (existingMission != null) {
					existingMission.setOrder(order);
					toUpdateList.add(existingMission);
					requestedMissionIds.add(missionId);
				}
			}
			order += OrderCalculator.DEFAULT_ORDER;
		}

		List<Long> toDeleteIdList = existingMissionIds.stream()
			.filter(id -> !requestedMissionIds.contains(id))
			.toList();

		missionRepository.deleteAllById(toDeleteIdList);
		missionRepository.saveAll(toAddList);
		missionRepository.saveAll(toUpdateList);
	}


	@Transactional
	public void updateMissionCheck(Long memberId, Long missionId, Boolean isChecked) {
		Mission mission = findMissionById(missionId);
		validateMissionAccessibleMember(mission, memberId);

		mission.setIsChecked(isChecked);
	}


	@Transactional
	public void deleteTodayMission(Long memberId, Long missionId) {
		Mission mission = findMissionById(missionId);
		validateMissionAccessibleMember(mission, memberId);

		missionRepository.delete(mission);
	}


	private Mission findMissionById(Long missionId) {
		return missionRepository.findById(missionId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_MISSION));
	}

	private void validateMissionAccessibleMember(Mission mission, Long memberId) {
		Member member = mission.getScenario().getMember();
		if (!memberId.equals(member.getId())) {
			throw new ServerException(ScenarioErrorResult.UNAUTHORIZED_ACCESS);
		}
	}

}
