package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.notification.constants.NotifMethodType;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationDayOfWeekRequest;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.NotificationDayOfWeekResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.MissionRequest;
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ReorderRequiredException;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.ScenarioRepository;
import com.und.server.scenario.util.MissionTypeGrouper;
import com.und.server.scenario.util.OrderCalculator;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class ScenarioServiceTest {

	@InjectMocks
	private ScenarioService scenarioService;

	@Mock
	private MissionService missionService;

	@Mock
	private NotificationService notificationService;

	@Mock
	private ScenarioRepository scenarioRepository;

	@Mock
	private MissionTypeGrouper missionTypeGrouper;

	@Mock
	private OrderCalculator orderCalculator;

	@Mock
	private EntityManager em;


	@Test
	void Given_memberId_When_FindScenarios_Then_ReturnScenarios() {
		//given
		final Long memberId = 1L;

		final Member member = Member.builder()
			.id(memberId)
			.build();

		final Notification notification1 = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();
		final Notification notification2 = Notification.builder()
			.id(2L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		final Scenario scenarioA = Scenario.builder()
			.id(1L)
			.member(member)
			.scenarioName("시나리오A")
			.memo("메모A")
			.order(1)
			.notification(notification1)
			.build();
		final Scenario scenarioB = Scenario.builder()
			.id(1L)
			.member(member)
			.scenarioName("시나리오B")
			.memo("메모B")
			.order(2)
			.notification(notification2)
			.build();

		final List<Scenario> scenarioList = List.of(scenarioA, scenarioB);

		//when
		Mockito
			.when(scenarioRepository.findByMemberIdAndNotification_NotifTypeOrderByOrder(memberId, NotifType.TIME))
			.thenReturn(scenarioList);

		List<ScenarioResponse> result = scenarioService.findScenariosByMemberId(memberId, NotifType.TIME);

		//then
		assertNotNull(result);
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0).getScenarioName()).isEqualTo("시나리오A");
		assertThat(result.get(1).getScenarioName()).isEqualTo("시나리오B");
	}


	@Test
	void Given_validScenario_When_findScenarioByScenarioId_Then_returnResponse() {
		// given
		final Long memberId = 1L;
		final Long scenarioId = 10L;

		final Member member = Member.builder()
			.id(memberId)
			.build();

		final Notification notification = Notification.builder()
			.id(100L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		final Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.scenarioName("아침 루틴")
			.memo("메모")
			.order(1)
			.notification(notification)
			.missionList(List.of())
			.build();

		final TimeNotificationResponse notifDetail = TimeNotificationResponse.builder()
			.hour(8)
			.minute(30)
			.build();

		final NotificationInfoDto notifInfoDto = new NotificationInfoDto(
			true,
			List.of(new NotificationDayOfWeekResponse(1L, 1)),
			notifDetail
		);

		// mock
		Mockito.when(scenarioRepository.findById(scenarioId)).thenReturn(Optional.of(scenario));
		Mockito.when(notificationService.findNotificationDetails(notification)).thenReturn(notifInfoDto);
		Mockito.when(missionTypeGrouper.groupAndSortByType(scenario.getMissionList(), MissionType.BASIC))
			.thenReturn(List.of());

		// when
		ScenarioDetailResponse response = scenarioService.findScenarioByScenarioId(memberId, scenarioId);

		// then
		assertNotNull(response);
		assertThat(response.getScenarioId()).isEqualTo(scenarioId);
		assertThat(response.getNotificationCondition()).isInstanceOf(TimeNotificationResponse.class);
		TimeNotificationResponse detail = (TimeNotificationResponse) response.getNotificationCondition();
		assertThat(detail.getHour()).isEqualTo(8);
		assertThat(detail.getMinute()).isEqualTo(30);
	}


	@Test
	void Given_notExistScenario_When_findScenarioByScenarioId_Then_throwNotFoundException() {
		// given
		final Long memberId = 1L;
		final Long scenarioId = 99L;

		Mockito.when(scenarioRepository.findById(scenarioId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> scenarioService.findScenarioByScenarioId(memberId, scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}


	@Test
	void Given_otherUserScenario_When_findScenarioByScenarioId_Then_throwUnauthorizedException() {
		// given
		final Long memberId = 1L;
		final Long scenarioId = 10L;

		final Member member = Member.builder()
			.id(2L)
			.build();

		final Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.scenarioName("내 시나리오 아님")
			.order(1)
			.notification(Notification.builder().isActive(false).notifType(NotifType.TIME).build())
			.missionList(List.of())
			.build();

		Mockito.when(scenarioRepository.findById(scenarioId))
			.thenReturn(Optional.of(scenario));

		// when & then
		assertThatThrownBy(() -> scenarioService.findScenarioByScenarioId(memberId, scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.UNAUTHORIZED_ACCESS.getMessage());
	}


	@Test
	void Given_ValidMemberAndScenario_When_AddTodayMissionToScenario_Then_InvokeMissionService() {
		Long memberId = 1L;
		Long scenarioId = 10L;

		Member member = Member.builder().id(memberId).build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.build();

		TodayMissionRequest request = new TodayMissionRequest("Stretch");

		Mockito.when(scenarioRepository.findById(scenarioId))
			.thenReturn(Optional.of(scenario));

		scenarioService.addTodayMissionToScenario(memberId, scenarioId, request);

		verify(missionService).addTodayMission(scenario, request);
	}


	@Test
	void Given_OtherUserScenario_When_AddTodayMissionToScenario_Then_ThrowUnauthorizedException() {
		Long requestMemberId = 1L;
		Long scenarioId = 10L;

		Member otherUser = Member.builder().id(999L).build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(otherUser)
			.build();

		TodayMissionRequest request = new TodayMissionRequest("Stretch");

		Mockito.when(scenarioRepository.findById(scenarioId))
			.thenReturn(Optional.of(scenario));

		assertThatThrownBy(() ->
			scenarioService.addTodayMissionToScenario(requestMemberId, scenarioId, request)
		).isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.UNAUTHORIZED_ACCESS.getMessage());
	}


	@Test
	void Given_ValidRequest_When_AddScenario_Then_SaveScenarioAndAddMissions() {
		//given
		Long memberId = 1L;
		int calculatedOrder = 3000;

		Member member = Member.builder().id(memberId).build();
		given(em.getReference(Member.class, memberId)).willReturn(member);

		MissionRequest mission1 = new MissionRequest();
		mission1.setContent("Run");
		mission1.setMissionType(MissionType.BASIC);

		MissionRequest mission2 = new MissionRequest();
		mission2.setContent("Read");
		mission2.setMissionType(MissionType.BASIC);

		List<MissionRequest> missionList = List.of(mission1, mission2);

		NotificationRequest notifRequest = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotifType.TIME)
			.notificationMethodType(NotifMethodType.ALARM)
			.dayOfWeekOrdinalList(List.of(
				new NotificationDayOfWeekRequest(1L, 1),
				new NotificationDayOfWeekRequest(2L, 2)
			))
			.build();

		TimeNotificationRequest condition = TimeNotificationRequest.builder()
			.hour(9)
			.minute(0)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("Morning")
			.memo("Routine")
			.basicMissionList(missionList)
			.notification(notifRequest)
			.notificationCondition(condition)
			.build();

		Notification savedNotification = Notification.builder()
			.id(10L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.notifMethodType(NotifMethodType.ALARM)
			.build();

		given(notificationService.addNotification(notifRequest, condition)).willReturn(savedNotification);

		given(orderCalculator.getOrder(anyInt(), isNull())).willReturn(calculatedOrder);

		ArgumentCaptor<Scenario> scenarioCaptor = ArgumentCaptor.forClass(Scenario.class);

		// when
		scenarioService.addScenario(memberId, scenarioRequest);

		// then
		verify(notificationService).addNotification(notifRequest, condition);
		verify(missionService).addBasicMission(any(Scenario.class), eq(missionList));
		verify(scenarioRepository).save(scenarioCaptor.capture());

		Scenario saved = scenarioCaptor.getValue();

		assertThat(saved.getScenarioName()).isEqualTo("Morning");
		assertThat(saved.getMemo()).isEqualTo("Routine");
		assertThat(saved.getOrder()).isEqualTo(calculatedOrder);
		assertThat(saved.getNotification()).isEqualTo(savedNotification);
		assertThat(saved.getMember()).isEqualTo(member);
	}


	@Test
	void Given_ReorderRequired_When_AddScenario_Then_ReorderAndRetry() {
		// given
		Long memberId = 1L;
		int reorderedOrder = 5000;

		Member member = Member.builder().id(memberId).build();
		given(em.getReference(Member.class, memberId)).willReturn(member);

		NotificationRequest notifRequest = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotifType.TIME)
			.notificationMethodType(NotifMethodType.ALARM)
			.build();

		TimeNotificationRequest condition = TimeNotificationRequest.builder()
			.hour(7)
			.minute(0)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("Evening")
			.memo("Routine")
			.basicMissionList(List.of())
			.notification(notifRequest)
			.notificationCondition(condition)
			.build();

		Notification savedNotification = Notification.builder()
			.id(11L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.notifMethodType(NotifMethodType.ALARM)
			.build();


		given(notificationService.addNotification(notifRequest, condition))
			.willReturn(savedNotification);
		given(orderCalculator.getOrder(anyInt(), isNull()))
			.willThrow(new ReorderRequiredException())
			.willReturn(reorderedOrder);
		given(scenarioRepository.findMaxOrderByMemberIdAndNotifType(memberId, NotifType.TIME))
			.willReturn(Optional.of(10_000_000));

		Scenario s1 = Scenario.builder().id(1L).order(10_000_000).build();
		given(scenarioRepository.findByMemberIdAndNotification_NotifTypeOrderByOrder(memberId, NotifType.TIME))
			.willReturn(List.of(s1));

		ArgumentCaptor<Scenario> captor = ArgumentCaptor.forClass(Scenario.class);

		// when
		scenarioService.addScenario(memberId, scenarioRequest);

		// then
		verify(scenarioRepository).saveAll(any());
		verify(scenarioRepository).save(captor.capture());

		Scenario saved = captor.getValue();
		assertThat(saved.getOrder()).isEqualTo(reorderedOrder);
	}


}
