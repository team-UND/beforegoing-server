package com.und.server.jwt;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.sentry.Sentry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final JwtProperties jwtProperties;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			Optional.ofNullable(request.getHeader(jwtProperties.header()))
				.map(this::replaceBearerToBlank)
				.map(jwtProvider::getAuthentication)
				.ifPresent(SecurityContextHolder.getContext()::setAuthentication);

			filterChain.doFilter(request, response);
		} catch (Exception e) {
			Sentry.captureException(e);
			SecurityContextHolder.clearContext();
			throw e;
		}
	}

	private String replaceBearerToBlank(String token) {
		final String prefix = jwtProperties.type() + " ";
		if (!token.startsWith(prefix)) {
			throw new BadCredentialsException("Bearer token is missing or invalid");
		}

		return token.substring(prefix.length());
	}

}
