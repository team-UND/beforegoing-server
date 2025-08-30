package com.und.server.weather.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.weather.constants.UvType;
import com.und.server.weather.infrastructure.dto.OpenMeteoResponse;

class UvIndexExtractorTest {

	private final UvIndexExtractor extractor = new UvIndexExtractor();

	@Test
	@DisplayName("정상 응답에서 UV 정보를 추출한다")
	void Given_ValidOpenMeteoResponse_When_ExtractUvForHours_Then_ReturnsUvMap() {
		// given
		List<String> times = Arrays.asList(
			"2024-01-01T09:00",
			"2024-01-01T10:00",
			"2024-01-01T11:00",
			"2024-01-01T12:00",
			"2024-01-01T13:00"
		);
		List<Double> uvIndex = Arrays.asList(1.0, 3.0, 5.0, 8.0, 12.0); // VERY_LOW, LOW, NORMAL, HIGH, VERY_HIGH
		List<Double> pm25 = Collections.nCopies(5, 0.0); // dummy
		List<Double> pm10 = Collections.nCopies(5, 0.0); // dummy

		OpenMeteoResponse.Hourly hourly = new OpenMeteoResponse.Hourly(times, pm25, pm10, uvIndex);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		List<Integer> targetHours = Arrays.asList(9, 10, 11, 12, 13);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, UvType> result = extractor.extractUvForHours(response, targetHours, date);

		// then
		assertThat(result).hasSize(5);
		assertThat(result.get(9)).isEqualTo(UvType.VERY_LOW);
		assertThat(result.get(10)).isEqualTo(UvType.LOW);
		assertThat(result.get(11)).isEqualTo(UvType.NORMAL);
		assertThat(result.get(12)).isEqualTo(UvType.HIGH);
		assertThat(result.get(13)).isEqualTo(UvType.VERY_HIGH);
	}

	@Test
	@DisplayName("응답이 null이면 빈 맵을 반환한다")
	void Given_NullResponse_When_ExtractUvForHours_Then_ReturnsEmptyMap() {
		Map<Integer, UvType> result =
			extractor.extractUvForHours(null, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("hourly가 null이면 빈 맵을 반환한다")
	void Given_NullHourly_When_ExtractUvForHours_Then_ReturnsEmptyMap() {
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, null);

		Map<Integer, UvType> result =
			extractor.extractUvForHours(response, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("times/uvIndex가 null이면 빈 맵을 반환한다")
	void Given_InvalidData_When_ExtractUvForHours_Then_ReturnsEmptyMap() {
		OpenMeteoResponse.Hourly hourly =
			new OpenMeteoResponse.Hourly(null, null, null, null);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		Map<Integer, UvType> result =
			extractor.extractUvForHours(response, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("날짜가 다르면 결과에 포함되지 않는다")
	void Given_DifferentDate_When_ExtractUvForHours_Then_Ignore() {
		List<String> times = List.of("2024-01-02T09:00");
		List<Double> uvIndex = List.of(5.0);
		List<Double> pm25 = List.of(0.0);
		List<Double> pm10 = List.of(0.0);

		OpenMeteoResponse.Hourly hourly =
			new OpenMeteoResponse.Hourly(times, pm25, pm10, uvIndex);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		Map<Integer, UvType> result =
			extractor.extractUvForHours(response, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("hour 파싱이 불가능하면 무시한다")
	void Given_InvalidHourString_When_ExtractUvForHours_Then_Ignore() {
		List<String> times = List.of("2024-01-01Tabc");
		List<Double> uvIndex = List.of(5.0);
		List<Double> pm25 = List.of(0.0);
		List<Double> pm10 = List.of(0.0);

		OpenMeteoResponse.Hourly hourly =
			new OpenMeteoResponse.Hourly(times, pm25, pm10, uvIndex);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		Map<Integer, UvType> result =
			extractor.extractUvForHours(response, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("uvIndex 값이 부족하거나 null이면 무시된다")
	void Given_IndexOutOfBoundsOrNull_When_ExtractUvForHours_Then_Ignore() {
		List<String> times = Arrays.asList("2024-01-01T09:00", "2024-01-01T10:00");
		List<Double> uvIndex = Arrays.asList(null, null);
		List<Double> pm25 = Arrays.asList(0.0, 0.0);
		List<Double> pm10 = Arrays.asList(0.0, 0.0);

		OpenMeteoResponse.Hourly hourly =
			new OpenMeteoResponse.Hourly(times, pm25, pm10, uvIndex);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		Map<Integer, UvType> result =
			extractor.extractUvForHours(response, Arrays.asList(9, 10), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	// --- UvType enum 전용 검증 ---

	@Test
	@DisplayName("UvType fromUvIndex 구간별 매핑 확인")
	void Given_Value_When_FromUvIndex_Then_CorrectLevel() {
		assertThat(UvType.fromUvIndex(1)).isEqualTo(UvType.VERY_LOW);
		assertThat(UvType.fromUvIndex(3)).isEqualTo(UvType.LOW);
		assertThat(UvType.fromUvIndex(5)).isEqualTo(UvType.NORMAL);
		assertThat(UvType.fromUvIndex(8)).isEqualTo(UvType.HIGH);
		assertThat(UvType.fromUvIndex(12)).isEqualTo(UvType.VERY_HIGH);
	}

	@Test
	@DisplayName("UvType getWorst는 더 나쁜 수준을 반환한다")
	void Given_TwoLevels_When_GetWorst_Then_ReturnWorst() {
		UvType result = UvType.getWorst(List.of(UvType.LOW, UvType.HIGH));
		assertThat(result).isEqualTo(UvType.HIGH);
	}

}
