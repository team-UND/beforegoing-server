package com.und.server.scenario.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.response.NotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.constants.MissionSearchType;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.MissionRequest;
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ReorderRequiredException;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.ScenarioRepository;
import com.und.server.scenario.util.MissionTypeGroupSorter;
import com.und.server.scenario.util.OrderCalculator;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioService {

	private final NotificationService notificationService;
	private final MissionService missionService;
	private final ScenarioRepository scenarioRepository;
	private final MissionTypeGroupSorter missionTypeGroupSorter;
	private final OrderCalculator orderCalculator;
	private final EntityManager em;


	@Transactional(readOnly = true)
	public List<ScenarioResponse> findScenariosByMemberId(Long memberId, NotifType notifType) {
		List<Scenario> scenarioList =
			scenarioRepository.findByMemberIdAndNotification_NotifTypeOrderByOrder(memberId, notifType);

		return ScenarioResponse.listOf(scenarioList);
	}


	@Transactional(readOnly = true)
	public ScenarioDetailResponse findScenarioDetailByScenarioId(Long memberId, Long scenarioId) {
		Scenario scenario = scenarioRepository.findByIdWithDefaultBasicMissions(
				memberId, scenarioId, MissionType.BASIC)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		List<Mission> groupdBasicMissionList =
			missionTypeGroupSorter.groupAndSortByType(scenario.getMissionList(), MissionType.BASIC);

		Notification notification = scenario.getNotification();
		NotificationInfoDto notifInfo = notificationService.findNotificationDetails(notification);

		return getScenarioDetailResponse(scenario, groupdBasicMissionList, notifInfo);
	}


	@Transactional
	public void addScenario(Long memberId, ScenarioDetailRequest scenarioInfo) {
		NotificationRequest notifInfo = scenarioInfo.getNotification();
		Member member = em.getReference(Member.class, memberId);

		Notification notification =
			notificationService.addNotification(notifInfo, scenarioInfo.getNotificationCondition());

		List<Integer> orderList =
			scenarioRepository.findOrdersByMemberIdAndNotification_NotifType(
				memberId, notifInfo.getNotificationType());

		if (orderList.size() >= 20) {
			throw new ServerException(ScenarioErrorResult.MAX_SCENARIO_COUNT_EXCEEDED);
		}

		int order = orderList.isEmpty()
			? OrderCalculator.START_ORDER
			: getValidScenarioOrder(Collections.max(orderList), memberId, notifInfo);

		Scenario scenario = Scenario.builder()
			.member(member)
			.scenarioName(scenarioInfo.getScenarioName())
			.memo(scenarioInfo.getMemo())
			.order(order)
			.notification(notification)
			.build();

		scenarioRepository.save(scenario);
		missionService.addBasicMission(scenario, scenarioInfo.getBasicMissionList());
	}

	private int getValidScenarioOrder(
		int maxScenarioOrder,
		Long memberId,
		NotificationRequest notifInfo
	) {
		try {
			return orderCalculator.getOrder(maxScenarioOrder, null);
		} catch (ReorderRequiredException e) {
			reorderScenarios(memberId, notifInfo.getNotificationType());
			int newMaxOrder =
				scenarioRepository.findMaxOrderByMemberIdAndNotifType(
						memberId, notifInfo.getNotificationType())
					.orElse(OrderCalculator.START_ORDER);

			return orderCalculator.getOrder(newMaxOrder, null);
		}
	}


	@Transactional
	public void addTodayMissionToScenario(
		Long memberId,
		Long scenarioId,
		TodayMissionRequest missionAddInfo,
		LocalDate date
	) {
		LocalDate today = LocalDate.now();
		MissionSearchType missionSearchType = MissionSearchType.getMissionSearchType(today, date);
		if (missionSearchType == MissionSearchType.PAST) {
			throw new ServerException(ScenarioErrorResult.INVALID_TODAY_MISSION_DATE);
		}

		Scenario scenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		List<Mission> missionList = scenario.getMissionList();
		if (missionList.size() >= 20) {
			throw new ServerException(ScenarioErrorResult.MAX_MISSION_COUNT_EXCEEDED);
		}

		missionService.addTodayMission(scenario, missionAddInfo, date);
	}


	@Transactional
	public void updateScenario(Long memberId, Long scenarioId, ScenarioDetailRequest scenarioInfo) {
		Scenario oldSCenario = scenarioRepository.findByIdWithDefaultBasicMissions(
				memberId, scenarioId, MissionType.BASIC)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		Notification newNotification = notificationService.updateNotification(
			oldSCenario.getNotification(),
			scenarioInfo.getNotification(),
			scenarioInfo.getNotificationCondition()
		);

		List<MissionRequest> newBasicMissionList = scenarioInfo.getBasicMissionList();
		missionService.updateBasicMission(oldSCenario, newBasicMissionList);

		oldSCenario.setScenarioName(scenarioInfo.getScenarioName());
		oldSCenario.setMemo(scenarioInfo.getMemo());
		oldSCenario.setNotification(newNotification);
	}


	@Transactional
	public void updateScenarioOrder(
		Long memberId,
		Long scenarioId,
		ScenarioOrderUpdateRequest scenarioOrderInfo
	) {
		Scenario scenario = scenarioRepository.findByIdAndMemberId(scenarioId, memberId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		try {
			int toUpdateOrder = orderCalculator.getOrder(
				scenarioOrderInfo.getPrevOrder(),
				scenarioOrderInfo.getNextOrder()
			);
			scenario.setOrder(toUpdateOrder);

		} catch (ReorderRequiredException e) {
			Notification notification = scenario.getNotification();
			reorderScenarios(memberId, notification.getNotifType());
		}
	}


	@Transactional
	public void deleteScenarioWithAllMissions(Long memberId, Long scenarioId) {
		Scenario scenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		notificationService.deleteNotification(scenario.getNotification());
		scenarioRepository.delete(scenario);
	}


	private ScenarioDetailResponse getScenarioDetailResponse(
		Scenario scenario,
		List<Mission> basicMissionList,
		NotificationInfoDto notifInfo
	) {
		Notification notification = scenario.getNotification();

		ScenarioDetailResponse result = ScenarioDetailResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.memo(scenario.getMemo())
			.basicMissionList(MissionResponse.listOf(basicMissionList))
			.build();
		NotificationResponse notificationResult = NotificationResponse.of(notification);

		if (notifInfo != null) {
			notificationResult.setIsEveryDay(notifInfo.isEveryDay());
			notificationResult.setDayOfWeekOrdinalList(
				notifInfo.dayOfWeekOrdinalList().isEmpty()
					? null
					: notifInfo.dayOfWeekOrdinalList()
			);
			result.setNotificationCondition(notifInfo.notificationCondition());
		}
		result.setNotification(notificationResult);

		return result;
	}

	private void reorderScenarios(Long memberId, NotifType notifType) {
		List<Scenario> scenarioList =
			scenarioRepository.findByMemberIdAndNotification_NotifTypeOrderByOrder(
				memberId, notifType
			);

		int order = OrderCalculator.START_ORDER;
		for (Scenario s : scenarioList) {
			s.setOrder(order);
			order += OrderCalculator.DEFAULT_ORDER;
		}
		scenarioRepository.saveAll(scenarioList);
	}

}
