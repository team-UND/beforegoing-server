package com.und.server.common.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
import com.und.server.auth.dto.response.AuthResponse;
import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.auth.filter.AuthMemberArgumentResolver;
import com.und.server.auth.service.AuthService;
import com.und.server.common.dto.request.TestAuthRequest;
import com.und.server.common.exception.GlobalExceptionHandler;
import com.und.server.common.exception.ServerException;
import com.und.server.member.dto.response.MemberResponse;
import com.und.server.member.entity.Member;
import com.und.server.member.exception.MemberErrorResult;
import com.und.server.member.service.MemberService;
import com.und.server.terms.dto.response.TermsAgreementResponse;
import com.und.server.terms.service.TermsService;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

	@InjectMocks
	private TestController testController;

	@Mock
	private MemberService memberService;

	@Mock
	private AuthService authService;

	@Mock
	private TermsService termsService;

	@Mock
	private AuthMemberArgumentResolver authMemberArgumentResolver;

	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(testController)
			.setCustomArgumentResolvers(authMemberArgumentResolver)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	@DisplayName("Issues tokens for an existing member")
	void Given_ExistingMember_When_LoginWithoutProviderId_Then_ReturnsCreatedWithTokens() throws Exception {
		// given
		final String url = "/v1/test/access";
		final TestAuthRequest request = new TestAuthRequest("kakao", "dummy.provider.id");
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

		resultActions.andExpect(status().isCreated());
		assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
	}

	@Test
	@DisplayName("Creates a new member and issues tokens when member does not exist")
	void Given_NonExistingMember_When_LoginWithoutProviderId_Then_CreatesMemberAndReturns()throws Exception {
		// given
		final String url = "/v1/test/access";
		final TestAuthRequest request = new TestAuthRequest("kakao", "provider-id-456");
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

		resultActions.andExpect(status().isCreated());
		assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
	}

	@Test
	@DisplayName("Fails to greet and returns not found when the authenticated member is not found")
	void Given_AuthenticatedUserNotFoundInDb_When_Greet_Then_ReturnsNotFound() throws Exception {
		// given
		final String url = "/v1/test/hello";
		final Long memberId = 3L;

		doThrow(new ServerException(MemberErrorResult.MEMBER_NOT_FOUND)).when(memberService).findMemberById(memberId);
		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());

		// when
		final ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
		);

		// then
		result.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(MemberErrorResult.MEMBER_NOT_FOUND.name()))
			.andExpect(jsonPath("$.message").value(MemberErrorResult.MEMBER_NOT_FOUND.getMessage()));
	}

	@Test
	@DisplayName("Fails to greet and returns unauthorized when user is not authenticated")
	void Given_UnauthenticatedUser_When_Greet_Then_ReturnsUnauthorized() throws Exception {
		// given
		final String url = "/v1/test/hello";
		final AuthErrorResult errorResult = AuthErrorResult.UNAUTHORIZED_ACCESS;

		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doThrow(new ServerException(errorResult))
			.when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());

		// when
		final ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
		);

		// then
		result.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(errorResult.name()))
			.andExpect(jsonPath("$.message").value(errorResult.getMessage()));
	}

	@Test
	@DisplayName("Returns a personalized greeting for an authenticated user with a nickname")
	void Given_AuthenticatedUserWithNickname_When_Greet_Then_ReturnsOkWithPersonalizedMessage() throws Exception {
		// given
		final String url = "/v1/test/hello";
		final Long memberId = 1L;
		final Member member = Member.builder().id(memberId).nickname("Chori").build();

		doReturn(member).when(memberService).findMemberById(memberId);
		doReturn(true).when(authMemberArgumentResolver).supportsParameter(any());
		doReturn(memberId).when(authMemberArgumentResolver).resolveArgument(any(), any(), any(), any());

		// when
		final ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
		);

		// then
		result.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Hello, Chori!"));
	}

	@Test
	@DisplayName("Retrieves all members and returns them as a list of MemberResponse DTOs")
	void Given_ExistingMembers_When_GetMemberList_Then_ReturnsListOfMemberResponses() throws Exception {
		// given
		final String url = "/v1/test/members";
		final List<MemberResponse> expectedResponse = List.of(
			new MemberResponse(1L, "user1", "dummyKakaoId", null, null, null),
			new MemberResponse(2L, "user2", null, "dummyAppleId", null, null)
		);
		doReturn(expectedResponse).when(memberService).getMemberList();

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
		);

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].id").value(1L))
			.andExpect(jsonPath("$[0].nickname").value("user1"))
			.andExpect(jsonPath("$[1].id").value(2L))
			.andExpect(jsonPath("$[1].nickname").value("user2"));
	}

	@Test
	@DisplayName("Returns a list of terms agreements when terms exist")
	void Given_TermsExist_When_GetTermsList_Then_ReturnsOkWithTermsList() throws Exception {
		// given
		final String url = "/v1/test/terms";
		final List<TermsAgreementResponse> expectedResponse = List.of(
			new TermsAgreementResponse(1L, 101L, true, true, true, false),
			new TermsAgreementResponse(2L, 102L, true, true, true, true)
		);
		doReturn(expectedResponse).when(termsService).getTermsList();

		// when
		final ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders.get(url)
		);

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].id").value(1L))
			.andExpect(jsonPath("$[0].memberId").value(101L))
			.andExpect(jsonPath("$[0].termsOfServiceAgreed").value(true))
			.andExpect(jsonPath("$[1].id").value(2L))
			.andExpect(jsonPath("$[1].memberId").value(102L))
			.andExpect(jsonPath("$[1].eventPushAgreed").value(true));
	}
}
