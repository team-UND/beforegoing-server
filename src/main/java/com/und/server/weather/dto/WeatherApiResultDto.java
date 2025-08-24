package com.und.server.weather.dto;

import com.und.server.weather.dto.api.KmaWeatherResponse;
import com.und.server.weather.dto.api.OpenMeteoResponse;

import lombok.Builder;

@Builder
public record WeatherApiResultDto(

	KmaWeatherResponse kmaWeatherResponse,
	OpenMeteoResponse openMeteoResponse

) {

	public static WeatherApiResultDto from(
		final KmaWeatherResponse kmaWeatherResponse,
		final OpenMeteoResponse openMeteoResponse
	) {
		return WeatherApiResultDto.builder()
			.kmaWeatherResponse(kmaWeatherResponse)
			.openMeteoResponse(openMeteoResponse)
			.build();
	}

}
