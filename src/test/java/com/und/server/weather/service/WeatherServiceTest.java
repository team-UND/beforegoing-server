package com.und.server.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.dto.response.WeatherResponse;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.exception.WeatherException;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherService 테스트")
class WeatherServiceTest {

	@Mock
	private WeatherCacheService weatherCacheService;

	@Mock
	private Clock clock;

	@InjectMocks
	private WeatherService weatherService;


	@BeforeEach
	void setUp() {
		// Clock 설정
		when(clock.withZone(ZoneId.of("Asia/Seoul"))).thenReturn(Clock.fixed(
			LocalDate.of(2024, 1, 15).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(),
			ZoneId.of("Asia/Seoul")
		));
	}


	@Test
	@DisplayName("오늘 날씨 정보를 정상적으로 조회한다")
	void Given_TodayWeatherRequest_When_GetWeatherInfo_Then_ReturnsTodayWeather() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate today = LocalDate.of(2024, 1, 15);
		LocalDateTime nowDateTime = LocalDateTime.of(2024, 1, 15, 12, 0);
		WeatherCacheData mockCacheData = WeatherCacheData.builder()
			.weather(WeatherType.SUNNY)
			.fineDust(FineDustType.GOOD)
			.uv(UvType.LOW)
			.build();

		when(weatherCacheService.getTodayWeatherCache(eq(request), any(LocalDateTime.class)))
			.thenReturn(mockCacheData);

		// when
		WeatherResponse response = weatherService.getWeatherInfo(request, today, "Asia/Seoul");

		// then
		assertThat(response).isNotNull();
		assertThat(response.weather()).isEqualTo(WeatherType.SUNNY);
		assertThat(response.fineDust()).isEqualTo(FineDustType.GOOD);
	}


	@Test
	@DisplayName("미래 날씨 정보를 정상적으로 조회한다")
	void Given_FutureWeatherRequest_When_GetWeatherInfo_Then_ReturnsFutureWeather() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate futureDate = LocalDate.of(2024, 1, 16);
		LocalDateTime nowDateTime = LocalDateTime.of(2024, 1, 15, 12, 0);
		WeatherCacheData mockCacheData = WeatherCacheData.builder()
			.weather(WeatherType.CLOUDY)
			.fineDust(FineDustType.NORMAL)
			.uv(UvType.NORMAL)
			.build();

		when(weatherCacheService.getFutureWeatherCache(eq(request), any(LocalDateTime.class), eq(futureDate)))
			.thenReturn(mockCacheData);

		// when
		WeatherResponse response = weatherService.getWeatherInfo(request, futureDate, "Asia/Seoul");

		// then
		assertThat(response).isNotNull();
		assertThat(response.weather()).isEqualTo(WeatherType.CLOUDY);
		assertThat(response.fineDust()).isEqualTo(FineDustType.NORMAL);
	}


	@Test
	@DisplayName("오늘 날씨 캐시가 null일 때 기본값을 반환한다")
	void Given_TodayWeatherCacheIsNull_When_GetWeatherInfo_Then_ReturnsDefault() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate today = LocalDate.of(2024, 1, 15);

		when(weatherCacheService.getTodayWeatherCache(eq(request), any(LocalDateTime.class)))
			.thenReturn(null);

		// when
		WeatherResponse response = weatherService.getWeatherInfo(request, today, "Asia/Seoul");

		// then
		assertThat(response).isNotNull();
		assertThat(response.weather()).isEqualTo(WeatherCacheData.getDefault().weather());
		assertThat(response.fineDust()).isEqualTo(WeatherCacheData.getDefault().fineDust());
	}


	@Test
	@DisplayName("오늘 날씨 캐시가 유효하지 않을 때 유효한 기본값을 반환한다")
	void Given_TodayWeatherCacheIsInvalid_When_GetWeatherInfo_Then_ReturnsValidDefault() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate today = LocalDate.of(2024, 1, 15);
		WeatherCacheData invalidCacheData = WeatherCacheData.builder()
			.weather(null)
			.fineDust(FineDustType.GOOD)
			.uv(UvType.LOW)
			.build();

		when(weatherCacheService.getTodayWeatherCache(eq(request), any(LocalDateTime.class)))
			.thenReturn(invalidCacheData);

		// when
		WeatherResponse response = weatherService.getWeatherInfo(request, today, "Asia/Seoul");

		// then
		assertThat(response).isNotNull();
		assertThat(response.weather()).isEqualTo(invalidCacheData.getValidDefault().weather());
		assertThat(response.fineDust()).isEqualTo(invalidCacheData.getValidDefault().fineDust());
	}


	@Test
	@DisplayName("미래 날씨 캐시가 null일 때 기본값을 반환한다")
	void Given_FutureWeatherCacheIsNull_When_GetWeatherInfo_Then_ReturnsDefault() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate futureDate = LocalDate.of(2024, 1, 16);

		when(weatherCacheService.getFutureWeatherCache(eq(request), any(LocalDateTime.class), eq(futureDate)))
			.thenReturn(null);

		// when
		WeatherResponse response = weatherService.getWeatherInfo(request, futureDate, "Asia/Seoul");

		// then
		assertThat(response).isNotNull();
		assertThat(response.weather()).isEqualTo(WeatherCacheData.getDefault().weather());
		assertThat(response.fineDust()).isEqualTo(WeatherCacheData.getDefault().fineDust());
	}


	@Test
	@DisplayName("미래 날씨 캐시가 유효하지 않을 때 유효한 기본값을 반환한다")
	void Given_FutureWeatherCacheIsInvalid_When_GetWeatherInfo_Then_ReturnsValidDefault() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate futureDate = LocalDate.of(2024, 1, 16);
		WeatherCacheData invalidCacheData = WeatherCacheData.builder()
			.weather(WeatherType.CLOUDY)
			.fineDust(null)
			.uv(UvType.NORMAL)
			.build();

		when(weatherCacheService.getFutureWeatherCache(eq(request), any(LocalDateTime.class), eq(futureDate)))
			.thenReturn(invalidCacheData);

		// when
		WeatherResponse response = weatherService.getWeatherInfo(request, futureDate, "Asia/Seoul");

		// then
		assertThat(response).isNotNull();
		assertThat(response.weather()).isEqualTo(invalidCacheData.getValidDefault().weather());
		assertThat(response.fineDust()).isEqualTo(invalidCacheData.getValidDefault().fineDust());
	}


	@Test
	@DisplayName("위도가 -90보다 작을 때 예외를 발생시킨다")
	void Given_LatitudeLessThanMinus90_When_GetWeatherInfo_Then_ThrowsException() {
		// given
		WeatherRequest request = new WeatherRequest(-91.0, 126.9780);
		LocalDate today = LocalDate.of(2024, 1, 15);

		// when & then
		assertThatThrownBy(() -> weatherService.getWeatherInfo(request, today, "Asia/Seoul"))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.INVALID_COORDINATES);
	}


	@Test
	@DisplayName("위도가 90보다 클 때 예외를 발생시킨다")
	void Given_LatitudeGreaterThan90_When_GetWeatherInfo_Then_ThrowsException() {
		// given
		WeatherRequest request = new WeatherRequest(91.0, 126.9780);
		LocalDate today = LocalDate.of(2024, 1, 15);

		// when & then
		assertThatThrownBy(() -> weatherService.getWeatherInfo(request, today, "Asia/Seoul"))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.INVALID_COORDINATES);
	}


	@Test
	@DisplayName("경도가 -180보다 작을 때 예외를 발생시킨다")
	void Given_LongitudeLessThanMinus180_When_GetWeatherInfo_Then_ThrowsException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, -181.0);
		LocalDate today = LocalDate.of(2024, 1, 15);

		// when & then
		assertThatThrownBy(() -> weatherService.getWeatherInfo(request, today, "Asia/Seoul"))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.INVALID_COORDINATES);
	}


	@Test
	@DisplayName("경도가 180보다 클 때 예외를 발생시킨다")
	void Given_LongitudeGreaterThan180_When_GetWeatherInfo_Then_ThrowsException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 181.0);
		LocalDate today = LocalDate.of(2024, 1, 15);

		// when & then
		assertThatThrownBy(() -> weatherService.getWeatherInfo(request, today, "Asia/Seoul"))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.INVALID_COORDINATES);
	}


	@Test
	@DisplayName("요청 날짜가 오늘보다 이전일 때 예외를 발생시킨다")
	void Given_DateBeforeToday_When_GetWeatherInfo_Then_ThrowsException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate yesterday = LocalDate.of(2024, 1, 14);

		// when & then
		assertThatThrownBy(() -> weatherService.getWeatherInfo(request, yesterday, "Asia/Seoul"))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.DATE_OUT_OF_RANGE);
	}


	@Test
	@DisplayName("요청 날짜가 최대 허용 날짜보다 이후일 때 예외를 발생시킨다")
	void Given_DateAfterMaxDate_When_GetWeatherInfo_Then_ThrowsException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate maxDatePlusOne = LocalDate.of(2024, 1, 19); // MAX_FUTURE_DATE + 1

		// when & then
		assertThatThrownBy(() -> weatherService.getWeatherInfo(request, maxDatePlusOne, "Asia/Seoul"))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.DATE_OUT_OF_RANGE);
	}


	@Test
	@DisplayName("유효한 좌표와 날짜로 날씨 정보를 조회한다")
	void Given_ValidCoordinatesAndDate_When_GetWeatherInfo_Then_ReturnsWeatherInfo() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate today = LocalDate.of(2024, 1, 15);
		WeatherCacheData mockCacheData = WeatherCacheData.builder()
			.weather(WeatherType.RAIN)
			.fineDust(FineDustType.BAD)
			.uv(UvType.HIGH)
			.build();

		when(weatherCacheService.getTodayWeatherCache(eq(request), any(LocalDateTime.class)))
			.thenReturn(mockCacheData);

		// when
		WeatherResponse response = weatherService.getWeatherInfo(request, today, "Asia/Seoul");

		// then
		assertThat(response).isNotNull();
		assertThat(response.weather()).isEqualTo(WeatherType.RAIN);
		assertThat(response.fineDust()).isEqualTo(FineDustType.BAD);
	}


	@Test
	@DisplayName("최대 허용 날짜로 날씨 정보를 조회한다")
	void Given_MaxAllowedDate_When_GetWeatherInfo_Then_ReturnsWeatherInfo() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate maxDate = LocalDate.of(2024, 1, 18); // MAX_FUTURE_DATE
		WeatherCacheData mockCacheData = WeatherCacheData.builder()
			.weather(WeatherType.SNOW)
			.fineDust(FineDustType.VERY_BAD)
			.uv(UvType.VERY_LOW)
			.build();

		when(weatherCacheService.getFutureWeatherCache(eq(request), any(LocalDateTime.class), eq(maxDate)))
			.thenReturn(mockCacheData);

		// when
		WeatherResponse response = weatherService.getWeatherInfo(request, maxDate, "Asia/Seoul");

		// then
		assertThat(response).isNotNull();
		assertThat(response.weather()).isEqualTo(WeatherType.SNOW);
		assertThat(response.fineDust()).isEqualTo(FineDustType.VERY_BAD);
	}

}
