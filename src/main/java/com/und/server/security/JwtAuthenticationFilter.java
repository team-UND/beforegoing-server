package com.und.server.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.jwt.JwtProvider;

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
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final List<String> permissivePaths = Arrays.asList("/v*/auth/tokens");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		final String token = resolveToken(request);

		if (token != null) {
			try {
				final Authentication authentication = jwtProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (ServerException e) {
				final boolean isPermissivePath = permissivePaths.stream().anyMatch(
					pattern -> pathMatcher.match(pattern, request.getServletPath()));

				if (e.getErrorResult() == ServerErrorResult.EXPIRED_TOKEN && isPermissivePath) {
					// For expired tokens on permissive paths, do not stop the chain
				} else {
					// For other errors, stop the chain and write the error response
					errorResponseWriter.sendErrorResponse(response, e.getErrorResult());
					return;
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		final String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

}
