package com.und.server.weather.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
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

import com.und.server.weather.config.WeatherProperties;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.exception.KmaApiException;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.infrastructure.client.KmaWeatherClient;
import com.und.server.weather.infrastructure.dto.KmaWeatherResponse;

@ExtendWith(MockitoExtension.class)
class KmaApiFacadeTest {

	@Mock
	private KmaWeatherClient kmaWeatherClient;

	@Mock
	private WeatherProperties weatherProperties;

	@InjectMocks
	private KmaApiFacade kmaApiFacade;

	private GridPoint gridPoint;
	private TimeSlot timeSlot;
	private LocalDate date;

	@BeforeEach
	void setUp() {
		gridPoint = new GridPoint(60, 127);
		timeSlot = TimeSlot.SLOT_09_12;
		date = LocalDate.of(2024, 1, 1);

		WeatherProperties.Kma props = org.mockito.Mockito.mock(WeatherProperties.Kma.class);
		given(props.serviceKey()).willReturn("test-key");
		given(weatherProperties.kma()).willReturn(props);
	}

	@Test
	@DisplayName("정상 호출 시 KmaWeatherResponse 반환")
	void Given_ValidRequest_When_CallWeatherApi_Then_ReturnResponse() {
		KmaWeatherResponse mockResponse = org.mockito.Mockito.mock(KmaWeatherResponse.class);
		given(kmaWeatherClient.getVilageForecast(any(), anyInt(), anyInt(), any(), any(), any(), anyInt(), anyInt()))
			.willReturn(mockResponse);

		KmaWeatherResponse result = kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date);

		assertThat(result).isEqualTo(mockResponse);
	}

	@Test
	@DisplayName("네트워크 타임아웃 발생 시 KMA_TIMEOUT 반환")
	void Given_Timeout_When_CallWeatherApi_Then_ThrowKmaTimeout() {
		given(kmaWeatherClient.getVilageForecast(any(), anyInt(), anyInt(), any(), any(), any(), anyInt(), anyInt()))
			.willThrow(new ResourceAccessException("timeout"));

		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.satisfies(e -> {
				assertThat(((KmaApiException) e).getErrorResult())
					.isEqualTo(WeatherErrorResult.KMA_TIMEOUT);
			});
	}

	@Test
	@DisplayName("4xx 발생 시 KMA_BAD_REQUEST 반환")
	void Given_4xx_When_CallWeatherApi_Then_ThrowKmaBadRequest() {
		given(kmaWeatherClient.getVilageForecast(any(), anyInt(), anyInt(), any(), any(), any(), anyInt(), anyInt()))
			.willThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad request", null, null, null));

		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.satisfies(e -> {
				assertThat(((KmaApiException) e).getErrorResult())
					.isEqualTo(WeatherErrorResult.KMA_BAD_REQUEST);
			});
	}

	@Test
	@DisplayName("5xx 발생 시 KMA_SERVER_ERROR 반환")
	void Given_5xx_When_CallWeatherApi_Then_ThrowKmaServerError() {
		given(kmaWeatherClient.getVilageForecast(any(), anyInt(), anyInt(), any(), any(), any(), anyInt(), anyInt()))
			.willThrow(
				HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", null, null, null));

		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.satisfies(e -> {
				assertThat(((KmaApiException) e).getErrorResult())
					.isEqualTo(WeatherErrorResult.KMA_SERVER_ERROR);
			});
	}

	@Test
	@DisplayName("429 발생 시 KMA_RATE_LIMIT 반환")
	void Given_429_When_CallWeatherApi_Then_ThrowKmaRateLimit() {
		RestClientResponseException rateLimitEx =
			new RestClientResponseException("Too many requests", 429, "Too many requests", null, null, null);

		given(kmaWeatherClient.getVilageForecast(any(), anyInt(), anyInt(), any(), any(), any(), anyInt(), anyInt()))
			.willThrow(rateLimitEx);

		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.satisfies(e -> {
				assertThat(((KmaApiException) e).getErrorResult())
					.isEqualTo(WeatherErrorResult.KMA_RATE_LIMIT);
			});
	}

	@Test
	@DisplayName("기타 Exception 발생 시 KMA_API_ERROR 반환")
	void Given_OtherError_When_CallWeatherApi_Then_ThrowKmaApiError() {
		given(kmaWeatherClient.getVilageForecast(any(), anyInt(), anyInt(), any(), any(), any(), anyInt(), anyInt()))
			.willThrow(new RuntimeException("Unexpected error"));

		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.satisfies(e -> {
				assertThat(((KmaApiException) e).getErrorResult())
					.isEqualTo(WeatherErrorResult.KMA_API_ERROR);
			});
	}

	@Test
	@DisplayName("RestClientResponseException (429 아님) 발생 시 KMA_API_ERROR 반환")
	void Given_RestClientResponseExceptionNon429_When_CallWeatherApi_Then_ThrowKmaApiError() {
		RestClientResponseException otherError =
			new RestClientResponseException("Other error", 418, "I'm a teapot", null, null, null);

		given(kmaWeatherClient.getVilageForecast(any(), anyInt(), anyInt(), any(), any(), any(), anyInt(), anyInt()))
			.willThrow(otherError);

		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.satisfies(e -> {
				assertThat(((KmaApiException) e).getErrorResult())
					.isEqualTo(WeatherErrorResult.KMA_API_ERROR);
			});
	}

}
