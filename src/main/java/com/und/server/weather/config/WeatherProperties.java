package com.und.server.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * 날씨 API 설정 Properties
 */
@Getter
@Setter  // Spring ConfigurationProperties 바인딩용
@Component
@ConfigurationProperties(prefix = "weather")
public class WeatherProperties {

	private Kma kma = new Kma();
	private OpenMeteo openMeteo = new OpenMeteo();

	@Getter
	@Setter  // Spring ConfigurationProperties 바인딩용
	public static class Kma {
		private String baseUrl;
		private String serviceKey;
	}

	@Getter
	@Setter  // Spring ConfigurationProperties 바인딩용
	public static class OpenMeteo {
		private String baseUrl;
	}
	
}
