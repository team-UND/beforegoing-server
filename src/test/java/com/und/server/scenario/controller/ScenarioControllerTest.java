package com.und.server.scenario.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.service.ScenarioService;

@ExtendWith(MockitoExtension.class)
class ScenarioControllerTest {

	@Mock
	private ScenarioService scenarioService;

	@InjectMocks
	private ScenarioController scenarioController;


	@Test
	void Given_ValidMemberIdAndNotifType_When_GetScenarios_Then_ReturnScenarioList() {
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

		List<ScenarioResponse> expectedScenarios = Arrays.asList(scenario1, scenario2);

		when(scenarioService.findScenariosByMemberId(memberId, notifType))
			.thenReturn(expectedScenarios);

		// when
		ResponseEntity<List<ScenarioResponse>> response = scenarioController.getScenarios(memberId, notifType);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expectedScenarios);
		verify(scenarioService).findScenariosByMemberId(memberId, notifType);
	}


	@Test
	void Given_ValidMemberIdAndScenarioId_When_GetScenarioDetail_Then_ReturnScenarioDetail() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;

		ScenarioDetailResponse expectedDetail = ScenarioDetailResponse.builder()
			.scenarioId(scenarioId)
			.scenarioName("시나리오 상세")
			.memo("시나리오 설명")
			.build();

		when(scenarioService.findScenarioDetailByScenarioId(memberId, scenarioId))
			.thenReturn(expectedDetail);

		// when
		ResponseEntity<ScenarioDetailResponse> response = scenarioController.getScenarioDetail(memberId, scenarioId);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expectedDetail);
		verify(scenarioService).findScenarioDetailByScenarioId(memberId, scenarioId);
	}


	@Test
	void Given_ValidMemberIdAndScenarioRequest_When_AddScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("새 시나리오")
			.memo("새 시나리오 설명")
			.build();

		// when
		ResponseEntity<Void> response = scenarioController.addScenario(memberId, scenarioRequest);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).addScenario(memberId, scenarioRequest);
	}


	@Test
	void Given_EmptyTitleRequest_When_AddScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("")
			.memo("빈 제목 시나리오")
			.build();

		// when
		ResponseEntity<Void> response = scenarioController.addScenario(memberId, scenarioRequest);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).addScenario(memberId, scenarioRequest);
	}


	@Test
	void Given_ValidMemberIdAndScenarioIdAndRequest_When_UpdateScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("수정된 시나리오")
			.memo("수정된 시나리오 설명")
			.build();

		// when
		ResponseEntity<Void> response = scenarioController.updateScenario(memberId, scenarioId, scenarioRequest);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).updateScenario(memberId, scenarioId, scenarioRequest);
	}


	@Test
	void Given_EmptyTitleRequest_When_UpdateScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("")
			.memo("수정된 빈 제목 시나리오")
			.build();

		// when
		ResponseEntity<Void> response = scenarioController.updateScenario(memberId, scenarioId, scenarioRequest);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).updateScenario(memberId, scenarioId, scenarioRequest);
	}


	@Test
	void Given_LongTitleRequest_When_AddScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("매우 긴 시나리오 제목입니다")
			.memo("긴 제목 시나리오")
			.build();

		// when
		ResponseEntity<Void> response = scenarioController.addScenario(memberId, scenarioRequest);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).addScenario(memberId, scenarioRequest);
	}


	@Test
	void Given_LongTitleRequest_When_UpdateScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		ScenarioDetailRequest scenarioRequest = ScenarioDetailRequest.builder()
			.scenarioName("매우 긴 수정된 시나리오 제목입니다")
			.memo("긴 제목 수정 시나리오")
			.build();

		// when
		ResponseEntity<Void> response = scenarioController.updateScenario(memberId, scenarioId, scenarioRequest);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).updateScenario(memberId, scenarioId, scenarioRequest);
	}


	@Test
	void Given_ValidMemberIdAndScenarioIdAndOrderRequest_When_UpdateScenarioOrder_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		ScenarioOrderUpdateRequest orderRequest = ScenarioOrderUpdateRequest.builder()
			.prevOrder(1000)
			.nextOrder(2000)
			.build();

		// when
		ResponseEntity<Void> response = scenarioController.updateScenarioOrder(memberId, scenarioId, orderRequest);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).updateScenarioOrder(memberId, scenarioId, orderRequest);
	}


	@Test
	void Given_ValidMemberIdAndScenarioId_When_DeleteScenario_Then_ReturnNoContent() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;

		// when
		ResponseEntity<Void> response = scenarioController.deleteScenario(memberId, scenarioId);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(response.getBody()).isNull();
		verify(scenarioService).deleteScenarioWithAllMissions(memberId, scenarioId);
	}

}
