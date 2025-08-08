package com.und.server.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.auth.filter.AuthMemberArgumentResolver;
import com.und.server.common.exception.CommonErrorResult;
import com.und.server.common.exception.GlobalExceptionHandler;
import com.und.server.common.exception.ServerException;
import com.und.server.member.dto.MemberResponse;
import com.und.server.member.dto.NicknameRequest;
import com.und.server.member.service.MemberService;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

	@InjectMocks
	private MemberController memberController;

	@Mock
	private MemberService memberService;

	@Mock
	private AuthMemberArgumentResolver authMemberArgumentResolver;

	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(memberController)
			.setCustomArgumentResolvers(authMemberArgumentResolver)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	@DisplayName("Fails to update nickname with bad request when nickname is null")
	void Given_NullNickname_When_UpdateNickname_Then_ReturnsBadRequest() throws Exception {
		// given
		final String url = "/v1/member/nickname";
		final NicknameRequest request = new NicknameRequest(null);
		final String requestBody = objectMapper.writeValueAsString(request);
		final Long memberId = 1L;

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.patch(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(CommonErrorResult.INVALID_PARAMETER.name()))
			.andExpect(jsonPath("$.message[0]").value("Nickname must not be blank"));
	}

	@Test
	@DisplayName("Fails to update nickname and returns unauthorized when user is not authenticated")
	void Given_UnauthenticatedUser_When_UpdateNickname_Then_ReturnsUnauthorized() throws Exception {
		// given
		final String url = "/v1/member/nickname";
		final NicknameRequest request = new NicknameRequest("new-nickname");
		final String requestBody = objectMapper.writeValueAsString(request);
		final AuthErrorResult errorResult = AuthErrorResult.UNAUTHORIZED_ACCESS;

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doThrow(new ServerException(errorResult))
			.when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.patch(url)
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(errorResult.name()))
			.andExpect(jsonPath("$.message").value(errorResult.getMessage()));
	}

	@Test
	@DisplayName("Succeeds in updating nickname for an authenticated user")
	void Given_AuthenticatedUser_When_UpdateNickname_Then_ReturnsOkWithUpdatedInfo() throws Exception {
		// given
		final String url = "/v1/member/nickname";
		final Long memberId = 1L;
		final String newNickname = "new-nickname";
		final NicknameRequest request = new NicknameRequest(newNickname);
		final MemberResponse response = new MemberResponse(memberId, newNickname, null, null, null, null);

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());
		doReturn(response).when(memberService).updateNickname(memberId, request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.patch(url)
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON)
		);

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(memberId))
			.andExpect(jsonPath("$.nickname").value(newNickname));
	}

	@Test
	@DisplayName("Fails to delete member and returns unauthorized when user is not authenticated")
	void Given_UnauthenticatedUser_When_DeleteMember_Then_ReturnsUnauthorized() throws Exception {
		// given
		final String url = "/v1/member";
		final AuthErrorResult errorResult = AuthErrorResult.UNAUTHORIZED_ACCESS;

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doThrow(new ServerException(errorResult))
			.when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.delete(url)
		);

		// then
		resultActions.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(errorResult.name()))
			.andExpect(jsonPath("$.message").value(errorResult.getMessage()));
	}

	@Test
	@DisplayName("Succeeds in deleting member for an authenticated user")
	void Given_AuthenticatedUser_When_DeleteMember_Then_ReturnsNoContent() throws Exception {
		// given
		final String url = "/v1/member";
		final Long memberId = 1L;

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.delete(url)
		);

		// then
		resultActions.andExpect(status().isNoContent());
		verify(memberService).deleteMemberById(memberId);
	}

}
