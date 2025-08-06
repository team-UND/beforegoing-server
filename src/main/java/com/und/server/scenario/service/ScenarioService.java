package com.und.server.scenario.service;

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
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.requeset.ScenarioDetailRequest;
import com.und.server.scenario.dto.requeset.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ReorderRequiredException;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.ScenarioRepository;
import com.und.server.scenario.util.MissionTypeGrouper;
import com.und.server.scenario.util.OrderCalculator;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScenarioService {

	private final NotificationService notificationService;
	private final MissionService missionService;
	private final ScenarioRepository scenarioRepository;
	private final MissionTypeGrouper missionTypeGrouper;
	private final OrderCalculator orderCalculator;
	private final EntityManager em;


	@Transactional(readOnly = true)
	public List<ScenarioResponse> findScenariosByMemberId(Long memberId, NotifType notifType) {
		List<Scenario> scenarioList =
			scenarioRepository.findByMemberIdAndNotification_NotifTypeOrderByOrder(memberId, notifType);

		return ScenarioResponse.listOf(scenarioList);
	}


	@Transactional(readOnly = true)
	public Scenario findScenarioByScenarioId(Long scenarioId) {
		return scenarioRepository.findById(scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));
	}


	@Transactional(readOnly = true)
	public ScenarioDetailResponse findScenarioByScenarioId(Long memberId, Long scenarioId) {
		Scenario scenario = findScenarioByScenarioId(scenarioId);
		validateScenarioAccessMember(memberId, scenario);

		List<Mission> groupdBasicMissionList =
			missionTypeGrouper.groupAndSortByType(scenario.getMissionList(), MissionType.BASIC);

		Notification notification = scenario.getNotification();

		NotificationInfoDto notifInfo = notificationService.findNotificationDetails(notification);

		return getScenarioDetailResponse(scenario, groupdBasicMissionList, notifInfo);
	}

	@Transactional
	public void addTodayMissionToScenario(Long memberId, Long scenarioId, TodayMissionRequest missionAddInfo) {
		Scenario scenario = findScenarioByScenarioId(scenarioId);
		validateScenarioAccessMember(memberId, scenario);

		missionService.addTodayMission(scenario, missionAddInfo);
	}


	@Transactional
	public void addScenario(Long memberId, ScenarioDetailRequest scenarioInfo) {
		NotificationRequest notifInfo = scenarioInfo.getNotification();
		Member member = em.getReference(Member.class, memberId);

		Notification notification =
			notificationService.addNotification(notifInfo, scenarioInfo.getNotificationCondition());

		int order = getValidScenarioOrder(memberId, notifInfo);

		Scenario scenario = Scenario.builder()
			.member(member)
			.scenarioName(scenarioInfo.getScenarioName())
			.memo(scenarioInfo.getMemo())
			.order(order)
			.notification(notification)
			.build();

		missionService.addBasicMission(scenario, scenarioInfo.getBasicMissionList());
		scenarioRepository.save(scenario);
	}

	private int getValidScenarioOrder(Long memberId, NotificationRequest notifInfo) {
		int maxScenarioOrder = getMaxScenarioOrder(memberId, notifInfo);

		try {
			return orderCalculator.calculateOrder(null, maxScenarioOrder);
		} catch (ReorderRequiredException e) {
			reorderScenarios(memberId, notifInfo.getNotificationType());
			int newMaxOrder = getMaxScenarioOrder(memberId, notifInfo);

			return orderCalculator.calculateOrder(null, newMaxOrder);
		}
	}

	private Integer getMaxScenarioOrder(Long memberId, NotificationRequest notifInfo) {
		return scenarioRepository.findMaxOrderByMemberIdAndNotifType(memberId, notifInfo.getNotificationType())
			.orElse(OrderCalculator.START_ORDER);
	}


	public void validateScenarioAccessMember(Long requestMemberId, Scenario scenario) {
		if (scenario.isAccessibleMember(requestMemberId)) {
			throw new ServerException(ScenarioErrorResult.UNAUTHORIZED_ACCESS);
		}
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
