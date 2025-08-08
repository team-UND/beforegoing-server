package com.und.server.scenario.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.service.MissionService;
import com.und.server.scenario.service.ScenarioService;

@ExtendWith(MockitoExtension.class)
class MissionControllerTest {

	@Mock
	private ScenarioService scenarioService;

	@Mock
	private MissionService missionService;

	@InjectMocks
	private MissionController missionController;


	@Test
	void Given_ValidMemberIdAndScenarioId_When_GetMissionsByScenarioId_Then_ReturnMissionGroupResponse() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.now();

		MissionGroupResponse expectedResponse = new MissionGroupResponse(
			List.of(), List.of()
		);

		when(missionService.findMissionsByScenarioId(memberId, scenarioId, date))
			.thenReturn(expectedResponse);

		// when
		ResponseEntity<MissionGroupResponse> response =
			missionController.getMissionsByScenarioId(memberId, scenarioId, date);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expectedResponse);
		verify(missionService).findMissionsByScenarioId(memberId, scenarioId, date);
	}


	@Test
	void Given_ValidRequest_When_AddTodayMissionToScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.now();
		TodayMissionRequest missionAddRequest = new TodayMissionRequest("오늘 미션");

		// when
		ResponseEntity<Void> response =
			missionController.addTodayMissionToScenario(memberId, scenarioId, missionAddRequest, date);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).addTodayMissionToScenario(memberId, scenarioId, missionAddRequest, date);
	}


	@Test
	void Given_EmptyContentRequest_When_AddTodayMissionToScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.now();
		TodayMissionRequest missionAddRequest = new TodayMissionRequest("");

		// when
		ResponseEntity<Void> response =
			missionController.addTodayMissionToScenario(memberId, scenarioId, missionAddRequest, date);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).addTodayMissionToScenario(memberId, scenarioId, missionAddRequest, date);
	}


	@Test
	void Given_LongContentRequest_When_AddTodayMissionToScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.now();
		TodayMissionRequest missionAddRequest = new TodayMissionRequest("매우 긴 미션 내용입니다");

		// when
		ResponseEntity<Void> response =
			missionController.addTodayMissionToScenario(memberId, scenarioId, missionAddRequest, date);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).addTodayMissionToScenario(memberId, scenarioId, missionAddRequest, date);
	}

}
