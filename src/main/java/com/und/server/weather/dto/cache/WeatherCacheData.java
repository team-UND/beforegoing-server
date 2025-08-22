package com.und.server.weather.dto.cache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.response.WeatherResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class WeatherCacheData {

	private WeatherType weather;
	private FineDustType dust;
	private UvType uv;

	public WeatherResponse toWeatherResponse() {
		return WeatherResponse.from(weather, dust, uv);
	}

	@JsonIgnore
	public boolean isValid() {
		return weather != null && dust != null && uv != null;
	}

	public static WeatherCacheData from(WeatherType weather, FineDustType dust, UvType uv) {
		return WeatherCacheData.builder()
			.weather(weather)
			.dust(dust)
			.uv(uv)
			.build();
	}

}
