package com.und.server.weather.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.weather.constants.TimeSlot;

@DisplayName("WeatherKeyGenerator 테스트")
class WeatherKeyGeneratorTest {

	private WeatherKeyGenerator weatherKeyGenerator;

	@BeforeEach
	void setUp() {
		weatherKeyGenerator = new WeatherKeyGenerator();
	}

	@Test
	@DisplayName("오늘 날씨 캐시 키를 생성할 수 있다")
	void Given_LatitudeAndLongitudeAndTodayAndSlot_When_GenerateTodayKey_Then_ReturnsCacheKey() {
		// given
		Double latitude = 37.5665;
		Double longitude = 126.9780;
		LocalDate today = LocalDate.of(2024, 1, 15);
		TimeSlot slot = TimeSlot.SLOT_12_15;

		// when
		String result = weatherKeyGenerator.generateTodayKey(latitude, longitude, today, slot);

		// then
		assertThat(result).isNotNull();
		assertThat(result).contains("wx");
		assertThat(result).contains("today");
		assertThat(result).contains("2024-01-15");
		assertThat(result).contains("SLOT_12_15");
	}


	@Test
	@DisplayName("미래 날씨 캐시 키를 생성할 수 있다")
	void Given_LatitudeAndLongitudeAndFutureDateAndSlot_When_GenerateFutureKey_Then_ReturnsCacheKey() {
		// given
		Double latitude = 37.5665;
		Double longitude = 126.9780;
		LocalDate futureDate = LocalDate.of(2024, 1, 20);
		TimeSlot slot = TimeSlot.SLOT_06_09;

		// when
		String result = weatherKeyGenerator.generateFutureKey(latitude, longitude, futureDate, slot);

		// then
		assertThat(result).isNotNull();
		assertThat(result).contains("wx");
		assertThat(result).contains("future");
		assertThat(result).contains("2024-01-20");
		assertThat(result).contains("SLOT_06_09");
	}


	@Test
	@DisplayName("시간대별 필드 키를 생성할 수 있다")
	void Given_DateTime_When_GenerateTodayHourFieldKey_Then_ReturnsHourString() {
		// given
		LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);

		// when
		String result = weatherKeyGenerator.generateTodayHourFieldKey(dateTime);

		// then
		assertThat(result).isEqualTo("14");
	}


	@Test
	@DisplayName("자정 시간대 필드 키를 생성할 수 있다")
	void Given_MidnightDateTime_When_GenerateTodayHourFieldKey_Then_ReturnsZeroHourString() {
		// given
		LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 0, 0);

		// when
		String result = weatherKeyGenerator.generateTodayHourFieldKey(dateTime);

		// then
		assertThat(result).isEqualTo("00");
	}


	@Test
	@DisplayName("자정 직전 시간대 필드 키를 생성할 수 있다")
	void Given_BeforeMidnightDateTime_When_GenerateTodayHourFieldKey_Then_ReturnsTwentyThreeHourString() {
		// given
		LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 23, 59);

		// when
		String result = weatherKeyGenerator.generateTodayHourFieldKey(dateTime);

		// then
		assertThat(result).isEqualTo("23");
	}


	@Test
	@DisplayName("다양한 시간대에 대해 캐시 키를 생성할 수 있다")
	void Given_DifferentTimeSlots_When_GenerateKeys_Then_ReturnsValidCacheKeys() {
		// given
		Double latitude = 37.5665;
		Double longitude = 126.9780;
		LocalDate date = LocalDate.of(2024, 1, 15);

		// when & then
		for (TimeSlot slot : TimeSlot.values()) {
			String todayKey = weatherKeyGenerator.generateTodayKey(latitude, longitude, date, slot);
			String futureKey = weatherKeyGenerator.generateFutureKey(latitude, longitude, date, slot);

			assertThat(todayKey).isNotNull();
			assertThat(futureKey).isNotNull();
			assertThat(todayKey).contains(slot.name());
			assertThat(futureKey).contains(slot.name());
		}
	}


	@Test
	@DisplayName("다양한 지역에 대해 캐시 키를 생성할 수 있다")
	void Given_DifferentLocations_When_GenerateTodayKey_Then_ReturnsDifferentCacheKeys() {
		// given
		LocalDate date = LocalDate.of(2024, 1, 15);
		TimeSlot slot = TimeSlot.SLOT_12_15;

		// 서울
		String seoulKey = weatherKeyGenerator.generateTodayKey(37.5665, 126.9780, date, slot);
		// 부산
		String busanKey = weatherKeyGenerator.generateTodayKey(35.1796, 129.0756, date, slot);
		// 제주도
		String jejuKey = weatherKeyGenerator.generateTodayKey(33.4996, 126.5312, date, slot);

		// then
		assertThat(seoulKey).isNotNull();
		assertThat(busanKey).isNotNull();
		assertThat(jejuKey).isNotNull();
		assertThat(seoulKey).isNotEqualTo(busanKey);
		assertThat(busanKey).isNotEqualTo(jejuKey);
		assertThat(seoulKey).isNotEqualTo(jejuKey);
	}


	@Test
	@DisplayName("같은 좌표와 시간대는 같은 캐시 키를 생성한다")
	void Given_SameCoordinatesAndTimeSlot_When_GenerateTodayKey_Then_ReturnsSameCacheKey() {
		// given
		Double latitude = 37.5665;
		Double longitude = 126.9780;
		LocalDate date = LocalDate.of(2024, 1, 15);
		TimeSlot slot = TimeSlot.SLOT_12_15;

		// when
		String key1 = weatherKeyGenerator.generateTodayKey(latitude, longitude, date, slot);
		String key2 = weatherKeyGenerator.generateTodayKey(latitude, longitude, date, slot);

		// then
		assertThat(key1).isEqualTo(key2);
	}


	@Test
	@DisplayName("캐시 키 형식이 올바르다")
	void Given_ValidInputs_When_GenerateCacheKeys_Then_ReturnsCorrectFormat() {
		// given
		Double latitude = 37.5665;
		Double longitude = 126.9780;
		LocalDate date = LocalDate.of(2024, 1, 15);
		TimeSlot slot = TimeSlot.SLOT_12_15;

		// when
		String todayKey = weatherKeyGenerator.generateTodayKey(latitude, longitude, date, slot);
		String futureKey = weatherKeyGenerator.generateFutureKey(latitude, longitude, date, slot);

		// then
		// 형식: wx:today:gridX:gridY:date:slot 또는 wx:future:gridX:gridY:date:slot
		assertThat(todayKey).matches("wx:today:\\d+:\\d+:\\d{4}-\\d{2}-\\d{2}:SLOT_\\d{2}_\\d{2}");
		assertThat(futureKey).matches("wx:future:\\d+:\\d+:\\d{4}-\\d{2}-\\d{2}:SLOT_\\d{2}_\\d{2}");
	}

}
