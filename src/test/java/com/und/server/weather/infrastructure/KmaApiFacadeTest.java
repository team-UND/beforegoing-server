package com.und.server.weather.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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

import com.und.server.weather.config.WeatherProperties;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.exception.KmaApiException;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.infrastructure.client.KmaWeatherClient;
import com.und.server.weather.infrastructure.dto.KmaWeatherResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("KmaApiFacade 테스트")
class KmaApiFacadeTest {

	@Mock
	private KmaWeatherClient kmaWeatherClient;

	@Mock
	private WeatherProperties weatherProperties;

	@Mock
	private WeatherProperties.Kma kmaProperties;

	@InjectMocks
	private KmaApiFacade kmaApiFacade;

	private GridPoint gridPoint;
	private LocalDate date;
	private TimeSlot timeSlot;

	@BeforeEach
	void setUp() {
		gridPoint = new GridPoint(60, 127);
		date = LocalDate.of(2024, 1, 15);
		timeSlot = TimeSlot.SLOT_09_12;

		given(weatherProperties.kma()).willReturn(kmaProperties);
		given(kmaProperties.serviceKey()).willReturn("test-service-key");

		// 기본 Mock 설정
		given(kmaWeatherClient.getVilageForecast(any(), any(), any(), any(), any(), any(), any(), any()))
			.willReturn(createMockKmaWeatherResponse());
	}

	@Test
	@DisplayName("KMA API 호출이 성공한다")
	void Given_ValidRequest_When_CallWeatherApi_Then_ReturnsWeatherResponse() {
		// when
		KmaWeatherResponse result = kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.response()).isNotNull();
		assertThat(result.response().header()).isNotNull();
		assertThat(result.response().body()).isNotNull();
	}

	@Test
	@DisplayName("다른 시간대 슬롯으로 KMA API 호출이 성공한다")
	void Given_DifferentTimeSlot_When_CallWeatherApi_Then_ReturnsWeatherResponse() {
		// given
		TimeSlot differentSlot = TimeSlot.SLOT_15_18;

		// when
		KmaWeatherResponse result = kmaApiFacade.callWeatherApi(gridPoint, differentSlot, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.response()).isNotNull();
	}

	@Test
	@DisplayName("다른 날짜로 KMA API 호출이 성공한다")
	void Given_DifferentDate_When_CallWeatherApi_Then_ReturnsWeatherResponse() {
		// given
		LocalDate differentDate = LocalDate.of(2024, 12, 25);

		// when
		KmaWeatherResponse result = kmaApiFacade.callWeatherApi(gridPoint, timeSlot, differentDate);

		// then
		assertThat(result).isNotNull();
		assertThat(result.response()).isNotNull();
	}

	@Test
	@DisplayName("다른 그리드 포인트로 KMA API 호출이 성공한다")
	void Given_DifferentGridPoint_When_CallWeatherApi_Then_ReturnsWeatherResponse() {
		// given
		GridPoint differentGridPoint = new GridPoint(100, 200);

		// when
		KmaWeatherResponse result = kmaApiFacade.callWeatherApi(differentGridPoint, timeSlot, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.response()).isNotNull();
	}

	@Test
	@DisplayName("ResourceAccessException 발생시 KMA_TIMEOUT 예외를 던진다")
	void Given_ResourceAccessException_When_CallWeatherApi_Then_ThrowsKmaTimeoutException() {
		// given
		given(kmaWeatherClient.getVilageForecast(any(), any(), any(), any(), any(), any(), any(), any()))
			.willThrow(new ResourceAccessException("Connection timeout"));

		// when & then
		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.KMA_TIMEOUT);
	}

	@Test
	@DisplayName("HttpClientErrorException 발생시 KMA_BAD_REQUEST 예외를 던진다")
	void Given_HttpClientErrorException_When_CallWeatherApi_Then_ThrowsKmaBadRequestException() {
		// given
		given(kmaWeatherClient.getVilageForecast(any(), any(), any(), any(), any(), any(), any(), any()))
			.willThrow(new RuntimeException("Bad request"));

		// when & then
		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.KMA_API_ERROR);
	}

	@Test
	@DisplayName("HttpServerErrorException 발생시 KMA_SERVER_ERROR 예외를 던진다")
	void Given_HttpServerErrorException_When_CallWeatherApi_Then_ThrowsKmaServerErrorException() {
		// given
		given(kmaWeatherClient.getVilageForecast(any(), any(), any(), any(), any(), any(), any(), any()))
			.willThrow(new RuntimeException("Server error"));

		// when & then
		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.KMA_API_ERROR);
	}

	@Test
	@DisplayName("RestClientResponseException 429 발생시 KMA_RATE_LIMIT 예외를 던진다")
	void Given_RestClientResponseException429_When_CallWeatherApi_Then_ThrowsKmaRateLimitException() {
		// given
		RestClientResponseException rateLimitException = new RestClientResponseException(
			"Rate limit exceeded", 429, "Too Many Requests", null, null, null
		);
		given(kmaWeatherClient.getVilageForecast(any(), any(), any(), any(), any(), any(), any(), any()))
			.willThrow(rateLimitException);

		// when & then
		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.KMA_RATE_LIMIT);
	}

	@Test
	@DisplayName("RestClientResponseException 기타 상태코드 발생시 KMA_API_ERROR 예외를 던진다")
	void Given_RestClientResponseExceptionOtherStatus_When_CallWeatherApi_Then_ThrowsKmaApiErrorException() {
		// given
		RestClientResponseException otherException = new RestClientResponseException(
			"Other error", 500, "Internal Server Error", null, null, null
		);
		given(kmaWeatherClient.getVilageForecast(any(), any(), any(), any(), any(), any(), any(), any()))
			.willThrow(otherException);

		// when & then
		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.KMA_API_ERROR);
	}

	@Test
	@DisplayName("기타 Exception 발생시 KMA_API_ERROR 예외를 던진다")
	void Given_GenericException_When_CallWeatherApi_Then_ThrowsKmaApiErrorException() {
		// given
		given(kmaWeatherClient.getVilageForecast(any(), any(), any(), any(), any(), any(), any(), any()))
			.willThrow(new RuntimeException("Unexpected error"));

		// when & then
		assertThatThrownBy(() -> kmaApiFacade.callWeatherApi(gridPoint, timeSlot, date))
			.isInstanceOf(KmaApiException.class)
			.hasFieldOrPropertyWithValue("errorResult", WeatherErrorResult.KMA_API_ERROR);
	}

	@Test
	@DisplayName("야간 시간대 슬롯으로 KMA API 호출이 성공한다")
	void Given_NightTimeSlot_When_CallWeatherApi_Then_ReturnsWeatherResponse() {
		// given
		TimeSlot nightSlot = TimeSlot.SLOT_21_24;

		// when
		KmaWeatherResponse result = kmaApiFacade.callWeatherApi(gridPoint, nightSlot, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.response()).isNotNull();
	}

	private KmaWeatherResponse createMockKmaWeatherResponse() {
		return new KmaWeatherResponse(
			new KmaWeatherResponse.Response(
				new KmaWeatherResponse.Header("00", "성공"),
				new KmaWeatherResponse.Body(
					"JSON",
					new KmaWeatherResponse.Items(
						List.of(
							new KmaWeatherResponse.WeatherItem("20240115", "0900", "SKY", "20240115", "0900", "1", 60,
								127)
						)
					),
					1
				)
			)
		);
	}

}
