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
import com.und.server.scenario.constants.MissionSearchType;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.BasicMissionRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
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


	@Transactional(readOnly = true)
	public MissionGroupResponse findMissionsByScenarioId(Long memberId, Long scenarioId, LocalDate date) {
		scenarioValidator.validateScenarioExists(scenarioId);
		if (date == null) {
			date = LocalDate.now();
		}

		List<Mission> missionList = getMissionListByDate(memberId, scenarioId, date);

		if (missionList == null || missionList.isEmpty()) {
			return MissionGroupResponse.from(List.of(), List.of());
		}

		List<Mission> groupedBasicMissionList =
			missionTypeGroupSorter.groupAndSortByType(missionList, MissionType.BASIC);
		List<Mission> groupedTodayMissionList =
			missionTypeGroupSorter.groupAndSortByType(missionList, MissionType.TODAY);

		return MissionGroupResponse.from(groupedBasicMissionList, groupedTodayMissionList);
	}


	@Transactional
	public void addBasicMission(Scenario scenario, List<BasicMissionRequest> missionRequestList) {
		if (missionRequestList.isEmpty()) {
			return;
		}

		List<Mission> missionList = new ArrayList<>();

		int order = OrderCalculator.START_ORDER;
		for (BasicMissionRequest missionInfo : missionRequestList) {
			missionList.add(missionInfo.toEntity(scenario, order));
			order += OrderCalculator.DEFAULT_ORDER;
		}
		missionValidator.validateMaxBasicMissionCount(missionList);

		missionRepository.saveAll(missionList);
	}


	@Transactional
	public void addTodayMission(
		Scenario scenario,
		TodayMissionRequest todayMissionRequest,
		LocalDate date
	) {
		LocalDate today = LocalDate.now();
		missionValidator.validateTodayMissionDateRange(today, date);

		List<Mission> todayMissionList = missionTypeGroupSorter.groupAndSortByType(
			scenario.getMissionList(), MissionType.TODAY);
		missionValidator.validateMaxTodayMissionCount(todayMissionList);

		Mission newMission = todayMissionRequest.toEntity(scenario, date);

		missionRepository.save(newMission);
	}


	@Transactional
	public void updateBasicMission(Scenario oldSCenario, List<BasicMissionRequest> missionRequestList) {
		List<Mission> oldMissionList =
			missionTypeGroupSorter.groupAndSortByType(oldSCenario.getMissionList(), MissionType.BASIC);

		if (missionRequestList.isEmpty()) {
			oldSCenario.getMissionList().removeIf(mission ->
				mission.getMissionType() == MissionType.BASIC
			);
			return;
		}

		Map<Long, Mission> existingMissions = oldMissionList.stream()
			.collect(Collectors.toMap(Mission::getId, mission -> mission));
		Set<Long> existingMissionIds = existingMissions.keySet();
		List<Long> requestedMissionIds = new ArrayList<>();

		List<Mission> toAddList = new ArrayList<>();

		int order = OrderCalculator.START_ORDER;
		for (BasicMissionRequest missionInfo : missionRequestList) {
			Long missionId = missionInfo.missionId();

			if (missionId == null) {
				toAddList.add(missionInfo.toEntity(oldSCenario, order));
			} else {
				Mission existingMission = existingMissions.get(missionId);
				if (existingMission != null) {
					existingMission.updateMissionOrder(order);
					toAddList.add(existingMission);
					requestedMissionIds.add(missionId);
				}
			}
			order += OrderCalculator.DEFAULT_ORDER;
		}
		missionValidator.validateMaxBasicMissionCount(toAddList);

		List<Long> toDeleteIdList = existingMissionIds.stream()
			.filter(id -> !requestedMissionIds.contains(id))
			.toList();

		oldSCenario.getMissionList().removeIf(mission ->
			mission.getMissionType() == MissionType.BASIC
				&& toDeleteIdList.contains(mission.getId())
		);
		missionRepository.saveAll(toAddList);
	}


	@Transactional
	public void updateMissionCheck(Long memberId, Long missionId, Boolean isChecked) {
		Mission mission = missionRepository.findById(missionId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_MISSION));
		missionValidator.validateMissionAccessibleMember(mission, memberId);

		mission.updateCheckStatus(isChecked);
	}


	@Transactional
	public void deleteTodayMission(Long memberId, Long missionId) {
		Mission mission = missionRepository.findById(missionId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_MISSION));
		missionValidator.validateMissionAccessibleMember(mission, memberId);

		missionRepository.delete(mission);
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

}
