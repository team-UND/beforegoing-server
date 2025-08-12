package com.und.server.scenario.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.response.NotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.constants.MissionSearchType;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.BasicMissionRequest;
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.HomeScenarioResponse;
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

	private static final int SCENARIO_MAX_COUNT = 20;
	private final NotificationService notificationService;
	private final MissionService missionService;
	private final ScenarioRepository scenarioRepository;
	private final MissionTypeGroupSorter missionTypeGroupSorter;
	private final OrderCalculator orderCalculator;
	private final EntityManager em;


	@Transactional(readOnly = true)
	public List<HomeScenarioResponse> findHomeScenariosByMemberId(Long memberId, NotificationType notificationType) {
		List<Scenario> scenarioList =
			scenarioRepository.findByMemberIdAndNotificationType(memberId, notificationType);

		return HomeScenarioResponse.listFrom(scenarioList);
	}


	@Transactional(readOnly = true)
	public List<ScenarioResponse> findScenariosByMemberId(Long memberId, NotificationType notificationType) {
		List<Scenario> scenarioList =
			scenarioRepository.findByMemberIdAndNotificationType(memberId, notificationType);

		return ScenarioResponse.listFrom(scenarioList);
	}


	@Transactional(readOnly = true)
	public ScenarioDetailResponse findScenarioDetailByScenarioId(Long memberId, Long scenarioId) {
		Scenario scenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		List<Mission> groupdBasicMissionList =
			missionTypeGroupSorter.groupAndSortByType(scenario.getMissionList(), MissionType.BASIC);

		Notification notification = scenario.getNotification();
		NotificationInfoDto notificationInfo = notificationService.findNotificationDetails(notification);

		return getScenarioDetailResponse(scenario, groupdBasicMissionList, notificationInfo);
	}


	@Transactional
	public void addScenario(Long memberId, ScenarioDetailRequest scenarioDetailRequest) {
		NotificationRequest notificationInfo = scenarioDetailRequest.getNotification();
		Member member = em.getReference(Member.class, memberId);

		Notification notification =
			notificationService.addNotification(
				notificationInfo, scenarioDetailRequest.getNotificationCondition());

		List<Integer> orderList =
			scenarioRepository.findOrdersByMemberIdAndNotificationType(
				memberId, notificationInfo.getNotificationType());

		validateMaxScenarioCount(orderList);

		int order = orderList.isEmpty()
			? OrderCalculator.START_ORDER
			: getValidScenarioOrder(Collections.max(orderList), memberId, notificationInfo);

		Scenario scenario = Scenario.builder()
			.member(member)
			.scenarioName(scenarioDetailRequest.getScenarioName())
			.memo(scenarioDetailRequest.getMemo())
			.scenarioOrder(order)
			.notification(notification)
			.build();

		scenarioRepository.save(scenario);
		missionService.addBasicMission(scenario, scenarioDetailRequest.getBasicMissionList());
	}

	private int getValidScenarioOrder(
		int maxScenarioOrder,
		Long memberId,
		NotificationRequest notificationRequest
	) {
		try {
			return orderCalculator.getOrder(maxScenarioOrder, null);
		} catch (ReorderRequiredException e) {
			reorderScenarios(memberId, notificationRequest.getNotificationType());
			int newMaxOrder =
				scenarioRepository.findMaxOrderByMemberIdAndNotificationType(
						memberId, notificationRequest.getNotificationType())
					.orElse(OrderCalculator.START_ORDER);

			return orderCalculator.getOrder(newMaxOrder, null);
		}
	}


	@Transactional
	public void addTodayMissionToScenario(
		Long memberId,
		Long scenarioId,
		TodayMissionRequest todayMissionRequest,
		LocalDate date
	) {
		LocalDate today = LocalDate.now();
		MissionSearchType missionSearchType = MissionSearchType.getMissionSearchType(today, date);
		if (missionSearchType == MissionSearchType.PAST) {
			throw new ServerException(ScenarioErrorResult.INVALID_TODAY_MISSION_DATE);
		}

		Scenario scenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		missionService.addTodayMission(scenario, todayMissionRequest, date);
	}


	@Transactional
	public void updateScenario(
		Long memberId,
		Long scenarioId,
		ScenarioDetailRequest scenarioDetailRequest
	) {
		Scenario oldSCenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		Notification newNotification = notificationService.updateNotification(
			oldSCenario.getNotification(),
			scenarioDetailRequest.getNotification(),
			scenarioDetailRequest.getNotificationCondition()
		);

		List<BasicMissionRequest> newBasicMissionList = scenarioDetailRequest.getBasicMissionList();
		missionService.updateBasicMission(oldSCenario, newBasicMissionList);

		oldSCenario.updateScenarioName(scenarioDetailRequest.getScenarioName());
		oldSCenario.updateMemo(scenarioDetailRequest.getMemo());
		oldSCenario.updateNotification(newNotification);
	}


	@Transactional
	public void updateScenarioOrder(
		Long memberId,
		Long scenarioId,
		ScenarioOrderUpdateRequest scenarioOrderUpdateRequest
	) {
		Scenario scenario = scenarioRepository.findByIdAndMemberId(scenarioId, memberId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		try {
			int toUpdateOrder = orderCalculator.getOrder(
				scenarioOrderUpdateRequest.getPrevOrder(),
				scenarioOrderUpdateRequest.getNextOrder()
			);
			scenario.updateScenarioOrder(toUpdateOrder);

		} catch (ReorderRequiredException e) {
			Notification notification = scenario.getNotification();
			reorderScenarios(memberId, notification.getNotificationType());
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
		NotificationInfoDto notificationInfo
	) {
		Notification notification = scenario.getNotification();

		ScenarioDetailResponse scenarioDetailResponse = ScenarioDetailResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.memo(scenario.getMemo())
			.basicMissionList(MissionResponse.listFrom(basicMissionList))
			.build();

		NotificationResponse notificationResponse;
		if (notificationInfo == null) {
			notificationResponse = NotificationResponse.from(
				notification, null, null);
		} else {
			notificationResponse = NotificationResponse.from(
				notification, notificationInfo.isEveryDay(), notificationInfo.dayOfWeekOrdinalList());
			scenarioDetailResponse.setNotificationCondition(notificationInfo.notificationConditionResponse());
		}

//		if (notificationInfo != null) {
//			notificationResponse.setIsEveryDay(notificationInfo.isEveryDay());
//			notificationResponse.setDayOfWeekOrdinalList(
//				notificationInfo.dayOfWeekOrdinalList().isEmpty()
//					? null
//					: notificationInfo.dayOfWeekOrdinalList()
//			);
//			scenarioDetailResponse.setNotificationCondition(notificationInfo.notificationConditionResponse());
//		}
		scenarioDetailResponse.setNotification(notificationResponse);

		return scenarioDetailResponse;
	}

	private void reorderScenarios(Long memberId, NotificationType notificationType) {
		List<Scenario> scenarioList =
			scenarioRepository.findByMemberIdAndNotificationType(memberId, notificationType);

		int order = OrderCalculator.START_ORDER;
		for (Scenario scenario : scenarioList) {
			scenario.updateScenarioOrder(order);
			order += OrderCalculator.DEFAULT_ORDER;
		}
		scenarioRepository.saveAll(scenarioList);
	}

	private void validateMaxScenarioCount(List<Integer> orderList) {
		if (orderList.size() >= SCENARIO_MAX_COUNT) {
			throw new ServerException(ScenarioErrorResult.MAX_SCENARIO_COUNT_EXCEEDED);
		}
	}

}
