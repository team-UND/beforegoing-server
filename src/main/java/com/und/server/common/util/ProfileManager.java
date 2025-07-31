package com.und.server.common.util;

import java.util.Arrays;
import java.util.Set;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProfileManager {

	private final Environment environment;

	public boolean isProdOrStgProfile() {
		return Arrays.stream(environment.getActiveProfiles())
			.anyMatch(Set.of("prod", "stg")::contains);
	}
}
