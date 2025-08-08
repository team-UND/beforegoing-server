package com.und.server.auth.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.und.server.auth.filter.CustomAuthenticationEntryPoint;
import com.und.server.auth.filter.JwtAuthenticationFilter;
import com.und.server.common.util.ProfileManager;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final ProfileManager profileManager;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@Order(1)
	public SecurityFilterChain actuatorSecurityFilterChain(final HttpSecurity http) throws Exception {
		return http
			.securityMatcher(EndpointRequest.toAnyEndpoint())
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(EndpointRequest.to("health")).permitAll()
				.requestMatchers(EndpointRequest.to("prometheus")).hasRole("OBSERVABILITY")
				.anyRequest().denyAll()
			)
			.httpBasic(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain apiSecurityFilterChain(final HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(authorize -> {
				authorize
					.requestMatchers(HttpMethod.POST, "/v*/auth/**").permitAll()
					.requestMatchers("/error").permitAll();

				if (!profileManager.isProdOrStgProfile()) {
					authorize
						.requestMatchers(HttpMethod.POST, "/v*/test/access").permitAll()
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
				}

				authorize.anyRequest().authenticated();
			})
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(handler -> handler.authenticationEntryPoint(customAuthenticationEntryPoint))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.build();
	}

}
