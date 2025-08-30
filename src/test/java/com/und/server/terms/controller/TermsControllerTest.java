package com.und.server.terms.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.und.server.common.exception.ServerException;
import com.und.server.terms.dto.request.EventPushAgreementRequest;
import com.und.server.terms.dto.request.TermsAgreementRequest;
import com.und.server.terms.dto.response.TermsAgreementResponse;
import com.und.server.terms.exception.TermsErrorResult;
import com.und.server.terms.service.TermsService;

@ExtendWith(MockitoExtension.class)
class TermsControllerTest {

	@InjectMocks
	private TermsController termsController;

	@Mock
	private TermsService termsService;

	@Mock
	private AuthMemberArgumentResolver authMemberArgumentResolver;

	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Long memberId = 1L;

	@BeforeEach
	void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(termsController)
			.setCustomArgumentResolvers(authMemberArgumentResolver)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	@DisplayName("Fails to get terms agreement and returns not found when no agreement exists")
	void Given_NoAgreement_When_GetTermsAgreement_Then_ReturnsNotFound() throws Exception {
		// given
		final String url = "/v1/terms";
		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());
		doThrow(new ServerException(TermsErrorResult.TERMS_NOT_FOUND)).when(termsService).getTermsAgreement(memberId);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
		);

		// then
		resultActions.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Successfully gets terms agreement")
	void Given_ExistingAgreement_When_GetTermsAgreement_Then_ReturnsOkWithResponse() throws Exception {
		// given
		final String url = "/v1/terms";
		final TermsAgreementResponse response = new TermsAgreementResponse(1L, memberId, true, true, true, true);
		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());
		doReturn(response).when(termsService).getTermsAgreement(memberId);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
		);

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.memberId").value(memberId))
			.andExpect(jsonPath("$.eventPushAgreed").value(true));
	}

	@Test
	@DisplayName("Fails to add terms agreement when it already exists")
	void Given_ExistingAgreement_When_AddTermsAgreement_Then_ReturnsConflict() throws Exception {
		// given
		final String url = "/v1/terms";
		final TermsAgreementRequest request = new TermsAgreementRequest(true, true, true, false);
		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());
		doThrow(new ServerException(TermsErrorResult.TERMS_ALREADY_EXISTS))
			.when(termsService).addTermsAgreement(memberId, request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("Successfully adds a new terms agreement")
	void Given_NoAgreement_When_AddTermsAgreement_Then_ReturnsCreatedWithResponse() throws Exception {
		// given
		final String url = "/v1/terms";
		final TermsAgreementRequest request = new TermsAgreementRequest(true, true, true, true);
		final TermsAgreementResponse response = new TermsAgreementResponse(1L, memberId, true, true, true, true);
		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());
		doReturn(response).when(termsService).addTermsAgreement(memberId, request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.eventPushAgreed").value(true));
	}

	@Test
	@DisplayName("Successfully updates event push agreement")
	void Given_ExistingAgreement_When_UpdateEventPushAgreement_Then_ReturnsOkWithResponse() throws Exception {
		// given
		final String url = "/v1/terms";
		final EventPushAgreementRequest request = new EventPushAgreementRequest(false);
		final TermsAgreementResponse response = new TermsAgreementResponse(1L, memberId, true, true, true, false);
		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());
		doReturn(response).when(termsService).updateEventPushAgreement(memberId, request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.patch(url)
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L))
			.andExpect(jsonPath("$.eventPushAgreed").value(false));
	}

}
