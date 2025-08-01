package com.und.server.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.und.server.auth.filter.AuthMemberArgumentResolver;
import com.und.server.common.exception.GlobalExceptionHandler;
import com.und.server.common.exception.ServerErrorResult;
import com.und.server.common.exception.ServerException;
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

	@BeforeEach
	void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(memberController)
			.setCustomArgumentResolvers(authMemberArgumentResolver)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	@DisplayName("Fails member deletion and returns unauthorized when user is not authenticated")
	void Given_UnauthenticatedUser_When_DeleteMember_Then_ReturnsUnauthorized() throws Exception {
		// given
		final String url = "/v1/member";
		final ServerErrorResult errorResult = ServerErrorResult.UNAUTHORIZED_ACCESS;

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

		verify(memberService, never()).deleteMemberById(any(Long.class));
	}

	@Test
	@DisplayName("Succeeds in deleting the member and returns no content")
	void Given_MemberId_When_DeleteMember_Then_ReturnsNoContent() throws Exception {
		// given
		final String url = "/v1/member";
		final Long memberId = 1L;

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());

		final Authentication auth = new UsernamePasswordAuthenticationToken(
			memberId,
			null,
			Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
		);

		// when & then
		mockMvc.perform(
			MockMvcRequestBuilders.delete(url).with(authentication(auth))
		).andExpect(status().isNoContent());

		verify(memberService).deleteMemberById(memberId);
	}

}
