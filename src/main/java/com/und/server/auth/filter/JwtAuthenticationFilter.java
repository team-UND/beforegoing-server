package com.und.server.auth.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.auth.jwt.JwtProvider;
import com.und.server.common.exception.ServerException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final SecurityErrorResponseWriter errorResponseWriter;
	private static final AntPathMatcher pathMatcher = new AntPathMatcher();
	private static final List<String> permissivePaths = List.of("/v*/auth/tokens");

	@Override
	protected void doFilterInternal(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final FilterChain filterChain
	) throws ServletException, IOException {
		final String token = resolveToken(request);
		if (token != null) {
			try {
				final Authentication authentication = jwtProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (final ServerException e) {
				final boolean isPermissivePath = permissivePaths.stream().anyMatch(
					pattern -> pathMatcher.match(pattern, request.getServletPath())
				);

				if (e.getErrorResult() != AuthErrorResult.EXPIRED_TOKEN || !isPermissivePath) {
					errorResponseWriter.sendErrorResponse(response, e.getErrorResult());
					return;
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(final HttpServletRequest request) {
		final String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		return null;
	}

}
