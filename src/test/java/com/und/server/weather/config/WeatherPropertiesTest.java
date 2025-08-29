package com.und.server.weather.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WeatherProperties 테스트")
class WeatherPropertiesTest {

	@Test
	@DisplayName("WeatherProperties가 올바르게 로드된다")
	void Given_ValidProperties_When_LoadWeatherProperties_Then_PropertiesAreLoaded() {
		// given & when
		WeatherProperties.Kma kma = new WeatherProperties.Kma("https://test-kma-api.com", "test-service-key");
		WeatherProperties.OpenMeteo openMeteo = new WeatherProperties.OpenMeteo("https://test-open-meteo-api.com");
		WeatherProperties.OpenMeteoKma openMeteoKma = new WeatherProperties.OpenMeteoKma("https://test-open-meteo-kma-api.com");
		WeatherProperties weatherProperties = new WeatherProperties(kma, openMeteo, openMeteoKma);

		// then
		assertThat(weatherProperties).isNotNull();
		assertThat(kma).isNotNull();
		assertThat(openMeteo).isNotNull();
		assertThat(openMeteoKma).isNotNull();
	}

	@Test
	@DisplayName("Kma 설정이 올바르게 생성된다")
	void Given_KmaProperties_When_CreateProperties_Then_PropertiesAreCreated() {
		// given
		String serviceKey = "test-service-key";
		String baseUrl = "https://test-kma-api.com";

		// when
		WeatherProperties.Kma kma = new WeatherProperties.Kma(baseUrl, serviceKey);

		// then
		assertThat(kma.serviceKey()).isEqualTo(serviceKey);
		assertThat(kma.baseUrl()).isEqualTo(baseUrl);
	}

	@Test
	@DisplayName("OpenMeteo 설정이 올바르게 생성된다")
	void Given_OpenMeteoProperties_When_CreateProperties_Then_PropertiesAreCreated() {
		// given
		String baseUrl = "https://test-open-meteo-api.com";

		// when
		WeatherProperties.OpenMeteo openMeteo = new WeatherProperties.OpenMeteo(baseUrl);

		// then
		assertThat(openMeteo.baseUrl()).isEqualTo(baseUrl);
	}

	@Test
	@DisplayName("OpenMeteoKma 설정이 올바르게 생성된다")
	void Given_OpenMeteoKmaProperties_When_CreateProperties_Then_PropertiesAreCreated() {
		// given
		String baseUrl = "https://test-open-meteo-kma-api.com";

		// when
		WeatherProperties.OpenMeteoKma openMeteoKma = new WeatherProperties.OpenMeteoKma(baseUrl);

		// then
		assertThat(openMeteoKma.baseUrl()).isEqualTo(baseUrl);
	}

	@Test
	@DisplayName("WeatherProperties의 전체 설정이 올바르게 생성된다")
	void Given_WeatherProperties_When_CreateAllProperties_Then_AllPropertiesAreCreated() {
		// given
		WeatherProperties.Kma kma = new WeatherProperties.Kma("https://test-kma-api.com", "test-kma-service-key");
		WeatherProperties.OpenMeteo openMeteo = new WeatherProperties.OpenMeteo("https://test-open-meteo-api.com");
		WeatherProperties.OpenMeteoKma openMeteoKma = new WeatherProperties.OpenMeteoKma("https://test-open-meteo-kma-api.com");

		// when
		WeatherProperties weatherProperties = new WeatherProperties(kma, openMeteo, openMeteoKma);

		// then
		assertThat(weatherProperties.kma()).isEqualTo(kma);
		assertThat(weatherProperties.openMeteo()).isEqualTo(openMeteo);
		assertThat(weatherProperties.openMeteoKma()).isEqualTo(openMeteoKma);
	}

	@Test
	@DisplayName("Kma 설정의 null 값이 올바르게 처리된다")
	void Given_KmaProperties_When_NullValues_Then_NullValuesAreHandled() {
		// given & when
		WeatherProperties.Kma kma = new WeatherProperties.Kma(null, null);

		// then
		assertThat(kma.serviceKey()).isNull();
		assertThat(kma.baseUrl()).isNull();
	}

	@Test
	@DisplayName("OpenMeteo 설정의 null 값이 올바르게 처리된다")
	void Given_OpenMeteoProperties_When_NullValues_Then_NullValuesAreHandled() {
		// given & when
		WeatherProperties.OpenMeteo openMeteo = new WeatherProperties.OpenMeteo(null);

		// then
		assertThat(openMeteo.baseUrl()).isNull();
	}

	@Test
	@DisplayName("OpenMeteoKma 설정의 null 값이 올바르게 처리된다")
	void Given_OpenMeteoKmaProperties_When_NullValues_Then_NullValuesAreHandled() {
		// given & when
		WeatherProperties.OpenMeteoKma openMeteoKma = new WeatherProperties.OpenMeteoKma(null);

		// then
		assertThat(openMeteoKma.baseUrl()).isNull();
	}

}
