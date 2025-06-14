package com.und.server.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class JwtAuthenticationFilterTest {

	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	@Mock
	private Authentication authentication;

	@BeforeEach
	void init() {
		MockitoAnnotations.openMocks(this);
		jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider, jwtProperties);

		SecurityContextHolder.clearContext();
	}

	@Test
	void setsAuthenticationAndContinuesFilterChain() throws ServletException, IOException {
		// given
		when(jwtProperties.header()).thenReturn("Authorization");
		when(jwtProperties.type()).thenReturn("Bearer");

		final String tokenWithBearer = "Bearer valid.jwt.token";

		when(request.getHeader("Authorization")).thenReturn(tokenWithBearer);
		when(jwtProvider.getAuthentication("valid.jwt.token")).thenReturn(authentication);

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void skipsSettingAuthenticationWhenHeaderMissing() throws ServletException, IOException {
		// given
		when(jwtProperties.header()).thenReturn("Authorization");
		when(request.getHeader("Authorization")).thenReturn(null);

		// when
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// then
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void throwsBadCredentialsExceptionWhenTokenDoesNotStartWithBearer() throws Exception {
		// given
		final String invalidToken = "InvalidPrefix token";

		doReturn("Authorization").when(jwtProperties).header();
		doReturn("Bearer").when(jwtProperties).type();
		doReturn(invalidToken).when(request).getHeader("Authorization");

		// when & then
		assertThatThrownBy(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain))
			.isInstanceOf(BadCredentialsException.class)
			.hasMessage("Bearer token is missing or invalid");

		verify(filterChain, never()).doFilter(request, response);
	}

}
