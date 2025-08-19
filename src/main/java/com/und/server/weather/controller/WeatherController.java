package com.und.server.weather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.dto.response.WeatherResponse;
import com.und.server.weather.service.WeatherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1/weather")
public class WeatherController {

	private final WeatherService weatherService;

	@PostMapping
	public ResponseEntity<WeatherResponse> getWeather(@RequestBody @Valid WeatherRequest request) {
		WeatherResponse response = weatherService.getWeatherInfo(request);

		return ResponseEntity.ok(response);
	}

}
