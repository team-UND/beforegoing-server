package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NofitDayOfWeekResponse;
import com.und.server.notification.dto.TimeNotifResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationService;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.NotificationInfoDto;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.ScenarioRepository;
import com.und.server.scenario.util.MissionTypeGrouper;

@ExtendWith(MockitoExtension.class)
class ScenarioServiceTest {

	@InjectMocks
	private ScenarioService scenarioService;

	@Mock
	private NotificationService notificationService;

	@Mock
	private ScenarioRepository scenarioRepository;

	@Mock
	private MissionTypeGrouper missionTypeGrouper;


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
			.when(scenarioRepository.findByMemberIdOrderByOrder(memberId))
			.thenReturn(scenarioList);

		List<ScenarioResponse> result = scenarioService.findScenariosByMemberId(memberId);

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
			.missionList(List.of()) // 비어 있어도 OK
			.build();

		final TimeNotifResponse notifDetail = TimeNotifResponse.builder()
			.hour(8)
			.minute(30)
			.build();

		final NotificationInfoDto notifInfoDto = new NotificationInfoDto(
			true,
			List.of(new NofitDayOfWeekResponse(1L, 1)),
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
		assertThat(response.getNotificationDetail()).isInstanceOf(TimeNotifResponse.class);
		TimeNotifResponse detail = (TimeNotifResponse) response.getNotificationDetail();
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

}
