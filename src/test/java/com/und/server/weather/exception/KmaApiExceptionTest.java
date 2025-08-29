package com.und.server.weather.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KmaApiException 테스트")
class KmaApiExceptionTest {

	@Test
	@DisplayName("WeatherErrorResult로 KmaApiException을 생성한다")
	void Given_WeatherErrorResult_When_CreateKmaApiException_Then_ExceptionIsCreated() {
		// given
		WeatherErrorResult errorResult = WeatherErrorResult.KMA_API_ERROR;

		// when
		KmaApiException exception = new KmaApiException(errorResult);

		// then
		assertThat(exception).isNotNull();
		assertThat(exception.getErrorResult()).isEqualTo(errorResult);
		assertThat(exception.getMessage()).isEqualTo(errorResult.getMessage());
	}

	@Test
	@DisplayName("WeatherErrorResult와 원인으로 KmaApiException을 생성한다")
	void Given_WeatherErrorResultAndCause_When_CreateKmaApiException_Then_ExceptionIsCreated() {
		// given
		WeatherErrorResult errorResult = WeatherErrorResult.KMA_BAD_REQUEST;
		Throwable cause = new RuntimeException("Test cause");

		// when
		KmaApiException exception = new KmaApiException(errorResult, cause);

		// then
		assertThat(exception).isNotNull();
		assertThat(exception.getErrorResult()).isEqualTo(errorResult);
		assertThat(exception.getMessage()).isEqualTo(errorResult.getMessage());
		assertThat(exception.getCause()).isEqualTo(cause);
	}

	@Test
	@DisplayName("다양한 WeatherErrorResult로 KmaApiException을 생성한다")
	void Given_DifferentWeatherErrorResults_When_CreateKmaApiException_Then_ExceptionsAreCreated() {
		// given & when & then
		KmaApiException badRequestException = new KmaApiException(WeatherErrorResult.KMA_BAD_REQUEST);
		assertThat(badRequestException.getErrorResult()).isEqualTo(WeatherErrorResult.KMA_BAD_REQUEST);

		KmaApiException serverErrorException = new KmaApiException(WeatherErrorResult.KMA_SERVER_ERROR);
		assertThat(serverErrorException.getErrorResult()).isEqualTo(WeatherErrorResult.KMA_SERVER_ERROR);

		KmaApiException rateLimitException = new KmaApiException(WeatherErrorResult.KMA_RATE_LIMIT);
		assertThat(rateLimitException.getErrorResult()).isEqualTo(WeatherErrorResult.KMA_RATE_LIMIT);

		KmaApiException apiErrorException = new KmaApiException(WeatherErrorResult.KMA_API_ERROR);
		assertThat(apiErrorException.getErrorResult()).isEqualTo(WeatherErrorResult.KMA_API_ERROR);
	}

	@Test
	@DisplayName("KmaApiException의 메시지가 올바르게 설정된다")
	void Given_KmaApiException_When_GetMessage_Then_MessageIsCorrect() {
		// given
		WeatherErrorResult errorResult = WeatherErrorResult.KMA_TIMEOUT;

		// when
		KmaApiException exception = new KmaApiException(errorResult);

		// then
		assertThat(exception.getMessage()).isEqualTo(errorResult.getMessage());
	}

	@Test
	@DisplayName("KmaApiException의 원인이 올바르게 설정된다")
	void Given_KmaApiExceptionWithCause_When_GetCause_Then_CauseIsCorrect() {
		// given
		WeatherErrorResult errorResult = WeatherErrorResult.KMA_API_ERROR;
		Throwable cause = new IllegalArgumentException("Invalid argument");

		// when
		KmaApiException exception = new KmaApiException(errorResult, cause);

		// then
		assertThat(exception.getCause()).isEqualTo(cause);
		assertThat(exception.getCause().getMessage()).isEqualTo("Invalid argument");
	}

	@Test
	@DisplayName("KmaApiException의 errorResult 필드가 올바르게 접근된다")
	void Given_KmaApiException_When_GetErrorResult_Then_ErrorResultIsCorrect() {
		// given
		WeatherErrorResult errorResult = WeatherErrorResult.KMA_BAD_REQUEST;

		// when
		KmaApiException exception = new KmaApiException(errorResult);

		// then
		assertThat(exception.getErrorResult()).isEqualTo(errorResult);
		assertThat(exception.getErrorResult().getMessage()).isEqualTo(errorResult.getMessage());
	}

}
