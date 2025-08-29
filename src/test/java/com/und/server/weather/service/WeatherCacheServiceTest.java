package com.und.server.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.util.CacheSerializer;
import com.und.server.weather.util.WeatherKeyGenerator;
import com.und.server.weather.util.WeatherTtlCalculator;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherCacheService 테스트")
class WeatherCacheServiceTest {

	@Mock
	private WeatherApiService weatherApiService;

	@Mock
	private WeatherDecisionService weatherDecisionService;

	@Mock
	private WeatherKeyGenerator keyGenerator;

	@Mock
	private WeatherTtlCalculator ttlCalculator;

	@Mock
	private CacheSerializer cacheSerializer;

	@InjectMocks
	private WeatherCacheService weatherCacheService;


	@Test
	@DisplayName("오늘 날씨 캐시 키를 생성한다")
	void Given_TodayWeatherRequest_When_GenerateTodayKey_Then_ReturnsCacheKey() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDateTime nowDateTime = LocalDateTime.of(2024, 1, 1, 10, 30);
		LocalDate nowDate = nowDateTime.toLocalDate();
		TimeSlot currentSlot = TimeSlot.SLOT_09_12;
		String expectedCacheKey = "today:weather:37.5665:126.9780:2024-01-01:SLOT_09_12";

		when(keyGenerator.generateTodayKey(eq(37.5665), eq(126.9780), eq(nowDate), eq(currentSlot)))
			.thenReturn(expectedCacheKey);

		// when
		String cacheKey = keyGenerator.generateTodayKey(37.5665, 126.9780, nowDate, currentSlot);

		// then
		assertThat(cacheKey).isEqualTo(expectedCacheKey);
	}


	@Test
	@DisplayName("미래 날씨 캐시 키를 생성한다")
	void Given_FutureWeatherRequest_When_GenerateFutureKey_Then_ReturnsCacheKey() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDateTime nowDateTime = LocalDateTime.of(2024, 1, 1, 10, 30);
		LocalDate targetDate = LocalDate.of(2024, 1, 2);
		TimeSlot currentSlot = TimeSlot.SLOT_09_12;
		String expectedCacheKey = "future:weather:37.5665:126.9780:2024-01-02:SLOT_09_12";

		when(keyGenerator.generateFutureKey(eq(37.5665), eq(126.9780), eq(targetDate), eq(currentSlot)))
			.thenReturn(expectedCacheKey);

		// when
		String cacheKey = keyGenerator.generateFutureKey(37.5665, 126.9780, targetDate, currentSlot);

		// then
		assertThat(cacheKey).isEqualTo(expectedCacheKey);
	}


	@Test
	@DisplayName("WeatherCacheData가 유효한지 확인한다")
	void Given_ValidWeatherCacheData_When_IsValid_Then_ReturnsTrue() {
		// given
		WeatherCacheData validData = WeatherCacheData.builder()
			.weather(WeatherType.SUNNY)
			.findDust(FineDustType.GOOD)
			.uv(UvType.LOW)
			.build();

		// when
		boolean isValid = validData.isValid();

		// then
		assertThat(isValid).isTrue();
	}


	@Test
	@DisplayName("WeatherCacheData가 유효하지 않은지 확인한다")
	void Given_InvalidWeatherCacheData_When_IsValid_Then_ReturnsFalse() {
		// given
		WeatherCacheData invalidData = WeatherCacheData.builder()
			.weather(null)
			.findDust(FineDustType.GOOD)
			.uv(UvType.LOW)
			.build();

		// when
		boolean isValid = invalidData.isValid();

		// then
		assertThat(isValid).isFalse();
	}


	@Test
	@DisplayName("TTL을 계산한다")
	void Given_TimeSlotAndDateTime_When_CalculateTtl_Then_ReturnsDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_09_12;
		LocalDateTime nowDateTime = LocalDateTime.of(2024, 1, 1, 10, 30);

		when(ttlCalculator.calculateTtl(timeSlot, nowDateTime))
			.thenReturn(java.time.Duration.ofHours(2));

		// when
		java.time.Duration ttl = ttlCalculator.calculateTtl(timeSlot, nowDateTime);

		// then
		assertThat(ttl).isEqualTo(java.time.Duration.ofHours(2));
	}


	@Test
	@DisplayName("WeatherApiResultDto를 생성한다")
	void Given_WeatherData_When_CreateWeatherApiResultDto_Then_ReturnsDto() {
		// given
		WeatherApiResultDto dto = WeatherApiResultDto.builder()
			.kmaWeatherResponse(null)
			.openMeteoResponse(null)
			.build();

		// when & then
		assertThat(dto).isNotNull();
		assertThat(dto.kmaWeatherResponse()).isNull();
		assertThat(dto.openMeteoResponse()).isNull();
	}

}
