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
	public List<HomeScenarioResponse> findHomeScenariosByMemberId(Long memberId, NotificationType notificationType) {
		List<Scenario> scenarios =
			scenarioRepository.findByMemberIdAndNotificationType(memberId, notificationType);

		return HomeScenarioResponse.listFrom(scenarios);
	}


	@Transactional(readOnly = true)
	public List<ScenarioResponse> findScenariosByMemberId(Long memberId, NotificationType notificationType) {
		List<Scenario> scenarios =
			scenarioRepository.findByMemberIdAndNotificationType(memberId, notificationType);

		return ScenarioResponse.listFrom(scenarios);
	}


	@Transactional(readOnly = true)
	public ScenarioDetailResponse findScenarioDetailByScenarioId(Long memberId, Long scenarioId) {
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
		Long memberId,
		Long scenarioId,
		TodayMissionRequest todayMissionRequest,
		LocalDate date
	) {
		Scenario scenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		return missionService.addTodayMission(scenario, todayMissionRequest, date);
	}


	@Transactional
	public void addScenario(Long memberId, ScenarioDetailRequest scenarioDetailRequest) {
		addScenarioInternal(
			memberId,
			scenarioDetailRequest.scenarioName(),
			scenarioDetailRequest.memo(),
			scenarioDetailRequest.basicMissions(),
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
			scenarioNoNotificationRequest.basicMissions(),
			scenarioNoNotificationRequest.notificationType(),
			() -> notificationService.addWithoutNotification(scenarioNoNotificationRequest.notificationType())
		);
	}


	@Transactional
	public void updateScenario(
		Long memberId,
		Long scenarioId,
		ScenarioDetailRequest scenarioDetailRequest
	) {
		updateScenarioInternal(
			memberId,
			scenarioId,
			scenarioDetailRequest.scenarioName(),
			scenarioDetailRequest.memo(),
			scenarioDetailRequest.basicMissions(),
			notification -> notificationService.updateNotification(
				notification,
				scenarioDetailRequest.notification(),
				scenarioDetailRequest.notificationCondition()
			)
		);
	}


	@Transactional
	public void updateScenarioWithoutNotification(
		Long memberId,
		Long scenarioId,
		ScenarioNoNotificationRequest scenarioNoNotificationRequest
	) {
		updateScenarioInternal(
			memberId,
			scenarioId,
			scenarioNoNotificationRequest.scenarioName(),
			scenarioNoNotificationRequest.memo(),
			scenarioNoNotificationRequest.basicMissions(),
			notificationService::updateWithoutNotification
		);
	}


	@Transactional
	public OrderUpdateResponse updateScenarioOrder(
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

		List<Integer> orders =
			scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, notificationType);
		scenarioValidator.validateMaxScenarioCount(orders);

		int order = orders.isEmpty()
			? OrderCalculator.START_ORDER
			: getValidScenarioOrder(Collections.max(orders), memberId, notificationType);

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
		List<BasicMissionRequest> newBasicMissions,
		Consumer<Notification> notificationUpdater
	) {
		Scenario oldScenario = scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		notificationUpdater.accept(oldScenario.getNotification());

		missionService.updateBasicMission(oldScenario, newBasicMissions);

		oldScenario.updateScenarioName(scenarioName);
		oldScenario.updateMemo(memo);
	}

	private ScenarioDetailResponse getScenarioDetailResponse(
		Scenario scenario,
		List<Mission> basicMissions,
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
				notificationInfo.daysOfWeekOrdinal()
			);
			notificationConditionResponse = notificationInfo.notificationConditionResponse();
		}

		return ScenarioDetailResponse.from(
			scenario, basicMissions, notificationResponse, notificationConditionResponse);
	}

	private int getValidScenarioOrder(
		int maxScenarioOrder,
		Long memberId,
		NotificationType notificationType
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
