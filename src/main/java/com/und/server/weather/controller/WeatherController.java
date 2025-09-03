package com.und.server.weather.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.dto.response.WeatherResponse;
import com.und.server.weather.service.WeatherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1/weather")
public class WeatherController {

	private final WeatherService weatherService;

	@PostMapping
	public ResponseEntity<WeatherResponse> getWeather(
		@RequestBody @Valid final WeatherRequest request,
		@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate date,
		@RequestParam(defaultValue = "Asia/Seoul") final String timezone
	) {
		final WeatherResponse response = weatherService.getWeatherInfo(request, date, timezone);

		return ResponseEntity.ok(response);
	}

}
