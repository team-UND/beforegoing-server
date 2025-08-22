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
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.NotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.dto.response.OrderUpdateResponse;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ReorderRequiredException;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.ScenarioRepository;
import com.und.server.scenario.util.MissionTypeGroupSorter;
import com.und.server.scenario.util.OrderCalculator;
import com.und.server.scenario.util.ScenarioValidator;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScenarioService {

	private final NotificationService notificationService;
	private final MissionService missionService;
	private final ScenarioRepository scenarioRepository;
	private final MissionTypeGroupSorter missionTypeGroupSorter;
	private final OrderCalculator orderCalculator;
	private final ScenarioValidator scenarioValidator;
	private final EntityManager em;


	@Transactional(readOnly = true)
	public List<ScenarioResponse> findScenariosByMemberId(
		final Long memberId, final NotificationType notificationType
	) {
		List<Scenario> scenarios =
			scenarioRepository.findByMemberIdAndNotificationType(memberId, notificationType);

		return ScenarioResponse.listFrom(scenarios);
	}


	@Transactional(readOnly = true)
	public ScenarioDetailResponse findScenarioDetailByScenarioId(final Long memberId, final Long scenarioId) {
		Scenario scenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		List<Mission> basicMissions =
			missionTypeGroupSorter.groupAndSortByType(scenario.getMissions(), MissionType.BASIC);

		Notification notification = scenario.getNotification();
		NotificationInfoDto notificationInfo = notificationService.findNotificationDetails(notification);

		return getScenarioDetailResponse(scenario, basicMissions, notificationInfo);
	}


	@Transactional
	public MissionResponse addTodayMissionToScenario(
		final Long memberId,
		final Long scenarioId,
		final TodayMissionRequest todayMissionRequest,
		final LocalDate date
	) {
		Scenario scenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		return missionService.addTodayMission(scenario, todayMissionRequest, date);
	}


	@Transactional
	public Long addScenario(final Long memberId, final ScenarioDetailRequest scenarioDetailRequest) {
		Member member = em.getReference(Member.class, memberId);

		NotificationRequest notificationRequest = scenarioDetailRequest.notification();
		NotificationType notificationType = notificationRequest.notificationType();

		List<Integer> orders =
			scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, notificationType);
		scenarioValidator.validateMaxScenarioCount(orders);

		int order = orders.isEmpty()
			? OrderCalculator.START_ORDER
			: getValidScenarioOrder(Collections.max(orders), memberId, notificationType);

		Notification notification = notificationService.addNotification(
			notificationRequest, scenarioDetailRequest.notificationCondition());

		Scenario scenario = Scenario.builder()
			.member(member)
			.scenarioName(scenarioDetailRequest.scenarioName())
			.memo(scenarioDetailRequest.memo())
			.scenarioOrder(order)
			.notification(notification)
			.build();

		scenarioRepository.save(scenario);
		missionService.addBasicMission(scenario, scenarioDetailRequest.basicMissions());

		return scenario.getId();
	}


	@Transactional
	public void updateScenario(
		final Long memberId,
		final Long scenarioId,
		final ScenarioDetailRequest scenarioDetailRequest
	) {
		Scenario oldScenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		notificationService.updateNotification(
			oldScenario.getNotification(),
			scenarioDetailRequest.notification(),
			scenarioDetailRequest.notificationCondition()
		);

		missionService.updateBasicMission(oldScenario, scenarioDetailRequest.basicMissions());

		oldScenario.updateScenarioName(scenarioDetailRequest.scenarioName());
		oldScenario.updateMemo(scenarioDetailRequest.memo());
	}


	@Transactional
	public OrderUpdateResponse updateScenarioOrder(
		final Long memberId,
		final Long scenarioId,
		final ScenarioOrderUpdateRequest scenarioOrderUpdateRequest
	) {
		Scenario scenario = scenarioRepository.findByIdAndMemberId(scenarioId, memberId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		try {
			int toUpdateOrder = orderCalculator.getOrder(
				scenarioOrderUpdateRequest.prevOrder(),
				scenarioOrderUpdateRequest.nextOrder()
			);
			scenario.updateScenarioOrder(toUpdateOrder);
			return OrderUpdateResponse.from(List.of(scenario), false);

		} catch (ReorderRequiredException e) {
			int errorOrder = e.getErrorOrder();
			Notification notification = scenario.getNotification();
			List<Scenario> scenarios =
				scenarioRepository.findByMemberIdAndNotificationType(memberId, notification.getNotificationType());
			scenarios = orderCalculator.reorder(scenarios, scenarioId, errorOrder);

			return OrderUpdateResponse.from(scenarios, true);
		}
	}


	@Transactional
	public void deleteScenarioWithAllMissions(final Long memberId, final Long scenarioId) {
		Scenario scenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		notificationService.deleteNotification(scenario.getNotification());
		scenarioRepository.delete(scenario);
	}


	private ScenarioDetailResponse getScenarioDetailResponse(
		final Scenario scenario,
		final List<Mission> basicMissions,
		final NotificationInfoDto notificationInfo
	) {
		Notification notification = scenario.getNotification();

		NotificationResponse notificationResponse;
		NotificationConditionResponse notificationConditionResponse = null;

		if (notificationInfo == null) {
			notificationResponse = NotificationResponse.from(
				notification, null, null);
		} else {
			notificationResponse = NotificationResponse.from(
				notification,
				notificationInfo.isEveryDay(),
				notificationInfo.daysOfWeekOrdinal()
			);
			notificationConditionResponse = notificationInfo.notificationConditionResponse();
		}

		return ScenarioDetailResponse.from(
			scenario, basicMissions, notificationResponse, notificationConditionResponse);
	}

	private int getValidScenarioOrder(
		final int maxScenarioOrder,
		final Long memberId,
		final NotificationType notificationType
	) {
		try {
			return orderCalculator.getOrder(maxScenarioOrder, null);
		} catch (ReorderRequiredException e) {
			List<Scenario> scenarios =
				scenarioRepository.findByMemberIdAndNotificationType(memberId, notificationType);

			return orderCalculator.getMaxOrderAfterReorder(scenarios);
		}
	}

}
