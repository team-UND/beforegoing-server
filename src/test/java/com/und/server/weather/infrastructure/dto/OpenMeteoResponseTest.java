package com.und.server.weather.infrastructure.dto;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenMeteoResponseTest {

	@Test
	@DisplayName("OpenMeteoResponse.HourlyUnits 생성 및 필드 검증")
	void testHourlyUnits() {
		OpenMeteoResponse.HourlyUnits units =
			new OpenMeteoResponse.HourlyUnits("time-unit", "µg/m3", "µg/m3", "index");

		assertThat(units.time()).isEqualTo("time-unit");
		assertThat(units.pm25()).isEqualTo("µg/m3");
		assertThat(units.pm10()).isEqualTo("µg/m3");
		assertThat(units.uvIndex()).isEqualTo("index");
	}

	@Test
	@DisplayName("OpenMeteoResponse.Hourly 생성 및 필드 검증")
	void testHourly() {
		OpenMeteoResponse.Hourly hourly = new OpenMeteoResponse.Hourly(
			List.of("2024-01-01T00:00"),
			List.of(10.0),
			List.of(20.0),
			List.of(5.0)
		);

		assertThat(hourly.time()).contains("2024-01-01T00:00");
		assertThat(hourly.pm25()).contains(10.0);
		assertThat(hourly.pm10()).contains(20.0);
		assertThat(hourly.uvIndex()).contains(5.0);
	}

	@Test
	@DisplayName("OpenMeteoResponse 생성 및 필드 검증")
	void testResponse() {
		OpenMeteoResponse.HourlyUnits units =
			new OpenMeteoResponse.HourlyUnits("time-unit", "µg/m3", "µg/m3", "index");
		OpenMeteoResponse.Hourly hourly = new OpenMeteoResponse.Hourly(
			List.of("2024-01-01T00:00"), List.of(10.0), List.of(20.0), List.of(5.0)
		);

		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", units, hourly);

		assertThat(response.latitude()).isEqualTo(37.5);
		assertThat(response.longitude()).isEqualTo(127.0);
		assertThat(response.timezone()).isEqualTo("Asia/Seoul");
		assertThat(response.hourlyUnits()).isEqualTo(units);
		assertThat(response.hourly()).isEqualTo(hourly);
	}

}
