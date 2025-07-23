package com.und.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.dto.AuthResponse;
import com.und.server.dto.TestAuthRequest;
import com.und.server.dto.TestHelloResponse;
import com.und.server.entity.Member;
import com.und.server.exception.GlobalExceptionHandler;
import com.und.server.oauth.Provider;
import com.und.server.repository.MemberRepository;
import com.und.server.service.AuthService;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

	@InjectMocks
	private TestController testController;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private AuthService authService;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(testController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();

		objectMapper = new ObjectMapper();
	}

	@Test
	void requireAccessTokenWhenMemberExists() throws Exception {
		// given
		final String url = "/v1/test/access";
		final TestAuthRequest request = new TestAuthRequest("kakao", "dummy.provider.id", "Chori");
		final Member existingMember = Member.builder().id(1L).kakaoId("dummy.provider.id").nickname("Chori").build();
		final AuthResponse expectedResponse = new AuthResponse("Bearer", "access-token", 3600, "refresh-token", 7200);

		doReturn(Provider.KAKAO).when(authService).convertToProvider(request.provider());
		doReturn(existingMember).when(authService).findMemberByProviderId(Provider.KAKAO, request.providerId());
		doReturn(expectedResponse).when(authService).issueTokens(existingMember.getId());

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		);

		// then
		resultActions.andExpect(status().isOk());

		final String responseBody = resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		final AuthResponse actualResponse = objectMapper.readValue(responseBody, AuthResponse.class);

		assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
		verify(authService).findMemberByProviderId(Provider.KAKAO, request.providerId());
		verify(authService, never()).createMember(Provider.KAKAO, request.providerId(), request.nickname());
		verify(authService).issueTokens(existingMember.getId());
	}

	@Test
	void requireAccessTokenWhenMemberDoesNotExist() throws Exception {
		// given
		final String url = "/v1/test/access";
		final TestAuthRequest request = new TestAuthRequest("kakao", "provider-id-456", "Newbie");
		final Member newMember = Member.builder().id(2L).kakaoId("provider-id-456").nickname("Newbie").build();
		final AuthResponse expectedResponse = new AuthResponse(
			"Bearer",
			"new-access-token",
			3600,
			"new-refresh-token",
			7200
		);

		doReturn(Provider.KAKAO).when(authService).convertToProvider(request.provider());
		doReturn(null).when(authService).findMemberByProviderId(Provider.KAKAO, request.providerId());
		doReturn(newMember).when(authService).createMember(Provider.KAKAO, request.providerId(), request.nickname());
		doReturn(expectedResponse).when(authService).issueTokens(newMember.getId());

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		);

		// then
		resultActions.andExpect(status().isOk());

		final String responseBody = resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		final AuthResponse actualResponse = objectMapper.readValue(responseBody, AuthResponse.class);

		assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
		verify(authService).findMemberByProviderId(Provider.KAKAO, request.providerId());
		verify(authService).createMember(Provider.KAKAO, request.providerId(), request.nickname());
		verify(authService).issueTokens(newMember.getId());
	}

	@Test
	void returnHelloWithNickname() throws Exception {
		// given
		final String url = "/v1/test/hello";
		final Long memberId = 1L;
		final Member member = Member.builder().id(memberId).nickname("Chori").build();

		doReturn(Optional.of(member)).when(memberRepository).findById(memberId);
		Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get(url).principal(auth)
		);

		// then
		result.andExpect(status().isOk());

		String responseBody = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		TestHelloResponse response = objectMapper.readValue(responseBody, TestHelloResponse.class);

		assertThat(response.message()).isEqualTo("Hello, Chori!");
	}

	@Test
	void returnHelloWithDefaultNickname() throws Exception {
		// given
		final String url = "/v1/test/hello";
		final Long memberId = 2L;
		final Member member = Member.builder().id(memberId).nickname(null).build();

		doReturn(Optional.of(member)).when(memberRepository).findById(memberId);
		Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get(url).principal(auth)
		);

		// then
		result.andExpect(status().isOk());

		String responseBody = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		TestHelloResponse response = objectMapper.readValue(responseBody, TestHelloResponse.class);

		assertThat(response.message()).isEqualTo("Hello, Member!");
	}

	@Test
	void failToReturnHelloWithMissingMember() throws Exception {
		// given
		final String url = "/v1/test/hello";
		final Long memberId = 3L;

		doReturn(Optional.empty()).when(memberRepository).findById(memberId);
		Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get(url).principal(auth)
		);

		// then
		result.andExpect(status().isUnauthorized());
	}

}
