package com.und.server.weather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "weather")
public record WeatherProperties(

	Kma kma,
	OpenMeteo openMeteo,
	OpenMeteoKma openMeteoKma

) {

	public record Kma(
		String baseUrl,
		String serviceKey
	) { }

	public record OpenMeteo(
		String baseUrl
	) { }

	public record OpenMeteoKma(
		String baseUrl
	) { }

}
