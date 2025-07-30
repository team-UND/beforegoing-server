package com.und.server.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;

@ExtendWith(MockitoExtension.class)
class AuthMemberArgumentResolverTest {

	@InjectMocks
	private AuthMemberArgumentResolver authMemberArgumentResolver;

	@Mock
	private MethodParameter parameter;

	@Mock
	private SecurityContext securityContext;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	@DisplayName("Supports parameter with @AuthMember and Long type")
	void Given_AuthMemberAnnotationAndLongType_When_SupportsParameter_Then_ReturnsTrue() {
		// given
		doReturn(true).when(parameter).hasParameterAnnotation(AuthMember.class);
		doReturn(Long.class).when(parameter).getParameterType();

		// when
		final boolean result = authMemberArgumentResolver.supportsParameter(parameter);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Does not support parameter without @AuthMember annotation")
	void Given_NoAuthMemberAnnotation_When_SupportsParameter_Then_ReturnsFalse() {
		// given
		doReturn(false).when(parameter).hasParameterAnnotation(AuthMember.class);

		// when
		final boolean result = authMemberArgumentResolver.supportsParameter(parameter);

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Does not support parameter with @AuthMember but not Long type")
	void Given_AuthMemberAnnotationButNotLongType_When_SupportsParameter_Then_ReturnsFalse() {
		// given
		doReturn(true).when(parameter).hasParameterAnnotation(AuthMember.class);
		doReturn(String.class).when(parameter).getParameterType();

		// when
		final boolean result = authMemberArgumentResolver.supportsParameter(parameter);

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Resolves memberId successfully when principal is a Long")
	void Given_ValidPrincipal_When_ResolveArgument_Then_ReturnsMemberId() {
		// given
		final Long memberId = 1L;
		final UsernamePasswordAuthenticationToken authentication
			= new UsernamePasswordAuthenticationToken(memberId, null);
		doReturn(authentication).when(securityContext).getAuthentication();

		// when
		final Object result = authMemberArgumentResolver.resolveArgument(parameter, null, null, null);

		// then
		assertThat(result).isEqualTo(memberId);
	}

	@Test
	@DisplayName("Throws ServerException when principal is not a Long")
	void Given_InvalidPrincipalType_When_ResolveArgument_Then_ThrowsServerException() {
		// given
		final String invalidPrincipal = "not-a-long";
		final UsernamePasswordAuthenticationToken authentication
			= new UsernamePasswordAuthenticationToken(invalidPrincipal, null);
		doReturn(authentication).when(securityContext).getAuthentication();

		// when & then
		assertThatThrownBy(() -> authMemberArgumentResolver.resolveArgument(parameter, null, null, null))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ServerErrorResult.UNAUTHORIZED_ACCESS);
	}
}
