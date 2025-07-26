package com.und.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class ObservabilityUserConfig {

	@Value("${observability.prometheus.username}")
	private String prometheusUsername;

	@Value("${observability.prometheus.password}")
	private String prometheusPassword;

	@Bean
	public InMemoryUserDetailsManager prometheusUserDetails(PasswordEncoder passwordEncoder) {
		return new InMemoryUserDetailsManager(User.builder()
			.username(prometheusUsername)
			.password(passwordEncoder.encode(prometheusPassword))
			.roles("OBSERVABILITY")
			.build());
	}
}
