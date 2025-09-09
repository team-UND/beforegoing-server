package com.und.server.weather.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.dto.response.WeatherResponse;
import com.und.server.weather.service.WeatherService;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherController 테스트")
class WeatherControllerTest {

	@Mock
	private WeatherService weatherService;

	@InjectMocks
	private WeatherController weatherController;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(weatherController).build();
		objectMapper = new ObjectMapper();
	}


	@Test
	@DisplayName("날씨 정보를 성공적으로 조회한다")
	void Given_ValidRequest_When_GetWeather_Then_ReturnsWeatherResponse() throws Exception {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate date = LocalDate.of(2024, 1, 15);
		WeatherResponse expectedResponse = WeatherResponse.from(
			WeatherType.SUNNY, FineDustType.GOOD, UvType.LOW
		);

		given(weatherService.getWeatherInfo((request), (date), ZoneId.of("Asia/Seoul")))
			.willReturn(expectedResponse);

		// when & then
		mockMvc.perform(post("/v1/weather")
				.param("date", "2024-01-15")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.weather").value("SUNNY"))
			.andExpect(jsonPath("$.fineDust").value("GOOD"))
			.andExpect(jsonPath("$.uv").value("LOW"));
	}


	@Test
	@DisplayName("비 오는 날씨 정보를 조회한다")
	void Given_RainyWeather_When_GetWeather_Then_ReturnsRainyWeatherResponse() throws Exception {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate date = LocalDate.of(2024, 1, 15);
		WeatherResponse expectedResponse = WeatherResponse.from(
			WeatherType.RAIN, FineDustType.NORMAL, UvType.NORMAL
		);

		given(weatherService.getWeatherInfo((request), (date), ZoneId.of("Asia/Seoul")))
			.willReturn(expectedResponse);

		// when & then
		mockMvc.perform(post("/v1/weather")
				.param("date", "2024-01-15")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.weather").value("RAIN"))
			.andExpect(jsonPath("$.fineDust").value("NORMAL"))
			.andExpect(jsonPath("$.uv").value("NORMAL"));
	}


	@Test
	@DisplayName("미세먼지 나쁨 상태의 날씨 정보를 조회한다")
	void Given_BadFineDust_When_GetWeather_Then_ReturnsBadFineDustResponse() throws Exception {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate date = LocalDate.of(2024, 1, 15);
		WeatherResponse expectedResponse = WeatherResponse.from(
			WeatherType.CLOUDY, FineDustType.BAD, UvType.HIGH
		);

		given(weatherService.getWeatherInfo((request), (date), ZoneId.of("Asia/Seoul")))
			.willReturn(expectedResponse);

		// when & then
		mockMvc.perform(post("/v1/weather")
				.param("date", "2024-01-15")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.weather").value("CLOUDY"))
			.andExpect(jsonPath("$.fineDust").value("BAD"))
			.andExpect(jsonPath("$.uv").value("HIGH"));
	}

	@Test
	@DisplayName("눈 오는 날씨 정보를 조회한다")
	void Given_SnowyWeather_When_GetWeather_Then_ReturnsSnowyWeatherResponse() throws Exception {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate date = LocalDate.of(2024, 1, 15);
		WeatherResponse expectedResponse = WeatherResponse.from(
			WeatherType.SNOW, FineDustType.GOOD, UvType.VERY_LOW
		);

		given(weatherService.getWeatherInfo((request), (date), ZoneId.of("Asia/Seoul")))
			.willReturn(expectedResponse);

		// when & then
		mockMvc.perform(post("/v1/weather")
				.param("date", "2024-01-15")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.weather").value("SNOW"))
			.andExpect(jsonPath("$.fineDust").value("GOOD"))
			.andExpect(jsonPath("$.uv").value("VERY_LOW"));
	}


	@Test
	@DisplayName("다른 좌표로 날씨 정보를 조회한다")
	void Given_DifferentCoordinates_When_GetWeather_Then_ReturnsWeatherResponse() throws Exception {
		// given
		WeatherRequest request = new WeatherRequest(35.1796, 129.0756); // 부산
		LocalDate date = LocalDate.of(2024, 1, 15);
		WeatherResponse expectedResponse = WeatherResponse.from(
			WeatherType.SUNNY, FineDustType.GOOD, UvType.VERY_HIGH
		);

		given(weatherService.getWeatherInfo((request), (date), ZoneId.of("Asia/Seoul")))
			.willReturn(expectedResponse);

		// when & then
		mockMvc.perform(post("/v1/weather")
				.param("date", "2024-01-15")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.weather").value("SUNNY"))
			.andExpect(jsonPath("$.fineDust").value("GOOD"))
			.andExpect(jsonPath("$.uv").value("VERY_HIGH"));
	}


	@Test
	@DisplayName("다른 날짜로 날씨 정보를 조회한다")
	void Given_DifferentDate_When_GetWeather_Then_ReturnsWeatherResponse() throws Exception {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate date = LocalDate.of(2024, 12, 25);
		WeatherResponse expectedResponse = WeatherResponse.from(
			WeatherType.CLOUDY, FineDustType.NORMAL, UvType.LOW
		);

		given(weatherService.getWeatherInfo((request), (date), ZoneId.of("Asia/Seoul")))
			.willReturn(expectedResponse);

		// when & then
		mockMvc.perform(post("/v1/weather")
				.param("date", "2024-12-25")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.weather").value("CLOUDY"))
			.andExpect(jsonPath("$.fineDust").value("NORMAL"))
			.andExpect(jsonPath("$.uv").value("LOW"));
	}


	@Test
	@DisplayName("모든 날씨 타입이 최악인 상태를 조회한다")
	void Given_WorstWeatherConditions_When_GetWeather_Then_ReturnsWorstWeatherResponse() throws Exception {
		// given
		WeatherRequest request = new WeatherRequest(37.5665, 126.9780);
		LocalDate date = LocalDate.of(2024, 1, 15);
		WeatherResponse expectedResponse = WeatherResponse.from(
			WeatherType.SNOW, FineDustType.VERY_BAD, UvType.VERY_HIGH
		);

		given(weatherService.getWeatherInfo((request), (date), ZoneId.of("Asia/Seoul")))
			.willReturn(expectedResponse);

		// when & then
		mockMvc.perform(post("/v1/weather")
				.param("date", "2024-01-15")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.weather").value("SNOW"))
			.andExpect(jsonPath("$.fineDust").value("VERY_BAD"))
			.andExpect(jsonPath("$.uv").value("VERY_HIGH"));
	}

}
