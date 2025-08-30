package com.und.server.weather.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.infrastructure.dto.OpenMeteoResponse;

class FineDustExtractorTest {

	private final FineDustExtractor extractor = new FineDustExtractor();

	@Test
	@DisplayName("정상 응답에서 미세먼지 정보를 추출한다 (GOOD, BAD, VERY_BAD)")
	void Given_ValidOpenMeteoResponse_When_ExtractDustForHours_Then_ReturnsFineDustMap() {
		// given
		List<String> times = Arrays.asList(
			"2024-01-01T09:00",
			"2024-01-01T10:00",
			"2024-01-01T11:00"
		);
		List<Double> pm25 = Arrays.asList(10.0, 50.0, 80.0);   // GOOD, BAD, VERY_BAD
		List<Double> pm10 = Arrays.asList(20.0, 120.0, 200.0); // GOOD, BAD, VERY_BAD
		List<Double> uv = Arrays.asList(1.0, 2.0, 3.0);

		OpenMeteoResponse.Hourly hourly = new OpenMeteoResponse.Hourly(times, pm25, pm10, uv);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		List<Integer> targetHours = Arrays.asList(9, 10, 11);
		LocalDate date = LocalDate.of(2024, 1, 1);

		// when
		Map<Integer, FineDustType> result = extractor.extractDustForHours(response, targetHours, date);

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(9)).isEqualTo(FineDustType.GOOD);
		assertThat(result.get(10)).isEqualTo(FineDustType.BAD);
		assertThat(result.get(11)).isEqualTo(FineDustType.VERY_BAD);
	}

	@Test
	@DisplayName("응답이 null이면 빈 맵을 반환한다")
	void Given_NullResponse_When_ExtractDustForHours_Then_ReturnsEmptyMap() {
		Map<Integer, FineDustType> result =
			extractor.extractDustForHours(null, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("hourly가 null이면 빈 맵을 반환한다")
	void Given_NullHourly_When_ExtractDustForHours_Then_ReturnsEmptyMap() {
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, null);

		Map<Integer, FineDustType> result =
			extractor.extractDustForHours(response, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("times/pm10/pm25가 null이면 빈 맵을 반환한다")
	void Given_InvalidData_When_ExtractDustForHours_Then_ReturnsEmptyMap() {
		OpenMeteoResponse.Hourly hourly =
			new OpenMeteoResponse.Hourly(null, null, null, null);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		Map<Integer, FineDustType> result =
			extractor.extractDustForHours(response, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("날짜가 다르면 결과에 포함되지 않는다")
	void Given_DifferentDate_When_ExtractDustForHours_Then_Ignore() {
		List<String> times = List.of("2024-01-02T09:00");
		List<Double> pm25 = List.of(10.0);
		List<Double> pm10 = List.of(20.0);
		List<Double> uv = List.of(1.0);

		OpenMeteoResponse.Hourly hourly =
			new OpenMeteoResponse.Hourly(times, pm25, pm10, uv);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		Map<Integer, FineDustType> result =
			extractor.extractDustForHours(response, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("hour 파싱이 불가능하면 무시한다")
	void Given_InvalidHourString_When_ExtractDustForHours_Then_Ignore() {
		List<String> times = List.of("2024-01-01Tabc");
		List<Double> pm25 = List.of(10.0);
		List<Double> pm10 = List.of(20.0);
		List<Double> uv = List.of(1.0);

		OpenMeteoResponse.Hourly hourly =
			new OpenMeteoResponse.Hourly(times, pm25, pm10, uv);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		Map<Integer, FineDustType> result =
			extractor.extractDustForHours(response, List.of(9), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("pm10/pm25 값이 부족하거나 null이면 무시된다")
	void Given_IndexOutOfBoundsOrNull_When_ExtractDustForHours_Then_Ignore() {
		List<String> times = Arrays.asList("2024-01-01T09:00", "2024-01-01T10:00");
		List<Double> pm25 = Arrays.asList(null, 50.0); // 첫 번째 null
		List<Double> pm10 = Collections.singletonList(20.0); // index 1 없음
		List<Double> uv = Arrays.asList(1.0, 2.0);

		OpenMeteoResponse.Hourly hourly =
			new OpenMeteoResponse.Hourly(times, pm25, pm10, uv);
		OpenMeteoResponse response =
			new OpenMeteoResponse(37.5, 127.0, "Asia/Seoul", null, hourly);

		Map<Integer, FineDustType> result =
			extractor.extractDustForHours(response, Arrays.asList(9, 10), LocalDate.of(2024, 1, 1));
		assertThat(result).isEmpty();
	}

	// --- FineDustType enum 전용 검증 ---

	@Test
	@DisplayName("FineDustType 구간별 매핑 동작 확인")
	void Given_Values_When_FromPm10AndPm25_Then_CorrectLevel() {
		assertThat(FineDustType.fromPm10Concentration(20)).isEqualTo(FineDustType.GOOD);
		assertThat(FineDustType.fromPm10Concentration(100)).isEqualTo(FineDustType.BAD);
		assertThat(FineDustType.fromPm25Concentration(10)).isEqualTo(FineDustType.GOOD);
		assertThat(FineDustType.fromPm25Concentration(40)).isEqualTo(FineDustType.BAD);
	}

	@Test
	@DisplayName("FineDustType getWorst는 더 나쁜 수준을 반환한다")
	void Given_TwoLevels_When_GetWorst_Then_ReturnWorst() {
		FineDustType result = FineDustType.getWorst(List.of(FineDustType.GOOD, FineDustType.BAD));
		assertThat(result).isEqualTo(FineDustType.BAD);
	}

}
