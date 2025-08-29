package com.und.server.weather.infrastructure.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KmaWeatherResponseTest {

	@Test
	@DisplayName("KmaWeatherResponse.Header 생성 및 필드 검증")
	void testHeader() {
		KmaWeatherResponse.Header header =
			new KmaWeatherResponse.Header("00", "NORMAL SERVICE");

		assertThat(header.resultCode()).isEqualTo("00");
		assertThat(header.resultMsg()).isEqualTo("NORMAL SERVICE");
	}

	@Test
	@DisplayName("KmaWeatherResponse.WeatherItem 생성 및 필드 검증")
	void testWeatherItem() {
		KmaWeatherResponse.WeatherItem item = new KmaWeatherResponse.WeatherItem(
			"20240101", "0200", "TMP",
			"20240101", "0300", "5",
			60, 127
		);

		assertThat(item.baseDate()).isEqualTo("20240101");
		assertThat(item.fcstValue()).isEqualTo("5");
		assertThat(item.nx()).isEqualTo(60);
		assertThat(item.ny()).isEqualTo(127);
	}

	@Test
	@DisplayName("KmaWeatherResponse 전체 구조 생성 및 필드 검증")
	void testResponse() {
		KmaWeatherResponse.WeatherItem item = new KmaWeatherResponse.WeatherItem(
			"20240101", "0200", "TMP", "20240101", "0300", "5", 60, 127
		);
		KmaWeatherResponse.Items items = new KmaWeatherResponse.Items(List.of(item));
		KmaWeatherResponse.Body body = new KmaWeatherResponse.Body("JSON", items, 1);
		KmaWeatherResponse.Header header = new KmaWeatherResponse.Header("00", "NORMAL SERVICE");
		KmaWeatherResponse.Response response = new KmaWeatherResponse.Response(header, body);

		KmaWeatherResponse weatherResponse = new KmaWeatherResponse(response);

		assertThat(weatherResponse.response()).isEqualTo(response);
		assertThat(weatherResponse.response().header().resultCode()).isEqualTo("00");
		assertThat(weatherResponse.response().body().items().item()).contains(item);
	}

}
