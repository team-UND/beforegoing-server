package com.und.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class ObservabilityUserConfig {

	private final String prometheusUsername;
	private final String prometheusPassword;

	public ObservabilityUserConfig(
		@Value("${observability.prometheus.username}") final String prometheusUsername,
		@Value("${observability.prometheus.password}") final String prometheusPassword
	) {
		this.prometheusUsername = prometheusUsername;
		this.prometheusPassword = prometheusPassword;
	}

	@Bean
	public InMemoryUserDetailsManager prometheusUserDetails(final PasswordEncoder passwordEncoder) {
		return new InMemoryUserDetailsManager(User.builder()
			.username(prometheusUsername)
			.password(passwordEncoder.encode(prometheusPassword))
			.roles("OBSERVABILITY")
			.build());
	}
}
