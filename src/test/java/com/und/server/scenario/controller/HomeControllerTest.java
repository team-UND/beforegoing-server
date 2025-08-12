package com.und.server.scenario.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.dto.response.HomeResponse;
import com.und.server.scenario.dto.response.HomeScenarioResponse;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.service.MissionService;
import com.und.server.scenario.service.ScenarioService;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

	@Mock
	private ScenarioService scenarioService;

	@Mock
	private MissionService missionService;

	@InjectMocks
	private HomeController homeController;


	@Test
	void Given_ValidMemberIdAndNotifType_When_GetHomeData_Then_ReturnHomeResponse() {
		// given
		Long memberId = 1L;
		NotificationType notifType = NotificationType.TIME;

		ScenarioResponse scenario1 = ScenarioResponse.builder()
			.scenarioId(1L)
			.scenarioName("시나리오 1")
			.build();

		ScenarioResponse scenario2 = ScenarioResponse.builder()
			.scenarioId(2L)
			.scenarioName("시나리오 2")
			.build();

		List<ScenarioResponse> scenarios = Arrays.asList(scenario1, scenario2);

		MissionGroupResponse missions = new MissionGroupResponse(
			List.of(), List.of()
		);

		when(scenarioService.findHomeScenariosByMemberId(memberId, notifType))
			.thenReturn(List.of(
				HomeScenarioResponse.builder().scenarioId(1L).scenarioName("시나리오 1").build(),
				HomeScenarioResponse.builder().scenarioId(2L).scenarioName("시나리오 2").build()
			));
		when(missionService.findMissionsByScenarioId(memberId, 1L, LocalDate.now()))
			.thenReturn(missions);

		// when
		ResponseEntity<HomeResponse> response = homeController.getHomeData(memberId, notifType);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().scenarioList()).hasSize(2);
		assertThat(response.getBody().missionListByType()).isEqualTo(missions);
		verify(scenarioService).findHomeScenariosByMemberId(memberId, notifType);
		verify(missionService).findMissionsByScenarioId(memberId, 1L, LocalDate.now());
	}


	@Test
	void Given_ValidMemberIdAndDefaultNotifType_When_GetHomeData_Then_ReturnHomeResponse() {
		// given
		Long memberId = 1L;
		NotificationType notifType = NotificationType.TIME; // 기본값

		ScenarioResponse scenario = ScenarioResponse.builder()
			.scenarioId(1L)
			.scenarioName("시나리오")
			.build();

		List<ScenarioResponse> scenarios = Arrays.asList(scenario);

		MissionGroupResponse missions = new MissionGroupResponse(
			List.of(), List.of()
		);

		when(scenarioService.findHomeScenariosByMemberId(memberId, notifType))
			.thenReturn(List.of(
				HomeScenarioResponse.builder().scenarioId(1L).scenarioName("시나리오").build()
			));
		when(missionService.findMissionsByScenarioId(memberId, 1L, LocalDate.now()))
			.thenReturn(missions);

		// when
		ResponseEntity<HomeResponse> response = homeController.getHomeData(memberId, notifType);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().scenarioList()).hasSize(1);
		assertThat(response.getBody().missionListByType()).isEqualTo(missions);
		verify(scenarioService).findHomeScenariosByMemberId(memberId, notifType);
		verify(missionService).findMissionsByScenarioId(memberId, 1L, LocalDate.now());
	}


	@Test
	void Given_ValidMemberIdAndLocationNotifType_When_GetHomeData_Then_ReturnHomeResponse() {
		// given
		Long memberId = 1L;
		NotificationType notifType = NotificationType.LOCATION;

		ScenarioResponse scenario = ScenarioResponse.builder()
			.scenarioId(1L)
			.scenarioName("위치 시나리오")
			.build();

		List<ScenarioResponse> scenarios = Arrays.asList(scenario);

		MissionGroupResponse missions = new MissionGroupResponse(
			List.of(), List.of()
		);

		when(scenarioService.findHomeScenariosByMemberId(memberId, notifType))
			.thenReturn(List.of(
				HomeScenarioResponse.builder().scenarioId(1L).scenarioName("위치 시나리오").build()
			));
		when(missionService.findMissionsByScenarioId(memberId, 1L, LocalDate.now()))
			.thenReturn(missions);

		// when
		ResponseEntity<HomeResponse> response = homeController.getHomeData(memberId, notifType);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().scenarioList()).hasSize(1);
		assertThat(response.getBody().missionListByType()).isEqualTo(missions);
		verify(scenarioService).findHomeScenariosByMemberId(memberId, notifType);
		verify(missionService).findMissionsByScenarioId(memberId, 1L, LocalDate.now());
	}


	@Test
	void Given_EmptyScenarios_When_GetHomeData_Then_ReturnHomeResponseWithNullMissions() {
		// given
		Long memberId = 1L;
		NotificationType notifType = NotificationType.TIME;

		when(scenarioService.findHomeScenariosByMemberId(memberId, notifType))
			.thenReturn(List.of());

		// when
		ResponseEntity<HomeResponse> response = homeController.getHomeData(memberId, notifType);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().scenarioList()).isEmpty();
		assertThat(response.getBody().missionListByType()).isNull();
		verify(scenarioService).findHomeScenariosByMemberId(memberId, notifType);
		// missionService는 호출되지 않아야 함
	}


	@Test
	void Given_MultipleScenarios_When_GetHomeData_Then_UseFirstScenarioForMissions() {
		// given
		Long memberId = 1L;
		NotificationType notifType = NotificationType.TIME;

		ScenarioResponse scenario1 = ScenarioResponse.builder()
			.scenarioId(1L)
			.scenarioName("첫 번째 시나리오")
			.build();

		ScenarioResponse scenario2 = ScenarioResponse.builder()
			.scenarioId(2L)
			.scenarioName("두 번째 시나리오")
			.build();

		List<ScenarioResponse> scenarios = Arrays.asList(scenario1, scenario2);

		MissionGroupResponse missions = new MissionGroupResponse(
			List.of(), List.of()
		);

		when(scenarioService.findHomeScenariosByMemberId(memberId, notifType))
			.thenReturn(List.of(
				HomeScenarioResponse.builder().scenarioId(1L).scenarioName("첫 번째 시나리오").build(),
				HomeScenarioResponse.builder().scenarioId(2L).scenarioName("두 번째 시나리오").build()
			));
		when(missionService.findMissionsByScenarioId(memberId, 1L, LocalDate.now()))
			.thenReturn(missions);

		// when
		ResponseEntity<HomeResponse> response = homeController.getHomeData(memberId, notifType);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().scenarioList()).hasSize(2);
		assertThat(response.getBody().missionListByType()).isEqualTo(missions);
		verify(scenarioService).findHomeScenariosByMemberId(memberId, notifType);
		verify(missionService).findMissionsByScenarioId(memberId, 1L, LocalDate.now());
	}

}

