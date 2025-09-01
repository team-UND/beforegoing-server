package com.und.server.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.dto.OpenMeteoWeatherApiResultDto;
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.exception.KmaApiException;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.exception.WeatherException;
import com.und.server.weather.infrastructure.KmaApiFacade;
import com.und.server.weather.infrastructure.OpenMeteoApiFacade;
import com.und.server.weather.infrastructure.dto.KmaWeatherResponse;
import com.und.server.weather.infrastructure.dto.OpenMeteoResponse;
import com.und.server.weather.infrastructure.dto.OpenMeteoWeatherResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherApiService 테스트")
class WeatherApiServiceTest {

	@Mock
	private KmaApiFacade kmaApiFacade;

	@Mock
	private OpenMeteoApiFacade openMeteoApiFacade;

	@Mock
	private Executor weatherExecutor;

	@InjectMocks
	private WeatherApiService weatherApiService;


	@BeforeEach
	void setUp() {
		// CompletableFuture.supplyAsync를 동기적으로 실행하도록 설정
		doAnswer(invocation -> {
			Runnable runnable = invocation.getArgument(0);
			runnable.run();
			return null;
		}).when(weatherExecutor).execute(any(Runnable.class));
	}


	@Test
	@DisplayName("오늘 날씨 API를 정상적으로 호출한다")
	void Given_TodayWeatherRequest_When_CallTodayWeather_Then_ReturnsWeatherApiResult() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		TimeSlot timeSlot = TimeSlot.SLOT_09_12;
		LocalDate today = LocalDate.now();

		KmaWeatherResponse mockKmaResponse = new KmaWeatherResponse(null);
		OpenMeteoResponse mockOpenMeteoResponse = new OpenMeteoResponse(
			37.5665, 126.9780, "Asia/Seoul", null, null);

		when(kmaApiFacade.callWeatherApi(any(GridPoint.class), eq(timeSlot), eq(today)))
			.thenReturn(mockKmaResponse);
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (today)))
			.thenReturn(mockOpenMeteoResponse);

		// when
		WeatherApiResultDto result = weatherApiService.callTodayWeather(request, timeSlot, today);

		// then
		assertThat(result).isNotNull();
	}


	@Test
	@DisplayName("미래 날씨 API를 정상적으로 호출한다")
	void Given_FutureWeatherRequest_When_CallFutureWeather_Then_ReturnsWeatherApiResult() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		TimeSlot timeSlot = TimeSlot.SLOT_15_18;
		LocalDate today = LocalDate.now();
		LocalDate targetDate = LocalDate.now().plusDays(1);

		KmaWeatherResponse mockKmaResponse = new KmaWeatherResponse(null);
		OpenMeteoResponse mockOpenMeteoResponse = new OpenMeteoResponse(
			37.5665, 126.9780, "Asia/Seoul", null, null);

		when(kmaApiFacade.callWeatherApi(any(GridPoint.class), eq(timeSlot), eq(today)))
			.thenReturn(mockKmaResponse);
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (targetDate)))
			.thenReturn(mockOpenMeteoResponse);

		// when
		WeatherApiResultDto result = weatherApiService.callFutureWeather(request, timeSlot, today, targetDate);

		// then
		assertThat(result).isNotNull();
	}


	@Test
	@DisplayName("OpenMeteo 폴백 날씨 API를 정상적으로 호출한다")
	void Given_OpenMeteoFallbackRequest_When_CallOpenMeteoFallBackWeather_Then_ReturnsOpenMeteoWeatherApiResult() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate targetDate = LocalDate.now().plusDays(2);

		OpenMeteoWeatherResponse mockWeatherResponse =
			new OpenMeteoWeatherResponse(
				37.5665, 126.9780, "Asia/Seoul", null, null);
		OpenMeteoResponse mockDustUvResponse = new OpenMeteoResponse(
			37.5665, 126.9780, "Asia/Seoul", null, null);

		when(openMeteoApiFacade.callWeatherApi((37.5665), (126.9780), (targetDate)))
			.thenReturn(mockWeatherResponse);
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (targetDate)))
			.thenReturn(mockDustUvResponse);

		// when
		OpenMeteoWeatherApiResultDto result = weatherApiService.callOpenMeteoFallBackWeather(request, targetDate);

		// then
		assertThat(result).isNotNull();
	}


	@Test
	@DisplayName("KMA API 타임아웃 시 KmaApiException을 발생시킨다")
	void Given_KmaApiTimeout_When_CallTodayWeather_Then_ThrowsKmaApiException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		TimeSlot timeSlot = TimeSlot.SLOT_09_12;
		LocalDate today = LocalDate.now();

		when(kmaApiFacade.callWeatherApi(any(GridPoint.class), eq(timeSlot), eq(today)))
			.thenThrow(new CompletionException(new TimeoutException("API timeout")));
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (today)))
			.thenReturn(new OpenMeteoResponse(
				37.5665, 126.9780, "Asia/Seoul", null, null));

		// when & then
		assertThatThrownBy(() -> weatherApiService.callTodayWeather(request, timeSlot, today))
			.isInstanceOf(KmaApiException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.KMA_TIMEOUT);
	}


	@Test
	@DisplayName("WeatherException이 발생하면 그대로 전파한다")
	void Given_WeatherException_When_CallTodayWeather_Then_ThrowsWeatherException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		TimeSlot timeSlot = TimeSlot.SLOT_09_12;
		LocalDate today = LocalDate.now();

		WeatherException expectedException = new WeatherException(WeatherErrorResult.INVALID_COORDINATES);
		when(kmaApiFacade.callWeatherApi(any(GridPoint.class), eq(timeSlot), eq(today)))
			.thenThrow(new CompletionException(expectedException));
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (today)))
			.thenReturn(new OpenMeteoResponse(
				37.5665, 126.9780, "Asia/Seoul", null, null));

		// when & then
		assertThatThrownBy(() -> weatherApiService.callTodayWeather(request, timeSlot, today))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.INVALID_COORDINATES);
	}


	@Test
	@DisplayName("예상치 못한 예외 발생 시 WeatherException을 발생시킨다")
	void Given_UnexpectedException_When_CallTodayWeather_Then_ThrowsWeatherException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		TimeSlot timeSlot = TimeSlot.SLOT_09_12;
		LocalDate today = LocalDate.now();

		RuntimeException unexpectedException = new RuntimeException("Unexpected error");
		when(kmaApiFacade.callWeatherApi(any(GridPoint.class), eq(timeSlot), eq(today)))
			.thenThrow(new CompletionException(unexpectedException));
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (today)))
			.thenReturn(new OpenMeteoResponse(
				37.5665, 126.9780, "Asia/Seoul", null, null));

		// when & then
		assertThatThrownBy(() -> weatherApiService.callTodayWeather(request, timeSlot, today))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.WEATHER_SERVICE_ERROR);
	}


	@Test
	@DisplayName("일반 예외 발생 시 WeatherException을 발생시킨다")
	void Given_GeneralException_When_CallTodayWeather_Then_ThrowsWeatherException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		TimeSlot timeSlot = TimeSlot.SLOT_09_12;
		LocalDate today = LocalDate.now();

		Exception generalException = new Exception("General error");
		when(kmaApiFacade.callWeatherApi(any(GridPoint.class), eq(timeSlot), eq(today)))
			.thenThrow(new CompletionException(generalException));

		// when & then
		assertThatThrownBy(() -> weatherApiService.callTodayWeather(request, timeSlot, today))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.WEATHER_SERVICE_ERROR);
	}


	@Test
	@DisplayName("미래 날씨 API에서 타임아웃 시 KmaApiException을 발생시킨다")
	void Given_KmaApiTimeout_When_CallFutureWeather_Then_ThrowsKmaApiException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		TimeSlot timeSlot = TimeSlot.SLOT_15_18;
		LocalDate today = LocalDate.now();
		LocalDate targetDate = LocalDate.now().plusDays(1);

		when(kmaApiFacade.callWeatherApi(any(GridPoint.class), eq(timeSlot), eq(today)))
			.thenThrow(new CompletionException(new TimeoutException("API timeout")));
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (targetDate)))
			.thenReturn(new OpenMeteoResponse(
				37.5665, 126.9780, "Asia/Seoul", null, null));

		// when & then
		assertThatThrownBy(() -> weatherApiService.callFutureWeather(request, timeSlot, today, targetDate))
			.isInstanceOf(KmaApiException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.KMA_TIMEOUT);
	}


	@Test
	@DisplayName("OpenMeteo 폴백에서 WeatherException이 발생하면 그대로 전파한다")
	void Given_WeatherException_When_CallOpenMeteoFallBackWeather_Then_ThrowsWeatherException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate targetDate = LocalDate.now().plusDays(2);

		WeatherException expectedException = new WeatherException(WeatherErrorResult.INVALID_COORDINATES);
		when(openMeteoApiFacade.callWeatherApi((37.5665), (126.9780), (targetDate)))
			.thenThrow(new CompletionException(expectedException));
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (targetDate)))
			.thenReturn(new OpenMeteoResponse(37.5665, 126.9780, "Asia/Seoul", null, null));

		// when & then
		assertThatThrownBy(() -> weatherApiService.callOpenMeteoFallBackWeather(request, targetDate))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.INVALID_COORDINATES);
	}


	@Test
	@DisplayName("OpenMeteo 폴백에서 예상치 못한 예외 발생 시 WeatherException을 발생시킨다")
	void Given_UnexpectedException_When_CallOpenMeteoFallBackWeather_Then_ThrowsWeatherException() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate targetDate = LocalDate.now().plusDays(2);

		RuntimeException unexpectedException = new RuntimeException("Unexpected error");
		when(openMeteoApiFacade.callWeatherApi((37.5665), (126.9780), (targetDate)))
			.thenThrow(new CompletionException(unexpectedException));
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (targetDate)))
			.thenReturn(new OpenMeteoResponse(
				37.5665, 126.9780, "Asia/Seoul", null, null));

		// when & then
		assertThatThrownBy(() -> weatherApiService.callOpenMeteoFallBackWeather(request, targetDate))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.WEATHER_SERVICE_ERROR);
	}


	@Test
	@DisplayName("다른 시간대에서도 정상적으로 API를 호출한다")
	void Given_DifferentTimeSlot_When_CallTodayWeather_Then_ReturnsWeatherApiResult() {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		TimeSlot timeSlot = TimeSlot.SLOT_21_24;
		LocalDate today = LocalDate.now();

		KmaWeatherResponse mockKmaResponse = new KmaWeatherResponse(null);
		OpenMeteoResponse mockOpenMeteoResponse = new OpenMeteoResponse(37.5665, 126.9780, "Asia/Seoul", null, null);

		when(kmaApiFacade.callWeatherApi(any(GridPoint.class), eq(timeSlot), eq(today)))
			.thenReturn(mockKmaResponse);
		when(openMeteoApiFacade.callDustUvApi((37.5665), (126.9780), (today)))
			.thenReturn(mockOpenMeteoResponse);

		// when
		WeatherApiResultDto result = weatherApiService.callTodayWeather(request, timeSlot, today);

		// then
		assertThat(result).isNotNull();
	}


	@Test
	@DisplayName("다른 좌표에서도 정상적으로 API를 호출한다")
	void Given_DifferentCoordinates_When_CallTodayWeather_Then_ReturnsWeatherApiResult() {
		// given
		WeatherRequest request = new WeatherRequest(35.1796, 129.0756); // 부산
		TimeSlot timeSlot = TimeSlot.SLOT_03_06;
		LocalDate today = LocalDate.now();

		KmaWeatherResponse mockKmaResponse = new KmaWeatherResponse(null);
		OpenMeteoResponse mockOpenMeteoResponse = new OpenMeteoResponse(
			35.1796, 129.0756, "Asia/Seoul", null, null);

		when(kmaApiFacade.callWeatherApi(any(GridPoint.class), eq(timeSlot), eq(today)))
			.thenReturn(mockKmaResponse);
		when(openMeteoApiFacade.callDustUvApi((35.1796), (129.0756), (today)))
			.thenReturn(mockOpenMeteoResponse);

		// when
		WeatherApiResultDto result = weatherApiService.callTodayWeather(request, timeSlot, today);

		// then
		assertThat(result).isNotNull();
	}

}
