package com.und.server.weather.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.exception.WeatherException;
import com.und.server.weather.infrastructure.client.OpenMeteoClient;
import com.und.server.weather.infrastructure.client.OpenMeteoKmaClient;
import com.und.server.weather.infrastructure.dto.OpenMeteoResponse;
import com.und.server.weather.infrastructure.dto.OpenMeteoWeatherResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenMeteoApiFacade 테스트")
class OpenMeteoApiFacadeTest {

	@Mock
	private OpenMeteoClient openMeteoClient;

	@Mock
	private OpenMeteoKmaClient openMeteoKmaClient;

	@InjectMocks
	private OpenMeteoApiFacade openMeteoApiFacade;

	private Double latitude;
	private Double longitude;
	private LocalDate date;

	@BeforeEach
	void setUp() {
		latitude = 37.5665;
		longitude = 126.9780;
		date = LocalDate.of(2024, 1, 15);
	}

	@Test
	@DisplayName("OpenMeteo Dust/UV API 호출이 성공한다")
	void Given_ValidRequest_When_CallDustUvApi_Then_ReturnsOpenMeteoResponse() {
		// given
		OpenMeteoResponse expectedResponse = createMockOpenMeteoResponse();
		String expectedVariables = String.join(",", FineDustType.OPEN_METEO_VARIABLES, UvType.OPEN_METEO_VARIABLES);

		given(openMeteoClient.getForecast(
			eq(latitude), eq(longitude), eq(expectedVariables),
			eq("2024-01-15"), eq("2024-01-15"), eq("Asia/Seoul")
		)).willReturn(expectedResponse);

		// when
		OpenMeteoResponse result = openMeteoApiFacade.callDustUvApi(latitude, longitude, date);

		// then
		assertThat(result).isEqualTo(expectedResponse);
		verify(openMeteoClient, times(1)).getForecast(
			eq(latitude), eq(longitude), eq(expectedVariables),
			eq("2024-01-15"), eq("2024-01-15"), eq("Asia/Seoul")
		);
	}

	@Test
	@DisplayName("다른 좌표로 OpenMeteo Dust/UV API 호출이 성공한다")
	void Given_DifferentCoordinates_When_CallDustUvApi_Then_ReturnsOpenMeteoResponse() {
		// given
		Double differentLat = 35.1796;
		Double differentLon = 129.0756;
		OpenMeteoResponse expectedResponse = createMockOpenMeteoResponse();
		String expectedVariables = String.join(",", FineDustType.OPEN_METEO_VARIABLES, UvType.OPEN_METEO_VARIABLES);

		given(openMeteoClient.getForecast(
			eq(differentLat), eq(differentLon), eq(expectedVariables),
			eq("2024-01-15"), eq("2024-01-15"), eq("Asia/Seoul")
		)).willReturn(expectedResponse);

		// when
		OpenMeteoResponse result = openMeteoApiFacade.callDustUvApi(differentLat, differentLon, date);

		// then
		assertThat(result).isEqualTo(expectedResponse);
		verify(openMeteoClient, times(1)).getForecast(
			eq(differentLat), eq(differentLon), eq(expectedVariables),
			eq("2024-01-15"), eq("2024-01-15"), eq("Asia/Seoul")
		);
	}

	@Test
	@DisplayName("다른 날짜로 OpenMeteo Dust/UV API 호출이 성공한다")
	void Given_DifferentDate_When_CallDustUvApi_Then_ReturnsOpenMeteoResponse() {
		// given
		LocalDate differentDate = LocalDate.of(2024, 12, 25);
		OpenMeteoResponse expectedResponse = createMockOpenMeteoResponse();
		String expectedVariables = String.join(",", FineDustType.OPEN_METEO_VARIABLES, UvType.OPEN_METEO_VARIABLES);

		given(openMeteoClient.getForecast(
			eq(latitude), eq(longitude), eq(expectedVariables),
			eq("2024-12-25"), eq("2024-12-25"), eq("Asia/Seoul")
		)).willReturn(expectedResponse);

		// when
		OpenMeteoResponse result = openMeteoApiFacade.callDustUvApi(latitude, longitude, differentDate);

		// then
		assertThat(result).isEqualTo(expectedResponse);
		verify(openMeteoClient, times(1)).getForecast(
			eq(latitude), eq(longitude), eq(expectedVariables),
			eq("2024-12-25"), eq("2024-12-25"), eq("Asia/Seoul")
		);
	}

	@Test
	@DisplayName("OpenMeteo Weather API 호출이 성공한다")
	void Given_ValidRequest_When_CallWeatherApi_Then_ReturnsOpenMeteoWeatherResponse() {
		// given
		OpenMeteoWeatherResponse expectedResponse = createMockOpenMeteoWeatherResponse();

		given(openMeteoKmaClient.getWeatherForecast(
			eq(latitude), eq(longitude), eq(WeatherType.OPEN_METEO_VARIABLES),
			eq("2024-01-15"), eq("2024-01-15"), eq("Asia/Seoul")
		)).willReturn(expectedResponse);

		// when
		OpenMeteoWeatherResponse result = openMeteoApiFacade.callWeatherApi(latitude, longitude, date);

		// then
		assertThat(result).isEqualTo(expectedResponse);
		verify(openMeteoKmaClient, times(1)).getWeatherForecast(
			eq(latitude), eq(longitude), eq(WeatherType.OPEN_METEO_VARIABLES),
			eq("2024-01-15"), eq("2024-01-15"), eq("Asia/Seoul")
		);
	}

	@Test
	@DisplayName("ResourceAccessException 발생시 OPEN_METEO_TIMEOUT 예외를 던진다")
	void Given_ResourceAccessException_When_CallDustUvApi_Then_ThrowsOpenMeteoTimeoutException() {
		// given
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(new ResourceAccessException("Connection timeout"));

		// when & then
		assertThatThrownBy(() -> openMeteoApiFacade.callDustUvApi(latitude, longitude, date))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.OPEN_METEO_TIMEOUT);
	}


	@Test
	@DisplayName("RestClientResponseException 429 발생시 OPEN_METEO_RATE_LIMIT 예외를 던진다")
	void Given_RestClientResponseException429_When_CallDustUvApi_Then_ThrowsOpenMeteoRateLimitException() {
		// given
		RestClientResponseException rateLimitException = new RestClientResponseException(
			"Rate limit exceeded", 429, "Too Many Requests", null, null, null
		);
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(rateLimitException);

		// when & then
		assertThatThrownBy(() -> openMeteoApiFacade.callDustUvApi(latitude, longitude, date))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.OPEN_METEO_RATE_LIMIT);
	}

	@Test
	@DisplayName("RestClientResponseException 기타 상태코드 발생시 OPEN_METEO_API_ERROR 예외를 던진다")
	void Given_RestClientResponseExceptionOtherStatus_When_CallDustUvApi_Then_ThrowsOpenMeteoApiErrorException() {
		// given
		RestClientResponseException otherException = new RestClientResponseException(
			"Other error", 500, "Internal Server Error", null, null, null
		);
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(otherException);

		// when & then
		assertThatThrownBy(() -> openMeteoApiFacade.callDustUvApi(latitude, longitude, date))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.OPEN_METEO_API_ERROR);
	}

	@Test
	@DisplayName("기타 Exception 발생시 OPEN_METEO_API_ERROR 예외를 던진다")
	void Given_GenericException_When_CallDustUvApi_Then_ThrowsOpenMeteoApiErrorException() {
		// given
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(new RuntimeException("Unexpected error"));

		// when & then
		assertThatThrownBy(() -> openMeteoApiFacade.callDustUvApi(latitude, longitude, date))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.OPEN_METEO_API_ERROR);
	}

	@Test
	@DisplayName("OpenMeteo Weather API에서 Exception 발생시 OPEN_METEO_API_ERROR 예외를 던진다")
	void Given_Exception_When_CallWeatherApi_Then_ThrowsOpenMeteoApiErrorException() {
		// given
		given(openMeteoKmaClient.getWeatherForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(new RuntimeException("Weather API error"));

		// when & then
		assertThatThrownBy(() -> openMeteoApiFacade.callWeatherApi(latitude, longitude, date))
			.isInstanceOf(WeatherException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.OPEN_METEO_API_ERROR);
	}

	@Test
	@DisplayName("다른 좌표로 OpenMeteo Weather API 호출이 성공한다")
	void Given_DifferentCoordinates_When_CallWeatherApi_Then_ReturnsOpenMeteoWeatherResponse() {
		// given
		Double differentLat = 35.1796;
		Double differentLon = 129.0756;
		OpenMeteoWeatherResponse expectedResponse = createMockOpenMeteoWeatherResponse();

		given(openMeteoKmaClient.getWeatherForecast(
			eq(differentLat), eq(differentLon), eq(WeatherType.OPEN_METEO_VARIABLES),
			eq("2024-01-15"), eq("2024-01-15"), eq("Asia/Seoul")
		)).willReturn(expectedResponse);

		// when
		OpenMeteoWeatherResponse result = openMeteoApiFacade.callWeatherApi(differentLat, differentLon, date);

		// then
		assertThat(result).isEqualTo(expectedResponse);
		verify(openMeteoKmaClient, times(1)).getWeatherForecast(
			eq(differentLat), eq(differentLon), eq(WeatherType.OPEN_METEO_VARIABLES),
			eq("2024-01-15"), eq("2024-01-15"), eq("Asia/Seoul")
		);
	}

	@Test
	@DisplayName("다른 날짜로 OpenMeteo Weather API 호출이 성공한다")
	void Given_DifferentDate_When_CallWeatherApi_Then_ReturnsOpenMeteoWeatherResponse() {
		// given
		LocalDate differentDate = LocalDate.of(2024, 12, 25);
		OpenMeteoWeatherResponse expectedResponse = createMockOpenMeteoWeatherResponse();

		given(openMeteoKmaClient.getWeatherForecast(
			eq(latitude), eq(longitude), eq(WeatherType.OPEN_METEO_VARIABLES),
			eq("2024-12-25"), eq("2024-12-25"), eq("Asia/Seoul")
		)).willReturn(expectedResponse);

		// when
		OpenMeteoWeatherResponse result = openMeteoApiFacade.callWeatherApi(latitude, longitude, differentDate);

		// then
		assertThat(result).isEqualTo(expectedResponse);
		verify(openMeteoKmaClient, times(1)).getWeatherForecast(
			eq(latitude), eq(longitude), eq(WeatherType.OPEN_METEO_VARIABLES),
			eq("2024-12-25"), eq("2024-12-25"), eq("Asia/Seoul")
		);
	}

	private OpenMeteoResponse createMockOpenMeteoResponse() {
		return new OpenMeteoResponse(
			37.5665,
			126.9780,
			"Asia/Seoul",
			new OpenMeteoResponse.HourlyUnits(
				"iso8601",
				"μg/m³",
				"μg/m³",
				""
			),
			new OpenMeteoResponse.Hourly(
				List.of("2024-01-15T09:00"),
				List.of(0.8), // PM2.5
				List.of(1.5), // PM10
				List.of(2.5)  // UV Index
			)
		);
	}

	private OpenMeteoWeatherResponse createMockOpenMeteoWeatherResponse() {
		return new OpenMeteoWeatherResponse(
			37.5665,
			126.9780,
			"Asia/Seoul",
			new OpenMeteoWeatherResponse.HourlyUnits(
				"iso8601",
				""
			),
			new OpenMeteoWeatherResponse.Hourly(
				List.of("2024-01-15T09:00"),
				List.of(1) // Weather code (1 = 맑음)
			)
		);
	}

}
