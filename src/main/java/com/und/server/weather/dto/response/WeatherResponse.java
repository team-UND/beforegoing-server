package com.und.server.weather.dto.response;

import lombok.Builder;

import com.und.server.weather.constants.FineDustLevel;
import com.und.server.weather.constants.UvIndexLevel;
import com.und.server.weather.constants.WeatherType;

/**
 * 날씨 정보 조회 응답 DTO
 */
@Builder
public record WeatherResponse(

	String weather,           // 날씨 ("비", "눈", "소나기", "진눈깨비", null)
	String fineDust,          // 미세먼지 등급 ("좋음", "보통", "나쁨", "매우나쁨")
	String uv            // 자외선 지수 등급 ("매우낮음", "낮음", "보통", "높음", "매우높음")

) {
	/**
	 * 정적 팩토리 메서드 - 오늘 조회용
	 */
	public static WeatherResponse from(WeatherType weather, FineDustLevel fineDust, UvIndexLevel uvIndex) {
		return WeatherResponse.builder()
			.weather(weather.getDescription())
			.fineDust(fineDust.getDescription())
			.uv(uvIndex.getDescription())
			.build();
	}

}
