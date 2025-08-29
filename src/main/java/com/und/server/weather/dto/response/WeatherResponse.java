package com.und.server.weather.dto.response;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Weather(Weather, FindDust, UV) response")
public record WeatherResponse(

	@Schema(description = "Weather condition", example = "RAIN")
	WeatherType weather,

	@Schema(description = "FineDust condition", example = "BAD")
	FineDustType fineDust,

	@Schema(description = "UV condition", example = "VERY_LOW")
	UvType uv

) {

	public static WeatherResponse from(
		final WeatherType weather,
		final FineDustType fineDust,
		final UvType uvIndex
	) {
		return WeatherResponse.builder()
			.weather(weather)
			.fineDust(fineDust)
			.uv(uvIndex)
			.build();
	}

}
