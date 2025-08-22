package com.und.server.weather.dto;

import lombok.Builder;

import com.und.server.weather.dto.api.KmaWeatherResponse;
import com.und.server.weather.dto.api.OpenMeteoResponse;

@Builder
public record WeatherApiResultDto(

	KmaWeatherResponse kmaWeatherResponse,
	OpenMeteoResponse openMeteoResponse

) {

	public static WeatherApiResultDto from(
		KmaWeatherResponse kmaWeatherResponse, OpenMeteoResponse openMeteoResponse
	) {
		return WeatherApiResultDto.builder()
			.kmaWeatherResponse(kmaWeatherResponse)
			.openMeteoResponse(openMeteoResponse)
			.build();
	}

}
