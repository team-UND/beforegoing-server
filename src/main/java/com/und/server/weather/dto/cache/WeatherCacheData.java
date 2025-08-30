package com.und.server.weather.dto.cache;

import java.util.Objects;

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
	FineDustType fineDust,
	UvType uv

) {

	@JsonIgnore
	public boolean isValid() {
		return weather != null && fineDust != null && uv != null;
	}

	@JsonIgnore
	public WeatherCacheData getValidDefault() {
		return WeatherCacheData.builder()
			.weather(Objects.requireNonNullElse(this.weather(), WeatherType.DEFAULT))
			.fineDust(Objects.requireNonNullElse(this.fineDust(), FineDustType.DEFAULT))
			.uv(Objects.requireNonNullElse(this.uv(), UvType.DEFAULT))
			.build();
	}

	public static WeatherCacheData from(
		final WeatherType weather,
		final FineDustType findDust,
		final UvType uv
	) {
		return WeatherCacheData.builder()
			.weather(weather)
			.fineDust(findDust)
			.uv(uv)
			.build();
	}

	public static WeatherCacheData getDefault() {
		return WeatherCacheData.builder()
			.weather(WeatherType.DEFAULT)
			.fineDust(FineDustType.DEFAULT)
			.uv(UvType.DEFAULT)
			.build();
	}

}
