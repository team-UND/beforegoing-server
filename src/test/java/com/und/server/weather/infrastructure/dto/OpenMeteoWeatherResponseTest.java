package com.und.server.weather.infrastructure.dto;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenMeteoWeatherResponseTest {

	@Test
	@DisplayName("OpenMeteoWeatherResponse.HourlyUnits 생성 및 필드 검증")
	void testHourlyUnits() {
		OpenMeteoWeatherResponse.HourlyUnits units =
			new OpenMeteoWeatherResponse.HourlyUnits("time-unit", "weather-code");

		assertThat(units.time()).isEqualTo("time-unit");
		assertThat(units.weathercode()).isEqualTo("weather-code");
	}

	@Test
	@DisplayName("OpenMeteoWeatherResponse.Hourly 생성 및 필드 검증")
	void testHourly() {
		OpenMeteoWeatherResponse.Hourly hourly =
			new OpenMeteoWeatherResponse.Hourly(List.of("2024-01-01T00:00"), List.of(80));

		assertThat(hourly.time()).contains("2024-01-01T00:00");
		assertThat(hourly.weathercode()).contains(80);
	}

	@Test
	@DisplayName("OpenMeteoWeatherResponse 생성 및 필드 검증")
	void testResponse() {
		OpenMeteoWeatherResponse.HourlyUnits units =
			new OpenMeteoWeatherResponse.HourlyUnits("time-unit", "weather-code");
		OpenMeteoWeatherResponse.Hourly hourly =
			new OpenMeteoWeatherResponse.Hourly(List.of("2024-01-01T00:00"), List.of(80));

		OpenMeteoWeatherResponse response =
			new OpenMeteoWeatherResponse(37.5, 127.0, "Asia/Seoul", units, hourly);

		assertThat(response.latitude()).isEqualTo(37.5);
		assertThat(response.longitude()).isEqualTo(127.0);
		assertThat(response.timezone()).isEqualTo("Asia/Seoul");
		assertThat(response.hourlyUnits()).isEqualTo(units);
		assertThat(response.hourly()).isEqualTo(hourly);
	}

}
