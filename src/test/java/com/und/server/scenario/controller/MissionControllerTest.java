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

import com.und.server.auth.filter.AuthMemberArgumentResolver;
import com.und.server.common.exception.GlobalExceptionHandler;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.service.MissionService;

@ExtendWith(MockitoExtension.class)
class MissionControllerTest {

	@InjectMocks
	private MissionController missionController;

	@Mock
	private AuthMemberArgumentResolver authMemberArgumentResolver;

	@Mock
	private MissionService missionService;

	private MockMvc mockMvc;


	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(missionController)
			.setCustomArgumentResolvers(authMemberArgumentResolver)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}


	@Test
	void Given_MemberIdAndScenarioId_When_GetMissionsByScenarioId_Then_ReturnMissionResponseList() throws Exception {
		// given
		final Long memberId = 1L;
		final Long scenarioId = 10L;
		final String url = "/v1/scenarios/" + scenarioId + "/missions";

		List<MissionResponse> response = List.of(
			MissionResponse.builder()
				.missionId(101L)
				.content("기상")
				.isChecked(false)
				.order(1)
				.missionType(MissionType.BASIC)
				.build(),
			MissionResponse.builder()
				.missionId(102L)
				.content("양치")
				.isChecked(true)
				.order(2)
				.missionType(MissionType.TODAY)
				.build()
		);

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());
		doReturn(response).when(missionService).findMissionsByScenarioId(memberId, scenarioId);

		// when
		ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
				.accept(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].missionId").value(101))
			.andExpect(jsonPath("$[0].content").value("기상"))
			.andExpect(jsonPath("$[1].missionId").value(102))
			.andExpect(jsonPath("$[1].isChecked").value(true));
	}

}
