package com.und.server.weather.service;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.infrastructure.dto.OpenMeteoWeatherResponse;

class OpenMeteoWeatherExtractorTest {

	private final OpenMeteoWeatherExtractor extractor = new OpenMeteoWeatherExtractor();


	@Test
	@DisplayName("OpenMeteo 응답에서 날씨 정보를 정상적으로 추출한다")
	void Given_ValidOpenMeteoResponse_When_ExtractWeatherForHours_Then_ReturnsWeatherMap() {
		// given
		List<String> times = Arrays.asList("2024-01-01T09:00", "2024-01-01T10:00");
		List<Integer> codes = Arrays.asList(0, 61); // SUNNY, RAIN
		OpenMeteoWeatherResponse.Hourly hourly = new OpenMeteoWeatherResponse.Hourly(times, codes);
		OpenMeteoWeatherResponse response =
			new OpenMeteoWeatherResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		List<Integer> targetHours = Arrays.asList(9, 10);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result = extractor.extractWeatherForHours(response, targetHours, date);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(9)).isEqualTo(WeatherType.SUNNY);
		assertThat(result.get(10)).isEqualTo(WeatherType.RAIN);
	}


	@Test
	@DisplayName("응답이 null이면 빈 맵을 반환한다")
	void Given_NullResponse_When_ExtractWeatherForHours_Then_ReturnsEmptyMap() {
		// when
		Map<Integer, WeatherType> result =
			extractor.extractWeatherForHours(null, List.of(9), LocalDate.of(2024, 1, 1));

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("hourly가 null이면 빈 맵을 반환한다")
	void Given_NullHourly_When_ExtractWeatherForHours_Then_ReturnsEmptyMap() {
		// given
		OpenMeteoWeatherResponse response =
			new OpenMeteoWeatherResponse(37.5, 127.0, "Asia/Seoul", null, null);

		// when
		Map<Integer, WeatherType> result =
			extractor.extractWeatherForHours(response, List.of(9), LocalDate.of(2024, 1, 1));

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("times나 weatherCodes가 null이면 빈 맵을 반환한다")
	void Given_InvalidData_When_ExtractWeatherForHours_Then_ReturnsEmptyMap() {
		// given
		OpenMeteoWeatherResponse.Hourly hourly =
			new OpenMeteoWeatherResponse.Hourly(null, null);
		OpenMeteoWeatherResponse response =
			new OpenMeteoWeatherResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		// when
		Map<Integer, WeatherType> result =
			extractor.extractWeatherForHours(response, List.of(9), LocalDate.of(2024, 1, 1));

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("날짜가 일치하지 않으면 결과에 포함되지 않는다")
	void Given_DifferentDate_When_ExtractWeatherForHours_Then_Ignore() {
		// given
		List<String> times = List.of("2024-01-02T09:00"); // 날짜 불일치
		List<Integer> codes = List.of(0);
		OpenMeteoWeatherResponse.Hourly hourly =
			new OpenMeteoWeatherResponse.Hourly(times, codes);
		OpenMeteoWeatherResponse response =
			new OpenMeteoWeatherResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		// when
		Map<Integer, WeatherType> result =
			extractor.extractWeatherForHours(response, List.of(9), LocalDate.of(2024, 1, 1));

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("hour 파싱이 불가능하면 무시한다")
	void Given_InvalidHourString_When_ExtractWeatherForHours_Then_Ignore() {
		// given
		List<String> times = List.of("2024-01-01Tabc"); // 시간 파싱 불가
		List<Integer> codes = List.of(0);
		OpenMeteoWeatherResponse.Hourly hourly =
			new OpenMeteoWeatherResponse.Hourly(times, codes);
		OpenMeteoWeatherResponse response =
			new OpenMeteoWeatherResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		// when
		Map<Integer, WeatherType> result =
			extractor.extractWeatherForHours(response, List.of(9), LocalDate.of(2024, 1, 1));

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("weatherCodes가 부족하거나 null이면 무시된다")
	void Given_IndexOutOfBoundsOrNull_When_ExtractWeatherForHours_Then_Ignore() {
		// given
		List<String> times =
			Arrays.asList("2024-01-01T09:00", "2024-01-01T10:00");
		List<Integer> codes = Collections.singletonList(null); // index 0 null, index 1 없음
		OpenMeteoWeatherResponse.Hourly hourly =
			new OpenMeteoWeatherResponse.Hourly(times, codes);
		OpenMeteoWeatherResponse response =
			new OpenMeteoWeatherResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		// when
		Map<Integer, WeatherType> result =
			extractor.extractWeatherForHours(response, Arrays.asList(9, 10), LocalDate.of(2024, 1, 1));

		// then
		assertThat(result).isEmpty();
	}

}
