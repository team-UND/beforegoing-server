package com.und.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.und.server.entity.Member;
import com.und.server.exception.GlobalExceptionHandler;
import com.und.server.exception.ServerErrorResult;
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
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(testController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	@DisplayName("Issues tokens for an existing member")
	void Given_ExistingMember_When_RequestAccessToken_Then_ReturnsOkWithTokens() throws Exception {
		// given
		final String url = "/v1/test/access";
		final TestAuthRequest request = new TestAuthRequest("kakao", "dummy.provider.id", "Chori");
		final AuthResponse expectedResponse = new AuthResponse(
			"Bearer",
			"access-token",
			3600,
			"refresh-token",
			7200
		);
		doReturn(expectedResponse).when(authService).issueTokensForTest(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		);

		// then
		final AuthResponse actualResponse = objectMapper.readValue(
			resultActions
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8), AuthResponse.class
		);

		resultActions.andExpect(status().isOk());
		assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
	}

	@Test
	@DisplayName("Creates a new member and issues tokens when member does not exist")
	void Given_NonExistingMember_When_RequestAccessToken_Then_CreatesMemberAndReturnsOkWithTokens() throws Exception {
		// given
		final String url = "/v1/test/access";
		final TestAuthRequest request = new TestAuthRequest("kakao", "provider-id-456", "Newbie");
		final AuthResponse expectedResponse = new AuthResponse(
			"Bearer",
			"new-access-token",
			3600,
			"new-refresh-token",
			7200
		);

		doReturn(expectedResponse).when(authService).issueTokensForTest(request);

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		);

		// then
		final AuthResponse actualResponse = objectMapper.readValue(
			resultActions
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8), AuthResponse.class
		);

		resultActions.andExpect(status().isOk());
		assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
	}

	@Test
	@DisplayName("Fails to greet and returns unauthorized when the authenticated member is not found")
	void Given_AuthenticatedUserNotFoundInDb_When_Greet_Then_ReturnsUnauthorized() throws Exception {
		// given
		final String url = "/v1/test/hello";
		final Long memberId = 3L;

		doReturn(Optional.empty()).when(memberRepository).findById(memberId);
		final Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		final ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get(url).principal(auth)
		);

		// then
		result.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ServerErrorResult.MEMBER_NOT_FOUND.name()))
			.andExpect(jsonPath("$.message").value(ServerErrorResult.MEMBER_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("Returns a personalized greeting for an authenticated user with a nickname")
	void Given_AuthenticatedUserWithNickname_When_Greet_Then_ReturnsOkWithPersonalizedMessage() throws Exception {
		// given
		final String url = "/v1/test/hello";
		final Long memberId = 1L;
		final Member member = Member.builder().id(memberId).nickname("Chori").build();

		doReturn(Optional.of(member)).when(memberRepository).findById(memberId);
		final Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		final ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get(url).principal(auth)
		);

		// then
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Hello, Chori!"));
	}

	@Test
	@DisplayName("Returns a default greeting for an authenticated user without a nickname")
	void Given_AuthenticatedUserWithoutNickname_When_Greet_Then_ReturnsOkWithDefaultMessage() throws Exception {
		// given
		final String url = "/v1/test/hello";
		final Long memberId = 2L;
		final Member member = Member.builder().id(memberId).nickname(null).build();

		doReturn(Optional.of(member)).when(memberRepository).findById(memberId);
		final Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		final ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get(url).principal(auth)
		);

		// then
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Hello, Member!"));
	}

}
