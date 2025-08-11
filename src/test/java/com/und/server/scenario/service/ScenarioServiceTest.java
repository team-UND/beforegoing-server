package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
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
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.MissionRequest;
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ReorderRequiredException;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.ScenarioRepository;
import com.und.server.scenario.util.MissionTypeGroupSorter;
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
	private MissionTypeGroupSorter missionTypeGrouper;

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
			.notificationType(NotifType.TIME)
			.build();
		final Notification notification2 = Notification.builder()
			.id(2L)
			.isActive(true)
			.notificationType(NotifType.TIME)
			.build();

		final Scenario scenarioA = Scenario.builder()
			.id(1L)
			.member(member)
			.scenarioName("시나리오A")
			.memo("메모A")
			.scenarioOrder(1)
			.notification(notification1)
			.build();
		final Scenario scenarioB = Scenario.builder()
			.id(1L)
			.member(member)
			.scenarioName("시나리오B")
			.memo("메모B")
			.scenarioOrder(2)
			.notification(notification2)
			.build();

		final List<Scenario> scenarioList = List.of(scenarioA, scenarioB);

		//when
		Mockito
			.when(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotifType.TIME))
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
			.notificationType(NotifType.TIME)
			.build();

		final Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.scenarioName("아침 루틴")
			.memo("메모")
			.scenarioOrder(1)
			.notification(notification)
			.missionList(List.of())
			.build();

		final TimeNotificationResponse notifDetail = TimeNotificationResponse.builder()
			.startHour(8)
			.startMinute(30)
			.build();

		final NotificationInfoDto notifInfoDto = new NotificationInfoDto(
			true,
			List.of(1),
			notifDetail
		);

		// mock
		Mockito.when(scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.of(scenario));
		Mockito.when(notificationService.findNotificationDetails(notification)).thenReturn(notifInfoDto);
		Mockito.when(missionTypeGrouper.groupAndSortByType(scenario.getMissionList(), MissionType.BASIC))
			.thenReturn(List.of());

		// when
		ScenarioDetailResponse response = scenarioService.findScenarioDetailByScenarioId(memberId, scenarioId);

		// then
		assertNotNull(response);
		assertThat(response.getScenarioId()).isEqualTo(scenarioId);
		assertThat(response.getNotificationCondition()).isInstanceOf(TimeNotificationResponse.class);
		TimeNotificationResponse detail = (TimeNotificationResponse) response.getNotificationCondition();
		assertThat(detail.getStartHour()).isEqualTo(8);
		assertThat(detail.getStartMinute()).isEqualTo(30);
	}


	@Test
	void Given_notExistScenario_When_findScenarioByScenarioId_Then_throwNotFoundException() {
		// given
		final Long memberId = 1L;
		final Long scenarioId = 99L;

		Mockito.when(scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> scenarioService.findScenarioDetailByScenarioId(memberId, scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}


	@Test
	void Given_otherUserScenario_When_findScenarioByScenarioId_Then_throwNotFoundException() {
		// given
		final Long memberId = 1L;
		final Long scenarioId = 10L;

		// 다른 사용자의 시나리오는 존재하지 않음 (권한 검증으로 인해)
		Mockito.when(scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> scenarioService.findScenarioDetailByScenarioId(memberId, scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}


	@Test
	void Given_ValidMemberAndScenario_When_AddTodayMissionToScenario_Then_InvokeMissionService() {
		Long memberId = 1L;
		Long scenarioId = 10L;
		LocalDate date = LocalDate.now();

		Member member = Member.builder().id(memberId).build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.missionList(new java.util.ArrayList<>())
			.build();

		TodayMissionRequest request = new TodayMissionRequest("Stretch");

		Mockito.when(scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.of(scenario));

		scenarioService.addTodayMissionToScenario(memberId, scenarioId, request, date);

		verify(missionService).addTodayMission(scenario, request, date);
	}


	@Test
	void Given_OtherUserScenario_When_AddTodayMissionToScenario_Then_ThrowNotFoundException() {
		Long requestMemberId = 1L;
		Long scenarioId = 10L;
		LocalDate date = LocalDate.now();

		TodayMissionRequest request = new TodayMissionRequest("Stretch");

		Mockito.when(scenarioRepository.findFetchByIdAndMemberId(requestMemberId, scenarioId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() ->
			scenarioService.addTodayMissionToScenario(requestMemberId, scenarioId, request, date)
		).isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}


	@Test
	void Given_ValidRequest_When_AddScenario_Then_SaveScenarioAndAddMissions() {
		//given
		Long memberId = 1L;
		int calculatedOrder = 1000;

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
			.dayOfWeekOrdinalList(List.of(1, 2))
			.build();

		TimeNotificationRequest condition = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
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
			.notificationType(NotifType.TIME)
			.notificationMethodType(NotifMethodType.ALARM)
			.build();

		given(notificationService.addNotification(notifRequest, condition)).willReturn(savedNotification);

		given(scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, NotifType.TIME))
			.willReturn(List.of());

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
		assertThat(saved.getScenarioOrder()).isEqualTo(calculatedOrder);
		assertThat(saved.getNotification()).isEqualTo(savedNotification);
		assertThat(saved.getMember().getId()).isEqualTo(member.getId());
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
			.startHour(7)
			.startMinute(0)
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
			.notificationType(NotifType.TIME)
			.notificationMethodType(NotifMethodType.ALARM)
			.build();


		given(notificationService.addNotification(notifRequest, condition))
			.willReturn(savedNotification);
		given(scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, NotifType.TIME))
			.willReturn(List.of(10_000_000));
		given(orderCalculator.getOrder(anyInt(), isNull()))
			.willThrow(new ReorderRequiredException())
			.willReturn(reorderedOrder);
		given(scenarioRepository.findMaxOrderByMemberIdAndNotifType(memberId, NotifType.TIME))
			.willReturn(Optional.of(10_000_000));

		Scenario s1 = Scenario.builder().id(1L).scenarioOrder(10_000_000).build();
		given(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotifType.TIME))
			.willReturn(List.of(s1));

		ArgumentCaptor<Scenario> captor = ArgumentCaptor.forClass(Scenario.class);

		// when
		scenarioService.addScenario(memberId, scenarioRequest);

		// then
		verify(scenarioRepository).saveAll(any());
		verify(scenarioRepository).save(captor.capture());

		Scenario saved = captor.getValue();
		assertThat(saved.getScenarioOrder()).isEqualTo(reorderedOrder);
	}

	@Test
	void Given_PastDate_When_AddTodayMissionToScenario_Then_ThrowException() {
		// given
		Long memberId = 1L;
		Long scenarioId = 10L;
		LocalDate pastDate = LocalDate.now().minusDays(1);

		TodayMissionRequest request = new TodayMissionRequest("Past Mission");

		// when & then
		assertThatThrownBy(() ->
			scenarioService.addTodayMissionToScenario(memberId, scenarioId, request, pastDate)
		).isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.INVALID_TODAY_MISSION_DATE.getMessage());
	}


	@Test
	void Given_ValidRequest_When_UpdateScenario_Then_UpdateScenarioAndNotification() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;

		Member member = Member.builder().id(memberId).build();
		Notification oldNotification = Notification.builder()
			.id(1L)
			.notificationType(NotifType.TIME)
			.build();
		Notification newNotification = Notification.builder()
			.id(2L)
			.notificationType(NotifType.TIME)
			.build();

		Scenario oldScenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.scenarioName("기존 시나리오")
			.memo("기존 메모")
			.notification(oldNotification)
			.missionList(new java.util.ArrayList<>())
			.build();

		NotificationRequest notifRequest = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotifType.TIME)
			.notificationMethodType(NotifMethodType.ALARM)
			.dayOfWeekOrdinalList(List.of(1, 2))
			.build();

		TimeNotificationRequest condition = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("수정된 시나리오")
			.memo("수정된 메모")
			.basicMissionList(List.of())
			.notification(notifRequest)
			.notificationCondition(condition)
			.build();

		Mockito.when(scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.of(oldScenario));
		Mockito.when(notificationService.updateNotification(oldNotification, notifRequest, condition))
			.thenReturn(newNotification);

		// when
		scenarioService.updateScenario(memberId, scenarioId, scenarioRequest);

		// then
		assertThat(oldScenario.getScenarioName()).isEqualTo("수정된 시나리오");
		assertThat(oldScenario.getMemo()).isEqualTo("수정된 메모");
		assertThat(oldScenario.getNotification()).isEqualTo(newNotification);
		verify(notificationService).updateNotification(oldNotification, notifRequest, condition);
		verify(missionService).updateBasicMission(oldScenario, List.of());
	}


	@Test
	void Given_ValidRequest_When_UpdateScenarioOrder_Then_UpdateOrder() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		int newOrder = 1500;

		Member member = Member.builder().id(memberId).build();
		Notification notification = Notification.builder()
			.id(1L)
			.notificationType(NotifType.TIME)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.notification(notification)
			.scenarioOrder(1000)
			.build();

		ScenarioOrderUpdateRequest orderRequest = ScenarioOrderUpdateRequest.builder()
			.prevOrder(1000)
			.nextOrder(2000)
			.build();

		Mockito.when(scenarioRepository.findByIdAndMemberId(scenarioId, memberId))
			.thenReturn(Optional.of(scenario));
		Mockito.when(orderCalculator.getOrder(1000, 2000))
			.thenReturn(newOrder);

		// when
		scenarioService.updateScenarioOrder(memberId, scenarioId, orderRequest);

		// then
		assertThat(scenario.getScenarioOrder()).isEqualTo(newOrder);
		verify(orderCalculator).getOrder(1000, 2000);
	}


	@Test
	void Given_ReorderRequired_When_UpdateScenarioOrder_Then_ReorderScenarios() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;

		Member member = Member.builder().id(memberId).build();
		Notification notification = Notification.builder()
			.id(1L)
			.notificationType(NotifType.TIME)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.notification(notification)
			.scenarioOrder(1000)
			.build();

		ScenarioOrderUpdateRequest orderRequest = ScenarioOrderUpdateRequest.builder()
			.prevOrder(1000)
			.nextOrder(1001) // 너무 가까워서 ReorderRequiredException 발생
			.build();

		Scenario scenario1 = Scenario.builder().id(1L).scenarioOrder(1000).build();
		Scenario scenario2 = Scenario.builder().id(2L).scenarioOrder(2000).build();

		Mockito.when(scenarioRepository.findByIdAndMemberId(scenarioId, memberId))
			.thenReturn(Optional.of(scenario));
		Mockito.when(orderCalculator.getOrder(1000, 1001))
			.thenThrow(new ReorderRequiredException());
		Mockito.when(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotifType.TIME))
			.thenReturn(List.of(scenario1, scenario2));

		// when
		scenarioService.updateScenarioOrder(memberId, scenarioId, orderRequest);

		// then
		verify(scenarioRepository).saveAll(anyList());
		assertThat(scenario1.getScenarioOrder()).isEqualTo(1000);
		assertThat(scenario2.getScenarioOrder()).isEqualTo(2000);
	}


	@Test
	void Given_ValidRequest_When_DeleteScenarioWithAllMissions_Then_DeleteScenarioAndNotification() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;

		Member member = Member.builder().id(memberId).build();
		Notification notification = Notification.builder()
			.id(1L)
			.notificationType(NotifType.TIME)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.notification(notification)
			.build();

		Mockito.when(scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.of(scenario));

		// when
		scenarioService.deleteScenarioWithAllMissions(memberId, scenarioId);

		// then
		verify(notificationService).deleteNotification(notification);
		verify(scenarioRepository).delete(scenario);
	}


	@Test
	void Given_NotExistScenario_When_UpdateScenario_Then_ThrowNotFoundException() {
		// given
		Long memberId = 1L;
		Long scenarioId = 99L;

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("수정할 시나리오")
			.memo("수정할 메모")
			.build();

		Mockito.when(scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> scenarioService.updateScenario(memberId, scenarioId, scenarioRequest))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}


	@Test
	void Given_NotExistScenario_When_UpdateScenarioOrder_Then_ThrowNotFoundException() {
		// given
		Long memberId = 1L;
		Long scenarioId = 99L;

		ScenarioOrderUpdateRequest orderRequest = ScenarioOrderUpdateRequest.builder()
			.prevOrder(1000)
			.nextOrder(2000)
			.build();

		Mockito.when(scenarioRepository.findByIdAndMemberId(scenarioId, memberId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> scenarioService.updateScenarioOrder(memberId, scenarioId, orderRequest))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}


	@Test
	void Given_NotExistScenario_When_DeleteScenarioWithAllMissions_Then_ThrowNotFoundException() {
		// given
		Long memberId = 1L;
		Long scenarioId = 99L;

		Mockito.when(scenarioRepository.findFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> scenarioService.deleteScenarioWithAllMissions(memberId, scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}

}
