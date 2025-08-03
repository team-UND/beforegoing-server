package com.und.server.scenario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.auth.filter.AuthMemberArgumentResolver;
import com.und.server.common.exception.GlobalExceptionHandler;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NofitDayOfWeekResponse;
import com.und.server.notification.dto.TimeNotifResponse;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.service.ScenarioService;

@ExtendWith(MockitoExtension.class)
class ScenarioControllerTest {

	@InjectMocks
	private ScenarioController scenarioController;

	@Mock
	private AuthMemberArgumentResolver authMemberArgumentResolver;

	@Mock
	private ScenarioService scenarioService;

	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();


	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(scenarioController)
			.setCustomArgumentResolvers(authMemberArgumentResolver)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}


	@Test
	void Given_MemberId_When_GetScenario_Than_ReturnScenario() throws Exception {
		// given
		final String url = "/v1/scenarios";
		final Long memberId = 1L;
		final List<ScenarioResponse> response = List.of(
			ScenarioResponse.builder().scenarioId(1L).scenarioName("시나리오1").memo("메모1").order(1).build(),
			ScenarioResponse.builder().scenarioId(2L).scenarioName("시나리오2").memo("메모2").order(2).build()
		);

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());
		doReturn(response).when(scenarioService).findScenariosByMemberId(memberId);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
				.accept(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].scenarioId").value(1))
			.andExpect(jsonPath("$[0].scenarioName").value("시나리오1"));
	}


	@Test
	void Given_MemberIdAndScenarioId_When_GetScenarioDetail_Then_ReturnScenarioDetailResponse() throws Exception {
		// given
		final Long memberId = 1L;
		final Long scenarioId = 10L;
		final String url = "/v1/scenarios/" + scenarioId;

		final TimeNotifResponse notifDetail = TimeNotifResponse.builder()
			.hour(9)
			.minute(30)
			.build();

		final ScenarioDetailResponse response = ScenarioDetailResponse.builder()
			.scenarioId(scenarioId)
			.scenarioName("기상 루틴")
			.memo("간단한 메모")
			.notificationId(100L)
			.isActive(true)
			.notificationType(NotifType.TIME)
			.isEveryDay(true)
			.dayOfWeekOrdinalList(List.of(new NofitDayOfWeekResponse(1L, 1)))
			.notificationDetail(notifDetail)
			.missionList(List.of())
			.build();

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());
		doReturn(response).when(scenarioService).findScenarioByScenarioId(memberId, scenarioId);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
				.accept(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.scenarioId").value(scenarioId))
			.andExpect(jsonPath("$.scenarioName").value("기상 루틴"))
			.andExpect(jsonPath("$.notificationDetail.hour").value(9))
			.andExpect(jsonPath("$.notificationDetail.minute").value(30));
	}

}
