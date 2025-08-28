package com.und.server.weather.dto;

import com.und.server.weather.dto.api.OpenMeteoResponse;
import com.und.server.weather.dto.api.OpenMeteoWeatherResponse;

import lombok.Builder;

@Builder
public record OpenMeteoWeatherApiResultDto(

	OpenMeteoWeatherResponse openMeteoWeatherResponse,
	OpenMeteoResponse openMeteoResponse

) {

	public static OpenMeteoWeatherApiResultDto from(
		final OpenMeteoWeatherResponse openMeteoWeatherResponse,
		final OpenMeteoResponse openMeteoResponse
	) {
		return OpenMeteoWeatherApiResultDto.builder()
			.openMeteoWeatherResponse(openMeteoWeatherResponse)
			.openMeteoResponse(openMeteoResponse)
			.build();
	}

}
