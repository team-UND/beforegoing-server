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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.ScenarioResponseListWrapper;
import com.und.server.scenario.dto.request.BasicMissionRequest;
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.response.OrderUpdateResponse;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.event.publisher.ScenarioEventPublisher;
import com.und.server.scenario.exception.ReorderRequiredException;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.repository.ScenarioRepository;
import com.und.server.scenario.util.MissionTypeGroupSorter;
import com.und.server.scenario.util.OrderCalculator;
import com.und.server.scenario.util.ScenarioValidator;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
	private MissionRepository missionRepository;

	@Mock
	private MissionTypeGroupSorter missionTypeGroupSorter;

	@Mock
	private OrderCalculator orderCalculator;

	@Mock
	private EntityManager em;

	@Mock
	private ScenarioValidator scenarioValidator;

	@Mock
	private ScenarioEventPublisher notificationEventPublisher;

	@Mock
	private Clock clock;

	@BeforeEach
	void setUp() {
		// Clock 설정
		when(clock.withZone(ZoneId.of("Asia/Seoul"))).thenReturn(Clock.fixed(
			LocalDate.of(2024, 1, 15).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(),
			ZoneId.of("Asia/Seoul")
		));
	}

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
			.notificationType(NotificationType.TIME)
			.build();
		final Notification notification2 = Notification.builder()
			.id(2L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
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
			.when(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.thenReturn(scenarioList);

		ScenarioResponseListWrapper result = scenarioService.findScenariosByMemberId(memberId, NotificationType.TIME);

		//then
		assertNotNull(result);
		assertThat(result.scenarioResponses())
			.hasSize(2)
			.satisfies(r ->
				assertThat(r.get(0).scenarioName()).isEqualTo("시나리오A"))
			.satisfies(r ->
				assertThat(r.get(1).scenarioName()).isEqualTo("시나리오B"));
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
			.notificationType(NotificationType.TIME)
			.build();

		final Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.scenarioName("아침 루틴")
			.memo("메모")
			.scenarioOrder(1)
			.notification(notification)
			.missions(List.of())
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
		Mockito.when(scenarioRepository.findScenarioDetailFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.of(scenario));
		Mockito.when(notificationService.findNotificationDetails(notification)).thenReturn(notifDetail);
		Mockito.when(missionTypeGroupSorter.groupAndSortByType(scenario.getMissions(), MissionType.BASIC))
			.thenReturn(List.of());

		// when
		ScenarioDetailResponse response = scenarioService.findScenarioDetailByScenarioId(memberId, scenarioId);

		// then
		assertNotNull(response);
		assertThat(response)
			.satisfies(r -> assertThat(r.scenarioId()).isEqualTo(scenarioId))
			.satisfies(r ->
				assertThat(r.notificationCondition()).isInstanceOf(TimeNotificationResponse.class))
			.satisfies(r -> {
				TimeNotificationResponse detail = (TimeNotificationResponse) r.notificationCondition();
				assertThat(detail.startHour()).isEqualTo(8);
				assertThat(detail.startMinute()).isEqualTo(30);
			});
	}


	@Test
	void Given_notExistScenario_When_findScenarioByScenarioId_Then_throwNotFoundException() {
		// given
		final Long memberId = 1L;
		final Long scenarioId = 99L;

		Mockito.when(scenarioRepository.findScenarioDetailFetchByIdAndMemberId(memberId, scenarioId))
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

		Mockito.when(scenarioRepository.findScenarioDetailFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> scenarioService.findScenarioDetailByScenarioId(memberId, scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}

	@Test
	void Given_ValidRequest_When_AddScenario_Then_SaveScenarioAndAddMissions() {
		//given
		Long memberId = 1L;
		int calculatedOrder = 100000;

		Member member = Member.builder().id(memberId).build();
		given(em.getReference(Member.class, memberId)).willReturn(member);

		BasicMissionRequest mission1 = BasicMissionRequest.builder()
			.content("Run")
			.build();

		BasicMissionRequest mission2 = BasicMissionRequest.builder()
			.content("Read")
			.build();

		List<BasicMissionRequest> missionList = List.of(mission1, mission2);

		NotificationRequest notifRequest = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeekOrdinal(List.of(1, 2))
			.build();

		TimeNotificationRequest condition = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("Morning")
			.memo("Routine")
			.basicMissions(missionList)
			.notification(notifRequest)
			.notificationCondition(condition)
			.build();

		Notification savedNotification = Notification.builder()
			.id(10L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.build();

		given(notificationService.addNotification(notifRequest, condition)).willReturn(savedNotification);

		given(scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(List.of());

		ArgumentCaptor<Scenario> scenarioCaptor = ArgumentCaptor.forClass(Scenario.class);

		Mission savedMission1 = Mission.builder()
			.id(1L)
			.content("Run")
			.missionType(MissionType.BASIC)
			.build();

		Mission savedMission2 = Mission.builder()
			.id(2L)
			.content("Read")
			.missionType(MissionType.BASIC)
			.build();

		List<Mission> savedMissions = List.of(savedMission1, savedMission2);
		List<Mission> groupedBasicMissions = List.of(savedMission1, savedMission2);

		given(missionService.addBasicMission(any(Scenario.class), eq(missionList)))
			.willReturn(savedMissions);

		Scenario createdScenario = Scenario.builder()
			.id(1L)
			.scenarioName("Morning")
			.memo("Routine")
			.scenarioOrder(calculatedOrder)
			.notification(savedNotification)
			.member(member)
			.build();
		List<ScenarioResponse> expectedResponse = List.of(ScenarioResponse.from(createdScenario));
		given(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(List.of(createdScenario));

		// when
		List<ScenarioResponse> result = scenarioService.addScenario(memberId, scenarioRequest);

		// then
		verify(notificationService).addNotification(notifRequest, condition);
		verify(missionService).addBasicMission(any(Scenario.class), eq(missionList));
		verify(scenarioRepository).save(scenarioCaptor.capture());
		verify(notificationEventPublisher).publishScenarioCreateEvent(eq(memberId), any(Scenario.class),
			eq(NotificationType.TIME));

		Scenario saved = scenarioCaptor.getValue();

		assertThat(saved)
			.satisfies(s -> assertThat(s.getScenarioName()).isEqualTo("Morning"))
			.satisfies(s -> assertThat(s.getMemo()).isEqualTo("Routine"))
			.satisfies(s -> assertThat(s.getScenarioOrder()).isEqualTo(calculatedOrder))
			.satisfies(s -> assertThat(s.getNotification()).isEqualTo(savedNotification))
			.satisfies(s -> assertThat(s.getMember().getId()).isEqualTo(member.getId()));

		assertThat(result)
			.isNotNull()
			.hasSize(1)
			.satisfies(r ->
				assertThat(r.get(0).scenarioId()).isEqualTo(1L))
			.satisfies(r ->
				assertThat(r.get(0).scenarioName()).isEqualTo("Morning"))
			.satisfies(r ->
				assertThat(r.get(0).memo()).isEqualTo("Routine"));
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
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeekOrdinal(List.of(1, 2))
			.build();

		TimeNotificationRequest condition = TimeNotificationRequest.builder()
			.startHour(7)
			.startMinute(0)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("Evening")
			.memo("Routine")
			.basicMissions(List.of())
			.notification(notifRequest)
			.notificationCondition(condition)
			.build();

		Notification savedNotification = Notification.builder()
			.id(11L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.build();


		given(notificationService.addNotification(notifRequest, condition))
			.willReturn(savedNotification);
		given(scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(List.of(10_000_000));
		given(orderCalculator.getOrder(anyInt(), isNull()))
			.willThrow(new ReorderRequiredException(10_000_000))
			.willReturn(reorderedOrder);

		Scenario s1 = Scenario.builder().id(1L).scenarioOrder(10_000_000).build();
		given(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(List.of(s1));
		given(orderCalculator.getMinOrderAfterReorder(List.of(s1)))
			.willReturn(reorderedOrder);

		ArgumentCaptor<Scenario> captor = ArgumentCaptor.forClass(Scenario.class);

		given(missionService.addBasicMission(any(Scenario.class), eq(List.of())))
			.willReturn(List.of());

		Scenario createdScenario = Scenario.builder()
			.id(1L)
			.scenarioName("Morning")
			.memo("Routine")
			.scenarioOrder(reorderedOrder)
			.notification(savedNotification)
			.member(member)
			.build();
		given(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(List.of(createdScenario));

		// when
		List<ScenarioResponse> result = scenarioService.addScenario(memberId, scenarioRequest);

		// then
		verify(scenarioRepository).save(captor.capture());
		verify(notificationEventPublisher).publishScenarioCreateEvent(eq(memberId), any(Scenario.class),
			eq(NotificationType.TIME));

		Scenario saved = captor.getValue();
		assertThat(result)
			.isNotNull()
			.hasSize(1)
			.satisfies(r -> assertThat(r.get(0).scenarioId()).isEqualTo(1L))
			.satisfies(r -> assertThat(r.get(0).scenarioName()).isEqualTo("Morning"))
			.satisfies(r -> assertThat(r.get(0).memo()).isEqualTo("Routine"));
	}

	@Test
	void Given_ValidRequest_When_UpdateScenario_Then_UpdateScenarioAndNotification() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;

		Member member = Member.builder().id(memberId).build();
		Notification oldNotification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		Scenario oldScenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.scenarioName("기존 시나리오")
			.memo("기존 메모")
			.notification(oldNotification)
			.missions(new java.util.ArrayList<>())
			.build();

		NotificationRequest notifRequest = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeekOrdinal(List.of(1, 2))
			.build();

		TimeNotificationRequest condition = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("수정된 시나리오")
			.memo("수정된 메모")
			.basicMissions(List.of())
			.notification(notifRequest)
			.notificationCondition(condition)
			.build();

		Mockito.when(scenarioRepository.findScenarioDetailFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.of(oldScenario));
		Mockito.doAnswer(invocation -> {
			Notification target = invocation.getArgument(0);
			target.activate(notifRequest.notificationType(),
				notifRequest.notificationMethodType(),
				notifRequest.daysOfWeekOrdinal());
			return null;
		}).when(notificationService).updateNotification(oldNotification, notifRequest, condition);

		Scenario updatedScenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("수정할 시나리오")
			.memo("수정할 메모")
			.notification(oldNotification)
			.member(member)
			.build();
		given(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(List.of(updatedScenario));

		// when
		List<ScenarioResponse> result = scenarioService.updateScenario(memberId, scenarioId, scenarioRequest);

		// then
		assertThat(oldScenario)
			.satisfies(s -> assertThat(s.getScenarioName()).isEqualTo("수정된 시나리오"))
			.satisfies(s -> assertThat(s.getMemo()).isEqualTo("수정된 메모"))
			.satisfies(
				s ->
					assertThat(s.getNotification().getNotificationType()).isEqualTo(notifRequest.notificationType()))
			.satisfies(s -> assertThat(s.getNotification().getNotificationMethodType())
				.isEqualTo(notifRequest.notificationMethodType()))
			.satisfies(s -> assertThat(s.getNotification().isActive()).isTrue());
		verify(notificationService).updateNotification(oldNotification, notifRequest, condition);
		verify(missionService).updateBasicMission(oldScenario, List.of());
		verify(notificationEventPublisher).publishScenarioUpdateEvent(eq(memberId), eq(oldScenario), eq(true),
			eq(NotificationType.TIME));

		assertThat(result)
			.isNotNull()
			.hasSize(1)
			.satisfies(r -> assertThat(r.get(0).scenarioId()).isEqualTo(scenarioId))
			.satisfies(r -> assertThat(r.get(0).scenarioName()).isEqualTo("수정할 시나리오"))
			.satisfies(r -> assertThat(r.get(0).memo()).isEqualTo("수정할 메모"));
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
			.isActive(true)
			.notificationType(NotificationType.TIME)
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
		OrderUpdateResponse response = scenarioService.updateScenarioOrder(memberId, scenarioId, orderRequest);

		// then
		assertThat(scenario.getScenarioOrder()).isEqualTo(newOrder);
		assertThat(response)
			.satisfies(r -> assertThat(r.isReorder()).isFalse())
			.satisfies(r -> assertThat(r.orderUpdates()).hasSize(1))
			.satisfies(r -> assertThat(r.orderUpdates().get(0).id()).isEqualTo(scenarioId))
			.satisfies(r -> assertThat(r.orderUpdates().get(0).newOrder()).isEqualTo(newOrder));
		verify(orderCalculator).getOrder(1000, 2000);
	}


	@Test
	void Given_ReorderRequired_When_UpdateScenarioOrder_Then_ReorderScenarios() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		int errorOrder = 1500;

		Member member = Member.builder().id(memberId).build();
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.notification(notification)
			.scenarioOrder(1000)
			.build();

		ScenarioOrderUpdateRequest orderRequest = ScenarioOrderUpdateRequest.builder()
			.prevOrder(1000)
			.nextOrder(1001)
			.build();

		Scenario scenario1 = Scenario.builder().id(1L).scenarioOrder(1000).build();
		Scenario scenario2 = Scenario.builder().id(2L).scenarioOrder(2000).build();
		List<Scenario> reorderedScenarios = List.of(scenario1, scenario2);

		Mockito.when(scenarioRepository.findByIdAndMemberId(scenarioId, memberId))
			.thenReturn(Optional.of(scenario));
		Mockito.when(orderCalculator.getOrder(1000, 1001))
			.thenThrow(new ReorderRequiredException(errorOrder));
		Mockito.when(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.thenReturn(List.of(scenario1, scenario2));
		Mockito.when(orderCalculator.reorder(anyList(), eq(scenarioId), eq(errorOrder)))
			.thenReturn(reorderedScenarios);

		// when
		OrderUpdateResponse response = scenarioService.updateScenarioOrder(memberId, scenarioId, orderRequest);

		// then
		assertThat(response)
			.satisfies(r -> assertThat(r.isReorder()).isTrue())
			.satisfies(r -> assertThat(r.orderUpdates()).hasSize(2));
		verify(orderCalculator).reorder(anyList(), eq(scenarioId), eq(errorOrder));
	}

	@Test
	void Given_ValidRequest_When_AddScenarioWithoutNotification_Then_CreateInactiveNotificationAndSave() {
		// given
		Long memberId = 1L;

		NotificationRequest notificationRequest = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("시나리오")
			.memo("메모")
			.basicMissions(List.of())
			.notification(notificationRequest)
			.notificationCondition(null)
			.build();

		given(scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(List.of());
		Notification saved =
			Notification.builder().id(1L).isActive(true).notificationType(NotificationType.TIME).build();
		given(notificationService.addNotification(notificationRequest, null)).willReturn(saved);

		// when
		scenarioService.addScenario(memberId, request);

		// then
		verify(notificationService).addNotification(notificationRequest, null);
		verify(scenarioRepository).save(any(Scenario.class));
		verify(missionService).addBasicMission(any(Scenario.class), eq(List.of()));
		verify(notificationEventPublisher).publishScenarioCreateEvent(eq(memberId), any(Scenario.class),
			eq(NotificationType.TIME));
	}


	@Test
	void Given_ValidRequest_When_DeleteScenarioWithAllMissions_Then_DeleteScenarioAndNotification() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;

		Member member = Member.builder().id(memberId).build();
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.notification(notification)
			.build();

		Mockito.when(scenarioRepository.findNotificationFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.of(scenario));

		// when
		scenarioService.deleteScenarioWithAllMissions(memberId, scenarioId);

		// then
		verify(notificationService).deleteNotification(notification);
		verify(scenarioRepository).delete(scenario);
		verify(notificationEventPublisher).publishScenarioDeleteEvent(eq(memberId), eq(scenarioId), eq(true),
			eq(NotificationType.TIME));
	}


	@Test
	void Given_NotExistScenario_When_UpdateScenario_Then_ThrowNotFoundException() {
		// given
		Long memberId = 1L;
		Long scenarioId = 99L;

		NotificationRequest notification = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("수정할 시나리오")
			.memo("수정할 메모")
			.basicMissions(List.of())
			.notification(notification)
			.notificationCondition(null)
			.build();

		Mockito.when(scenarioRepository.findScenarioDetailFetchByIdAndMemberId(memberId, scenarioId))
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

		Mockito.when(scenarioRepository.findNotificationFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> scenarioService.deleteScenarioWithAllMissions(memberId, scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}


	@Test
	void Given_ValidRequest_When_UpdateScenarioWithoutNotification_Then_UpdateScenarioAndNotification() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;

		Member member = Member.builder().id(memberId).build();
		Notification oldNotification = Notification.builder()
			.id(1L)
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		Scenario oldScenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.scenarioName("기존 시나리오")
			.memo("기존 메모")
			.notification(oldNotification)
			.missions(new java.util.ArrayList<>())
			.build();

		NotificationRequest notificationRequest = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("수정된 시나리오")
			.memo("수정된 메모")
			.basicMissions(List.of())
			.notification(notificationRequest)
			.notificationCondition(null)
			.build();

		Mockito.when(scenarioRepository.findScenarioDetailFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.of(oldScenario));

		Scenario updatedScenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("수정할 시나리오")
			.memo("수정할 메모")
			.notification(oldNotification)
			.member(member)
			.build();
		given(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(List.of(updatedScenario));

		// when
		List<ScenarioResponse> result = scenarioService.updateScenario(memberId, scenarioId, scenarioRequest);

		// then
		assertThat(oldScenario)
			.satisfies(s -> assertThat(s.getScenarioName()).isEqualTo("수정된 시나리오"))
			.satisfies(s -> assertThat(s.getMemo()).isEqualTo("수정된 메모"));
		verify(notificationService).updateNotification(
			oldNotification, notificationRequest, null);
		verify(missionService).updateBasicMission(oldScenario, List.of());
		verify(notificationEventPublisher).publishScenarioUpdateEvent(eq(memberId), eq(oldScenario), eq(false),
			eq(NotificationType.TIME));

		assertThat(result)
			.isNotNull()
			.hasSize(1)
			.satisfies(r ->
				assertThat(r.get(0).scenarioId()).isEqualTo(scenarioId))
			.satisfies(r ->
				assertThat(r.get(0).scenarioName()).isEqualTo("수정할 시나리오"))
			.satisfies(r ->
				assertThat(r.get(0).memo()).isEqualTo("수정할 메모"));
	}


	@Test
	void Given_NotExistScenario_When_UpdateScenarioWithoutNotification_Then_ThrowNotFoundException() {
		// given
		Long memberId = 1L;
		Long scenarioId = 99L;

		NotificationRequest notificationRequest = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("수정할 시나리오")
			.memo("수정할 메모")
			.basicMissions(List.of())
			.notification(notificationRequest)
			.notificationCondition(null)
			.build();

		Mockito.when(scenarioRepository.findScenarioDetailFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() ->
			scenarioService.updateScenario(memberId, scenarioId, scenarioRequest))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}


	@Test
	void Given_MaxScenarioCountExceeded_When_AddScenario_Then_ThrowMaxCountExceededException() {
		// given
		Long memberId = 1L;

		NotificationRequest notifRequest = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeekOrdinal(List.of(1, 2))
			.build();

		TimeNotificationRequest condition = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("New Scenario")
			.memo("New Memo")
			.basicMissions(List.of())
			.notification(notifRequest)
			.notificationCondition(condition)
			.build();

		// 20개의 시나리오가 이미 존재 (최대 개수)
		List<Integer> orderList = new java.util.ArrayList<>();
		for (int i = 0; i < 20; i++) {
			orderList.add(1000 + i * 1000);
		}

		given(scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(orderList);
		doThrow(new ServerException(ScenarioErrorResult.MAX_SCENARIO_COUNT_EXCEEDED))
			.when(scenarioValidator).validateMaxScenarioCount(orderList);

		// when & then
		assertThatThrownBy(() -> scenarioService.addScenario(memberId, scenarioRequest))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.MAX_SCENARIO_COUNT_EXCEEDED.getMessage());
	}


	@Test
	void Given_MaxScenarioCountExceeded_When_AddScenarioWithoutNotification_Then_ThrowMaxCountExceededException() {
		// given
		Long memberId = 1L;

		NotificationRequest notificationRequest = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("시나리오")
			.memo("메모")
			.basicMissions(List.of())
			.notification(notificationRequest)
			.notificationCondition(null)
			.build();

		// 20개의 시나리오가 이미 존재 (최대 개수)
		List<Integer> orderList = new java.util.ArrayList<>();
		for (int i = 0; i < 20; i++) {
			orderList.add(1000 + i * 1000);
		}

		given(scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(orderList);
		doThrow(new ServerException(ScenarioErrorResult.MAX_SCENARIO_COUNT_EXCEEDED))
			.when(scenarioValidator).validateMaxScenarioCount(orderList);

		// when & then
		assertThatThrownBy(() -> scenarioService.addScenario(memberId, request))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.MAX_SCENARIO_COUNT_EXCEEDED.getMessage());
	}


	@Test
	void Given_notificationInfoIsNull_When_findScenarioByScenarioId_Then_returnResponseWithNullCondition() {
		// given
		final Long memberId = 1L;
		final Long scenarioId = 10L;

		final Member member = Member.builder()
			.id(memberId)
			.build();

		final Notification notification = Notification.builder()
			.id(100L)
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.build();

		final Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.scenarioName("알림 없는 루틴")
			.memo("메모")
			.scenarioOrder(1)
			.notification(notification)
			.missions(List.of())
			.build();

		Mockito.when(scenarioRepository.findScenarioDetailFetchByIdAndMemberId(memberId, scenarioId))
			.thenReturn(Optional.of(scenario));
		Mockito.when(notificationService.findNotificationDetails(notification)).thenReturn(null);
		Mockito.when(missionTypeGroupSorter.groupAndSortByType(scenario.getMissions(), MissionType.BASIC))
			.thenReturn(List.of());

		// when
		ScenarioDetailResponse response = scenarioService.findScenarioDetailByScenarioId(memberId, scenarioId);

		// then
		assertNotNull(response);
		assertThat(response)
			.satisfies(r -> assertThat(r.scenarioId()).isEqualTo(scenarioId))
			.satisfies(r -> assertThat(r.notification().isEveryDay()).isNull())
			.satisfies(r -> assertThat(r.notification().daysOfWeekOrdinal()).isNull())
			.satisfies(r -> assertThat(r.notificationCondition()).isNull());
	}


	@Test
	void Given_EmptyOrderList_When_AddScenario_Then_CreateScenarioWithStartOrder() {
		// given
		Long memberId = 1L;

		Member member = Member.builder().id(memberId).build();
		given(em.getReference(Member.class, memberId)).willReturn(member);

		NotificationRequest notifRequest = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.daysOfWeekOrdinal(List.of(1, 2))
			.build();

		TimeNotificationRequest condition = TimeNotificationRequest.builder()
			.startHour(9)
			.startMinute(0)
			.build();

		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("First Scenario")
			.memo("First Memo")
			.basicMissions(List.of())
			.notification(notifRequest)
			.notificationCondition(condition)
			.build();

		Notification savedNotification = Notification.builder()
			.id(10L)
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.ALARM)
			.build();

		given(notificationService.addNotification(notifRequest, condition)).willReturn(savedNotification);
		// 빈 리스트 반환 - 첫 번째 시나리오
		given(scenarioRepository.findOrdersByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.willReturn(List.of());

		ArgumentCaptor<Scenario> scenarioCaptor = ArgumentCaptor.forClass(Scenario.class);

		// when
		scenarioService.addScenario(memberId, scenarioRequest);

		// then
		verify(scenarioRepository).save(scenarioCaptor.capture());
		verify(notificationEventPublisher).publishScenarioCreateEvent(eq(memberId), any(Scenario.class),
			eq(NotificationType.TIME));

		Scenario saved = scenarioCaptor.getValue();
		assertThat(saved.getScenarioOrder()).isEqualTo(OrderCalculator.START_ORDER);
	}

	@Test
	void Given_EmptyScenarioList_When_FindScenariosByMemberId_Then_ReturnEmptyList() {
		// given
		final Long memberId = 1L;

		Mockito
			.when(scenarioRepository.findByMemberIdAndNotificationType(memberId, NotificationType.TIME))
			.thenReturn(List.of());

		// when
		ScenarioResponseListWrapper result = scenarioService.findScenariosByMemberId(memberId, NotificationType.TIME);

		// then
		assertNotNull(result);
		assertThat(result.scenarioResponses()).isEmpty();
	}

}
