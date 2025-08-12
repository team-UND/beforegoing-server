package com.und.server.scenario.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.NotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.BasicMissionRequest;
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.ScenarioNoNotificationRequest;
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.HomeScenarioResponse;
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
	private final ScenarioValidator scenarioValidator;
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
	public void addTodayMissionToScenario(
		Long memberId,
		Long scenarioId,
		TodayMissionRequest todayMissionRequest,
		LocalDate date
	) {
		Scenario scenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		missionService.addTodayMission(scenario, todayMissionRequest, date);
	}


	@Transactional
	public void addScenario(Long memberId, ScenarioDetailRequest scenarioDetailRequest) {
		addScenarioInternal(
			memberId,
			scenarioDetailRequest.scenarioName(),
			scenarioDetailRequest.memo(),
			scenarioDetailRequest.basicMissionList(),
			scenarioDetailRequest.notification().notificationType(),
			() -> notificationService.addNotification(
				scenarioDetailRequest.notification(),
				scenarioDetailRequest.notificationCondition()
			)
		);
	}


	@Transactional
	public void addScenarioWithoutNotification(
		Long memberId, ScenarioNoNotificationRequest scenarioNoNotificationRequest
	) {
		addScenarioInternal(
			memberId,
			scenarioNoNotificationRequest.scenarioName(),
			scenarioNoNotificationRequest.memo(),
			scenarioNoNotificationRequest.basicMissionList(),
			scenarioNoNotificationRequest.notificationType(),
			() -> notificationService.addWithoutNotification(scenarioNoNotificationRequest.notificationType())
		);
	}


	@Transactional
	public void updateScenario(Long memberId, Long scenarioId, ScenarioDetailRequest scenarioDetailRequest) {
		updateScenarioInternal(
			memberId,
			scenarioId,
			scenarioDetailRequest.scenarioName(),
			scenarioDetailRequest.memo(),
			scenarioDetailRequest.basicMissionList(),
			notification -> notificationService.updateNotification(
				notification,
				scenarioDetailRequest.notification(),
				scenarioDetailRequest.notificationCondition()
			)
		);
	}


	@Transactional
	public void updateScenarioWithoutNotification(
		Long memberId, Long scenarioId, ScenarioNoNotificationRequest scenarioNoNotificationRequest
	) {
		updateScenarioInternal(
			memberId,
			scenarioId,
			scenarioNoNotificationRequest.scenarioName(),
			scenarioNoNotificationRequest.memo(),
			scenarioNoNotificationRequest.basicMissionList(),
			notificationService::updateWithoutNotification
		);
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
				scenarioOrderUpdateRequest.prevOrder(),
				scenarioOrderUpdateRequest.nextOrder()
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


	private void addScenarioInternal(
		Long memberId,
		String scenarioName,
		String memo,
		List<BasicMissionRequest> missions,
		NotificationType notificationType,
		Supplier<Notification> notificationSupplier
	) {
		Member member = em.getReference(Member.class, memberId);

		List<Integer> orderList =
			scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, notificationType);
		scenarioValidator.validateMaxScenarioCount(orderList);

		int order = orderList.isEmpty()
			? OrderCalculator.START_ORDER
			: getValidScenarioOrder(Collections.max(orderList), memberId, notificationType);

		Notification notification = notificationSupplier.get();

		Scenario scenario = Scenario.builder()
			.member(member)
			.scenarioName(scenarioName)
			.memo(memo)
			.scenarioOrder(order)
			.notification(notification)
			.build();

		scenarioRepository.save(scenario);
		missionService.addBasicMission(scenario, missions);
	}

	private void updateScenarioInternal(
		Long memberId,
		Long scenarioId,
		String scenarioName,
		String memo,
		List<BasicMissionRequest> newBasicMissionList,
		Consumer<Notification> notificationUpdater
	) {
		Scenario oldScenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		notificationUpdater.accept(oldScenario.getNotification());

		missionService.updateBasicMission(oldScenario, newBasicMissionList);

		oldScenario.updateScenarioName(scenarioName);
		oldScenario.updateMemo(memo);
	}

	private ScenarioDetailResponse getScenarioDetailResponse(
		Scenario scenario,
		List<Mission> basicMissionList,
		NotificationInfoDto notificationInfo
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
				notificationInfo.dayOfWeekOrdinalList()
			);
			notificationConditionResponse = notificationInfo.notificationConditionResponse();
		}

		return ScenarioDetailResponse.from(
			scenario, basicMissionList, notificationResponse, notificationConditionResponse);
	}

	private int getValidScenarioOrder(
		int maxScenarioOrder,
		Long memberId,
		NotificationType notificationType
	) {
		try {
			return orderCalculator.getOrder(maxScenarioOrder, null);
		} catch (ReorderRequiredException e) {
			reorderScenarios(memberId, notificationType);
			int newMaxOrder =
				scenarioRepository.findMaxOrderByMemberIdAndNotificationType(
						memberId, notificationType)
					.orElse(OrderCalculator.START_ORDER);

			return orderCalculator.getOrder(newMaxOrder, null);
		}
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

}
