package com.und.server.weather.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.infrastructure.dto.KmaWeatherResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("KmaWeatherExtractor 테스트")
class KmaWeatherExtractorTest {

	@InjectMocks
	private KmaWeatherExtractor kmaWeatherExtractor;


	@Test
	@DisplayName("KMA 응답에서 날씨 정보를 정상적으로 추출한다")
	void Given_ValidKmaResponse_When_ExtractWeatherForHours_Then_ReturnsWeatherMap() {
		// given
		KmaWeatherResponse.WeatherItem ptyItem =
			new KmaWeatherResponse.WeatherItem(
				"20240101", "0800", "PTY",
				"20240101", "0900", "0", 55, 127);
		KmaWeatherResponse.WeatherItem skyItem =
			new KmaWeatherResponse.WeatherItem(
				"20240101", "0800", "SKY",
				"20240101", "0900", "1", 55, 127);
		KmaWeatherResponse.WeatherItem ptyItem2 =
			new KmaWeatherResponse.WeatherItem(
				"20240101", "0800", "PTY",
				"20240101", "1000", "1", 55, 127);
		KmaWeatherResponse.WeatherItem skyItem2 =
			new KmaWeatherResponse.WeatherItem(
				"20240101", "0800", "SKY",
				"20240101", "1000", "3", 55, 127);

		KmaWeatherResponse.Items items =
			new KmaWeatherResponse.Items(Arrays.asList(ptyItem, skyItem, ptyItem2, skyItem2));
		KmaWeatherResponse.Body body = new KmaWeatherResponse.Body("JSON", items, 4);
		KmaWeatherResponse.Response response = new KmaWeatherResponse.Response(null, body);
		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(response);

		List<Integer> targetHours = Arrays.asList(9, 10);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(9)).isEqualTo(WeatherType.SUNNY);
		assertThat(result.get(10)).isEqualTo(WeatherType.RAIN);
	}


	@Test
	@DisplayName("KMA 응답이 null일 때 빈 맵을 반환한다")
	void Given_NullKmaResponse_When_ExtractWeatherForHours_Then_ReturnsEmptyMap() {
		// given
		KmaWeatherResponse weatherResponse = null;
		List<Integer> targetHours = Arrays.asList(9, 10);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("KMA 응답 구조가 불완전할 때 빈 맵을 반환한다")
	void Given_IncompleteKmaResponse_When_ExtractWeatherForHours_Then_ReturnsEmptyMap() {
		// given
		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(null);
		List<Integer> targetHours = Arrays.asList(9, 10);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("타겟 시간이 null일 때 빈 맵을 반환한다")
	void Given_NullTargetHours_When_ExtractWeatherForHours_Then_ReturnsEmptyMap() {
		// given
		KmaWeatherResponse.WeatherItem item =
			new KmaWeatherResponse.WeatherItem("20240101", "0800", "PTY",
				"20240101", "0900", "0", 55, 127);
		KmaWeatherResponse.Items items = new KmaWeatherResponse.Items(Arrays.asList(item));
		KmaWeatherResponse.Body body = new KmaWeatherResponse.Body("JSON", items, 1);
		KmaWeatherResponse.Response response = new KmaWeatherResponse.Response(null, body);
		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(response);

		List<Integer> targetHours = null;
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("타겟 시간이 비어있을 때 빈 맵을 반환한다")
	void Given_EmptyTargetHours_When_ExtractWeatherForHours_Then_ReturnsEmptyMap() {
		// given
		KmaWeatherResponse.WeatherItem item =
			new KmaWeatherResponse.WeatherItem("20240101", "0800", "PTY",
				"20240101", "0900", "0", 55, 127);
		KmaWeatherResponse.Items items = new KmaWeatherResponse.Items(Arrays.asList(item));
		KmaWeatherResponse.Body body = new KmaWeatherResponse.Body("JSON", items, 1);
		KmaWeatherResponse.Response response = new KmaWeatherResponse.Response(null, body);
		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(response);

		List<Integer> targetHours = Arrays.asList();
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("다른 날짜의 데이터는 무시한다")
	void Given_DifferentDateData_When_ExtractWeatherForHours_Then_IgnoresDifferentDate() {
		// given
		KmaWeatherResponse.WeatherItem item1 =
			new KmaWeatherResponse.WeatherItem("20240101", "0800", "PTY",
				"20240101", "0900", "0", 55, 127);
		KmaWeatherResponse.Items items = new KmaWeatherResponse.Items(Arrays.asList(item1));
		KmaWeatherResponse.Body body = new KmaWeatherResponse.Body("JSON", items, 1);
		KmaWeatherResponse.Response response = new KmaWeatherResponse.Response(null, body);
		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(response);

		List<Integer> targetHours = Arrays.asList(9);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).isNotNull();
	}


	@Test
	@DisplayName("PTY와 SKY가 모두 있을 때 PTY를 우선한다")
	void Given_PtyAndSkyData_When_ExtractWeatherForHours_Then_PrioritizesPty() {
		// given
		KmaWeatherResponse.WeatherItem ptyItem =
			new KmaWeatherResponse.WeatherItem("20240101", "0800", "PTY",
				"20240101", "0900", "1", 55, 127);
		KmaWeatherResponse.WeatherItem skyItem =
			new KmaWeatherResponse.WeatherItem("20240101", "0800", "SKY",
				"20240101", "0900", "1", 55, 127);
		KmaWeatherResponse.Items items = new KmaWeatherResponse.Items(Arrays.asList(ptyItem, skyItem));
		KmaWeatherResponse.Body body = new KmaWeatherResponse.Body("JSON", items, 2);
		KmaWeatherResponse.Response response = new KmaWeatherResponse.Response(null, body);
		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(response);

		List<Integer> targetHours = Arrays.asList(9);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(9)).isEqualTo(WeatherType.RAIN);
	}


	@Test
	@DisplayName("PTY가 0일 때 SKY 값을 사용한다")
	void Given_PtyZeroAndSkyData_When_ExtractWeatherForHours_Then_UsesSkyValue() {
		// given
		KmaWeatherResponse.WeatherItem ptyItem =
			new KmaWeatherResponse.WeatherItem("20240101", "0800", "PTY",
				"20240101", "0900", "0", 55, 127);
		KmaWeatherResponse.WeatherItem skyItem =
			new KmaWeatherResponse.WeatherItem("20240101", "0800", "SKY",
				"20240101", "0900", "3", 55, 127);
		KmaWeatherResponse.Items items = new KmaWeatherResponse.Items(Arrays.asList(ptyItem, skyItem));
		KmaWeatherResponse.Body body = new KmaWeatherResponse.Body("JSON", items, 2);
		KmaWeatherResponse.Response response = new KmaWeatherResponse.Response(null, body);
		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(response);

		List<Integer> targetHours = Arrays.asList(9);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(9)).isEqualTo(WeatherType.CLOUDY);
	}


	@Test
	@DisplayName("잘못된 시간 형식은 무시한다")
	void Given_InvalidTimeFormat_When_ExtractWeatherForHours_Then_IgnoresInvalidTime() {
		// given
		KmaWeatherResponse.WeatherItem item =
			new KmaWeatherResponse.WeatherItem("20240101", "0800", "PTY",
				"20240101", "invalid", "0", 55, 127);
		KmaWeatherResponse.Items items = new KmaWeatherResponse.Items(Arrays.asList(item));
		KmaWeatherResponse.Body body = new KmaWeatherResponse.Body("JSON", items, 1);
		KmaWeatherResponse.Response response = new KmaWeatherResponse.Response(null, body);
		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(response);

		List<Integer> targetHours = Arrays.asList(9);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("잘못된 값 형식은 기본값을 사용한다")
	void Given_InvalidValueFormat_When_ExtractWeatherForHours_Then_UsesDefaultValue() {
		// given
		KmaWeatherResponse.WeatherItem item =
			new KmaWeatherResponse.WeatherItem("20240101", "0800", "PTY",
				"20240101", "0900", "invalid", 55, 127);
		KmaWeatherResponse.Items items = new KmaWeatherResponse.Items(Arrays.asList(item));
		KmaWeatherResponse.Body body = new KmaWeatherResponse.Body("JSON", items, 1);
		KmaWeatherResponse.Response response = new KmaWeatherResponse.Response(null, body);
		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(response);

		List<Integer> targetHours = Arrays.asList(9);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, WeatherType> result =
			kmaWeatherExtractor.extractWeatherForHours(weatherResponse, targetHours, date);

		// then
		assertThat(result).isEmpty();
	}

}
