package com.und.server.weather.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.exception.WeatherException;
import com.und.server.weather.infrastructure.client.OpenMeteoClient;
import com.und.server.weather.infrastructure.client.OpenMeteoKmaClient;
import com.und.server.weather.infrastructure.dto.OpenMeteoResponse;
import com.und.server.weather.infrastructure.dto.OpenMeteoWeatherResponse;

@ExtendWith(MockitoExtension.class)
class OpenMeteoApiFacadeTest {

	@Mock
	private OpenMeteoClient openMeteoClient;

	@Mock
	private OpenMeteoKmaClient openMeteoKmaClient;

	@InjectMocks
	private OpenMeteoApiFacade facade;

	private final Double latitude = 37.5;
	private final Double longitude = 127.0;
	private final LocalDate date = LocalDate.of(2024, 1, 1);

	@Test
	@DisplayName("callDustUvApi - 정상 응답 반환")
	void Given_ValidRequest_When_CallDustUvApi_Then_ReturnResponse() {
		// given
		OpenMeteoResponse mockResponse = mock(OpenMeteoResponse.class);
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willReturn(mockResponse);

		// when
		OpenMeteoResponse result = facade.callDustUvApi(latitude, longitude, date);

		// then
		assertThat(result).isEqualTo(mockResponse);
	}

	@Test
	@DisplayName("callDustUvApi - 네트워크 타임아웃 시 WeatherException 발생")
	void Given_Timeout_When_CallDustUvApi_Then_ThrowWeatherException() {
		// given
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(new ResourceAccessException("timeout"));

		// when
		Throwable thrown = catchThrowable(() ->
			facade.callDustUvApi(37.5, 127.0, LocalDate.of(2024, 1, 1)));

		// then
		assertThat(thrown).isInstanceOf(WeatherException.class);
		WeatherException ex = (WeatherException) thrown;
		assertThat(ex.getErrorResult()).isEqualTo(WeatherErrorResult.OPEN_METEO_TIMEOUT);
	}


	@Test
	@DisplayName("callWeatherApi - 정상 응답 반환")
	void Given_ValidRequest_When_CallWeatherApi_Then_ReturnResponse() {
		// given
		OpenMeteoWeatherResponse mockResponse = mock(OpenMeteoWeatherResponse.class);
		given(openMeteoKmaClient.getWeatherForecast(any(), any(), any(), any(), any(), any()))
			.willReturn(mockResponse);

		// when
		OpenMeteoWeatherResponse result = facade.callWeatherApi(latitude, longitude, date);

		// then
		assertThat(result).isEqualTo(mockResponse);
	}

	@Test
	@DisplayName("callWeatherApi - 예외 발생 시 WeatherException 발생")
	void Given_Error_When_CallWeatherApi_Then_ThrowWeatherException() {
		// given
		given(openMeteoKmaClient.getWeatherForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(new RuntimeException("API error"));

		// when
		Throwable thrown = catchThrowable(() ->
			facade.callWeatherApi(37.5, 127.0, LocalDate.of(2024, 1, 1)));

		// then
		assertThat(thrown).isInstanceOf(WeatherException.class);
		WeatherException ex = (WeatherException) thrown;
		assertThat(ex.getErrorResult()).isEqualTo(WeatherErrorResult.OPEN_METEO_API_ERROR);
	}


	@Test
	@DisplayName("callDustUvApi - 4xx 발생 시 WeatherException 발생")
	void Given_HttpClientError_When_CallDustUvApi_Then_ThrowWeatherException() {
		// given
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

		// when
		Throwable thrown = catchThrowable(() ->
			facade.callDustUvApi(latitude, longitude, date));

		// then
		assertThat(thrown).isInstanceOf(WeatherException.class);
		WeatherException ex = (WeatherException) thrown;
		assertThat(ex.getErrorResult()).isEqualTo(WeatherErrorResult.OPEN_METEO_BAD_REQUEST);
	}

	@Test
	@DisplayName("callDustUvApi - 5xx 발생 시 WeatherException 발생")
	void Given_HttpServerError_When_CallDustUvApi_Then_ThrowWeatherException() {
		// given
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

		// when
		Throwable thrown = catchThrowable(() ->
			facade.callDustUvApi(latitude, longitude, date));

		// then
		assertThat(thrown).isInstanceOf(WeatherException.class);
		WeatherException ex = (WeatherException) thrown;
		assertThat(ex.getErrorResult()).isEqualTo(WeatherErrorResult.OPEN_METEO_SERVER_ERROR);
	}

	@Test
	@DisplayName("callDustUvApi - 429 발생 시 WeatherException 발생")
	void Given_TooManyRequests_When_CallDustUvApi_Then_ThrowWeatherException() {
		// given
		RestClientResponseException tooManyRequests =
			new RestClientResponseException("Rate limit", 429, "Too Many Requests", null, null, null);
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(tooManyRequests);

		// when
		Throwable thrown = catchThrowable(() ->
			facade.callDustUvApi(latitude, longitude, date));

		// then
		assertThat(thrown).isInstanceOf(WeatherException.class);
		WeatherException ex = (WeatherException) thrown;
		assertThat(ex.getErrorResult()).isEqualTo(WeatherErrorResult.OPEN_METEO_RATE_LIMIT);
	}

	@Test
	@DisplayName("callDustUvApi - RestClientResponseException(기타) 발생 시 WeatherException 발생")
	void Given_RestClientResponseException_When_CallDustUvApi_Then_ThrowWeatherException() {
		// given
		RestClientResponseException otherError =
			new RestClientResponseException("Other error", 418, "I'm a teapot", null, null, null);
		given(openMeteoClient.getForecast(any(), any(), any(), any(), any(), any()))
			.willThrow(otherError);

		// when
		Throwable thrown = catchThrowable(() ->
			facade.callDustUvApi(latitude, longitude, date));

		// then
		assertThat(thrown).isInstanceOf(WeatherException.class);
		WeatherException ex = (WeatherException) thrown;
		assertThat(ex.getErrorResult()).isEqualTo(WeatherErrorResult.OPEN_METEO_API_ERROR);
	}

}
