package com.und.server.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class ProfileManagerTest {

	@InjectMocks
	private ProfileManager profileManager;

	@Mock
	private Environment environment;

	@Test
	@DisplayName("Returns true when 'prod' profile is active")
	void Given_ProdProfile_When_IsProdOrStgProfile_Then_ReturnsTrue() {
		// given
		doReturn(new String[] {"prod"}).when(environment).getActiveProfiles();

		// when
		final boolean result = profileManager.isProdOrStgProfile();

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Returns true when 'stg' profile is active")
	void Given_StgProfile_When_IsProdOrStgProfile_Then_ReturnsTrue() {
		// given
		doReturn(new String[] {"stg"}).when(environment).getActiveProfiles();

		// when
		final boolean result = profileManager.isProdOrStgProfile();

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Returns false when a non-prod/stg profile is active")
	void Given_DevProfile_When_IsProdOrStgProfile_Then_ReturnsFalse() {
		// given
		doReturn(new String[] {"dev"}).when(environment).getActiveProfiles();

		// when
		final boolean result = profileManager.isProdOrStgProfile();

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("Returns false when no profiles are active")
	void Given_NoActiveProfiles_When_IsProdOrStgProfile_Then_ReturnsFalse() {
		// given
		doReturn(new String[] {}).when(environment).getActiveProfiles();

		// when
		final boolean result = profileManager.isProdOrStgProfile();

		// then
		assertThat(result).isFalse();
	}
}
