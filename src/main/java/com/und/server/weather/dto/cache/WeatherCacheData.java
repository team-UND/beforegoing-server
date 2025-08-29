package com.und.server.weather.dto.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherCacheData(

	WeatherType weather,
	FineDustType findDust,
	UvType uv

) {

	@JsonIgnore
	public boolean isValid() {
		return weather != null && findDust != null && uv != null;
	}

	public static WeatherCacheData from(
		final WeatherType weather,
		final FineDustType findDust,
		final UvType uv
	) {
		return WeatherCacheData.builder()
			.weather(weather)
			.findDust(findDust)
			.uv(uv)
			.build();
	}

	public static WeatherCacheData getDefault() {
		return WeatherCacheData.builder()
			.weather(WeatherType.DEFAULT)
			.findDust(FineDustType.DEFAULT)
			.uv(UvType.DEFAULT)
			.build();
	}

}
