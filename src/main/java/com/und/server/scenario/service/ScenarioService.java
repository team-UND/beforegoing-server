package com.und.server.scenario.service;

import java.time.LocalDate;
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
import com.und.server.scenario.dto.request.TodayMissionRequest;
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
	public Scenario findScenarioByScenarioId(Long memberId, Long scenarioId) {
		return scenarioRepository.findByIdAndMemberId(memberId, scenarioId)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));
	}


	@Transactional(readOnly = true)
	public ScenarioDetailResponse findScenarioDetailByScenarioId(Long memberId, Long scenarioId) {
		LocalDate today = LocalDate.now();

		Scenario scenario = scenarioRepository.findByIdWithDefaultMissions(memberId, scenarioId, today)
			.orElseThrow(() -> new ServerException(ScenarioErrorResult.NOT_FOUND_SCENARIO));

		List<Mission> groupdBasicMissionList =
			missionTypeGrouper.groupAndSortByType(scenario.getMissionList(), MissionType.BASIC);

		Notification notification = scenario.getNotification();

		NotificationInfoDto notifInfo = notificationService.findNotificationDetails(notification);

		return getScenarioDetailResponse(scenario, groupdBasicMissionList, notifInfo);
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

		Scenario scenario = findScenarioByScenarioId(memberId, scenarioId);

		missionService.addTodayMission(scenario, missionAddInfo, date);
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
			return orderCalculator.getOrder(maxScenarioOrder, null);
		} catch (ReorderRequiredException e) {
			reorderScenarios(memberId, notifInfo.getNotificationType());
			int newMaxOrder = getMaxScenarioOrder(memberId, notifInfo);

			return orderCalculator.getOrder(newMaxOrder, null);
		}
	}

	private Integer getMaxScenarioOrder(Long memberId, NotificationRequest notifInfo) {
		return scenarioRepository.findMaxOrderByMemberIdAndNotifType(memberId, notifInfo.getNotificationType())
			.orElse(OrderCalculator.START_ORDER);
	}


	@Transactional
	public void updateScenario(Long memberId, Long scenarioId, ScenarioDetailRequest scenarioInfo) {
		Scenario oldSCenario = findScenarioByScenarioId(memberId, scenarioId);

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
