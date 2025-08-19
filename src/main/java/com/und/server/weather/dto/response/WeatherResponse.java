package com.und.server.weather.dto.response;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Weather response")
public record WeatherResponse(

	@Schema(description = "Weather condition", example = "비")
	String weather,

	@Schema(description = "FineDust condition", example = "나쁨")
	String fineDust,

	@Schema(description = "UV condition", example = "낮음")
	String uv

) {

	public static WeatherResponse from(WeatherType weather, FineDustType fineDust, UvType uvIndex) {
		return WeatherResponse.builder()
			.weather(weather.getDescription())
			.fineDust(fineDust.getDescription())
			.uv(uvIndex.getDescription())
			.build();
	}

}
