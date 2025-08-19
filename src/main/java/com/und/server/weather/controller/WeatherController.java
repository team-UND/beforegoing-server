package com.und.server.weather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.dto.response.WeatherResponse;
import com.und.server.weather.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 정보 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Tag(name = "Weather", description = "날씨 정보 API")
public class WeatherController {

	private final WeatherService weatherService;

	/**
	 * 날씨 정보 조회
	 *
	 * @param request 날씨 조회 요청 (위도, 경도, 날짜)
	 * @return 날씨 정보 (날씨, 미세먼지, 자외선 지수)
	 */
	@PostMapping
	@Operation(
		summary = "날씨 정보 조회",
		description = "위도/경도와 날짜를 기반으로 날씨, 미세먼지, 자외선 지수 정보를 조회합니다.\n" +
			"- 오늘 날짜: 현재 4시간 구간의 최악 시나리오\n" +
			"- 미래 날짜: 하루 전체의 최악 시나리오\n" +
			"- 최대 +3일까지 조회 가능"
	)
	public ResponseEntity<WeatherResponse> getWeather(
		@Valid @RequestBody WeatherRequest request
	) {
		log.info("날씨 정보 조회 요청: {}", request);

		WeatherResponse response = weatherService.getWeatherInfo(request);
		log.info("날씨 정보 조회 완료: {}", response);

		return ResponseEntity.ok(response);
	}
}
