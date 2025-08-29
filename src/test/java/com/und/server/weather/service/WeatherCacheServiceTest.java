package com.und.server.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.OpenMeteoWeatherApiResultDto;
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.exception.KmaApiException;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.util.CacheSerializer;
import com.und.server.weather.util.WeatherKeyGenerator;
import com.und.server.weather.util.WeatherTtlCalculator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("WeatherCacheService 테스트")
class WeatherCacheServiceTest {

	@Mock
	private RedisTemplate<String, String> redisTemplate;
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

	private final WeatherRequest request = new WeatherRequest(37.5, 127.0);

	@Mock
	private HashOperations hashOperations;
	@Mock
	private ValueOperations valueOperations;

	@BeforeEach
	void setUp() {
		when(redisTemplate.opsForHash()).thenReturn(hashOperations);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	@DisplayName("오늘 날씨 캐시 키를 생성한다")
	void Given_TodayWeatherRequest_When_GenerateTodayKey_Then_ReturnsCacheKey() {
		// given
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
		WeatherCacheData validData = WeatherCacheData.from(
			WeatherType.SUNNY, FineDustType.GOOD, UvType.LOW
		);
		assertThat(validData.isValid()).isTrue();
	}

	@Test
	@DisplayName("WeatherCacheData가 유효하지 않은지 확인한다")
	void Given_InvalidWeatherCacheData_When_IsValid_Then_ReturnsFalse() {
		WeatherCacheData invalidData = WeatherCacheData.from(
			null, FineDustType.GOOD, UvType.LOW
		);
		assertThat(invalidData.isValid()).isFalse();
	}

	@Test
	@DisplayName("TTL을 계산한다")
	void Given_TimeSlotAndDateTime_When_CalculateTtl_Then_ReturnsDuration() {
		TimeSlot timeSlot = TimeSlot.SLOT_09_12;
		LocalDateTime nowDateTime = LocalDateTime.of(2024, 1, 1, 10, 30);

		when(ttlCalculator.calculateTtl(timeSlot, nowDateTime))
			.thenReturn(Duration.ofHours(2));

		Duration ttl = ttlCalculator.calculateTtl(timeSlot, nowDateTime);

		assertThat(ttl).isEqualTo(Duration.ofHours(2));
	}

	@Test
	@DisplayName("캐시에 유효한 today 데이터가 있으면 그대로 반환한다")
	void Given_CacheExists_When_GetTodayWeatherCache_Then_ReturnCachedData() {
		LocalDateTime now = LocalDateTime.of(2024, 1, 1, 9, 0);
		String cacheKey = "todayKey";
		String hourKey = "09";

		WeatherCacheData cachedData = WeatherCacheData.from(
			WeatherType.SUNNY, FineDustType.GOOD, UvType.LOW
		);

		given(keyGenerator.generateTodayKey(any(), any(), any(), any())).willReturn(cacheKey);
		given(keyGenerator.generateTodayHourFieldKey(any())).willReturn(hourKey);
		given(hashOperations.get(cacheKey, hourKey)).willReturn("json");
		given(cacheSerializer.deserializeWeatherCacheDataFromHash("json")).willReturn(cachedData);

		WeatherCacheData result = weatherCacheService.getTodayWeatherCache(request, now);

		assertThat(result).isEqualTo(cachedData);
	}

	@Test
	@DisplayName("캐시에 데이터 없으면 API 호출 후 저장한다")
	void Given_NoCache_When_GetTodayWeatherCache_Then_CallApiAndSave() {
		LocalDateTime now = LocalDateTime.of(2024, 1, 1, 9, 0);
		String cacheKey = "todayKey";
		String hourKey = "09";

		WeatherCacheData newData = WeatherCacheData.getDefault();
		Map<String, WeatherCacheData> newMap = Map.of(hourKey, newData);

		given(keyGenerator.generateTodayKey(any(), any(), any(), any())).willReturn(cacheKey);
		given(keyGenerator.generateTodayHourFieldKey(any())).willReturn(hourKey);
		given(hashOperations.get(cacheKey, hourKey)).willReturn(null);

		given(weatherApiService.callTodayWeather(any(), any(), any()))
			.willReturn(mock(WeatherApiResultDto.class));
		given(weatherDecisionService.getTodayWeatherCacheData(any(), any(), any())).willReturn(newMap);
		given(ttlCalculator.calculateTtl(any(), any())).willReturn(Duration.ofMinutes(10));
		given(cacheSerializer.serializeWeatherCacheDataToHash(any())).willReturn(Map.of(hourKey, "{}"));

		WeatherCacheData result = weatherCacheService.getTodayWeatherCache(request, now);

		assertThat(result).isEqualTo(newData);
		verify(redisTemplate).expire(eq(cacheKey), any());
	}

	@Test
	@DisplayName("KMA API 실패시 OpenMeteo fallback 사용한다")
	void Given_KmaFails_When_GetTodayWeatherCache_Then_UseFallback() {
		LocalDateTime now = LocalDateTime.of(2024, 1, 1, 9, 0);
		String cacheKey = "todayKey";
		String hourKey = "09";
		WeatherCacheData fallbackData = WeatherCacheData.getDefault();
		Map<String, WeatherCacheData> map = Map.of(hourKey, fallbackData);

		given(keyGenerator.generateTodayKey(any(), any(), any(), any())).willReturn(cacheKey);
		given(keyGenerator.generateTodayHourFieldKey(any())).willReturn(hourKey);
		given(hashOperations.get(cacheKey, hourKey)).willReturn(null);

		given(weatherApiService.callTodayWeather(any(), any(), any()))
			.willThrow(new KmaApiException(WeatherErrorResult.KMA_TIMEOUT, new RuntimeException()));

		given(weatherApiService.callOpenMeteoFallBackWeather(any(), any()))
			.willReturn(mock(OpenMeteoWeatherApiResultDto.class));

		given(weatherDecisionService.getTodayWeatherCacheDataFallback(any(), any(), any())).willReturn(map);
		given(ttlCalculator.calculateTtl(any(), any())).willReturn(Duration.ofMinutes(5));
		given(cacheSerializer.serializeWeatherCacheDataToHash(any())).willReturn(Map.of(hourKey, "{}"));

		// when
		WeatherCacheData result = weatherCacheService.getTodayWeatherCache(request, now);

		// then
		assertThat(result).isEqualTo(fallbackData);
	}


	@Test
	@DisplayName("Future 캐시 조회 시 캐시에 있으면 그대로 반환한다")
	void Given_CacheExists_When_GetFutureWeatherCache_Then_ReturnCachedData() {
		LocalDateTime now = LocalDateTime.of(2024, 1, 1, 9, 0);
		LocalDate targetDate = LocalDate.of(2024, 1, 2);
		String cacheKey = "futureKey";

		WeatherCacheData cachedData = WeatherCacheData.from(
			WeatherType.CLOUDY, FineDustType.NORMAL, UvType.HIGH
		);

		given(keyGenerator.generateFutureKey(any(), any(), any(), any())).willReturn(cacheKey);
		given(valueOperations.get(cacheKey)).willReturn("json");
		given(cacheSerializer.deserializeWeatherCacheData("json")).willReturn(cachedData);

		WeatherCacheData result = weatherCacheService.getFutureWeatherCache(request, now, targetDate);

		assertThat(result).isEqualTo(cachedData);
	}

	@Test
	@DisplayName("캐시에 유효하지 않은 today 데이터면 API 호출로 대체한다")
	void Given_InvalidCache_When_GetTodayWeatherCache_Then_CallApi() {
		LocalDateTime now = LocalDateTime.of(2024, 1, 1, 9, 0);
		String cacheKey = "todayKey";
		String hourKey = "09";

		WeatherCacheData invalidData = WeatherCacheData.from(null, FineDustType.GOOD, UvType.LOW);

		given(keyGenerator.generateTodayKey(any(), any(), any(), any())).willReturn(cacheKey);
		given(keyGenerator.generateTodayHourFieldKey(any())).willReturn(hourKey);
		given(hashOperations.get(cacheKey, hourKey)).willReturn("json");
		given(cacheSerializer.deserializeWeatherCacheDataFromHash("json")).willReturn(invalidData);

		// API 대체 호출
		WeatherCacheData newData = WeatherCacheData.getDefault();
		Map<String, WeatherCacheData> map = Map.of(hourKey, newData);
		given(weatherApiService.callTodayWeather(any(), any(), any())).willReturn(mock(WeatherApiResultDto.class));
		given(weatherDecisionService.getTodayWeatherCacheData(any(), any(), any())).willReturn(map);
		given(ttlCalculator.calculateTtl(any(), any())).willReturn(Duration.ofMinutes(10));
		given(cacheSerializer.serializeWeatherCacheDataToHash(any())).willReturn(Map.of(hourKey, "{}"));

		WeatherCacheData result = weatherCacheService.getTodayWeatherCache(request, now);

		assertThat(result).isEqualTo(newData);
	}

	@Test
	@DisplayName("Future 캐시 없으면 API 호출 후 저장한다")
	void Given_NoCache_When_GetFutureWeatherCache_Then_CallApiAndSave() {
		LocalDateTime now = LocalDateTime.of(2024, 1, 1, 9, 0);
		LocalDate targetDate = LocalDate.of(2024, 1, 2);
		String cacheKey = "futureKey";

		given(keyGenerator.generateFutureKey(any(), any(), any(), any())).willReturn(cacheKey);
		given(valueOperations.get(cacheKey)).willReturn(null);

		WeatherCacheData newData = WeatherCacheData.getDefault();
		given(weatherApiService.callFutureWeather(any(), any(), any(), any())).willReturn(
			mock(WeatherApiResultDto.class));
		given(weatherDecisionService.getFutureWeatherCacheData(any(), any())).willReturn(newData);
		given(ttlCalculator.calculateTtl(any(), any())).willReturn(Duration.ofMinutes(5));
		given(cacheSerializer.serializeWeatherCacheData(any())).willReturn("{}");

		WeatherCacheData result = weatherCacheService.getFutureWeatherCache(request, now, targetDate);

		assertThat(result).isEqualTo(newData);

		verify(redisTemplate, times(2)).opsForValue();
		verify(valueOperations).set(eq(cacheKey), any(), eq(Duration.ofMinutes(5)));
	}


	@Test
	@DisplayName("Future 캐시 조회시 KMA 실패하면 fallback 호출한다")
	void Given_KmaFails_When_GetFutureWeatherCache_Then_UseFallback() {
		LocalDateTime now = LocalDateTime.of(2024, 1, 1, 9, 0);
		LocalDate targetDate = LocalDate.of(2024, 1, 2);
		String cacheKey = "futureKey";

		given(keyGenerator.generateFutureKey(any(), any(), any(), any())).willReturn(cacheKey);
		given(valueOperations.get(cacheKey)).willReturn(null);

		given(weatherApiService.callFutureWeather(any(), any(), any(), any()))
			.willThrow(new KmaApiException(WeatherErrorResult.KMA_TIMEOUT, new RuntimeException()));

		WeatherCacheData fallbackData = WeatherCacheData.getDefault();
		given(weatherApiService.callOpenMeteoFallBackWeather(any(), any())).willReturn(mock());
		given(weatherDecisionService.getFutureWeatherCacheDataFallback(any(), any())).willReturn(fallbackData);
		given(ttlCalculator.calculateTtl(any(), any())).willReturn(Duration.ofMinutes(5));
		given(cacheSerializer.serializeWeatherCacheData(any())).willReturn("{}");

		WeatherCacheData result = weatherCacheService.getFutureWeatherCache(request, now, targetDate);

		assertThat(result).isEqualTo(fallbackData);
	}


}
