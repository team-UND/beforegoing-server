package com.und.server.weather.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.und.server.weather.dto.cache.WeatherCacheData;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CacheSerializer {

	private final ObjectMapper objectMapper;

	public CacheSerializer() {
		this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	}

	public String serializeWeatherCacheData(final WeatherCacheData data) {
		try {
			return objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			log.error("WeatherCacheData serialization failed", e);
			return null;
		}
	}

	public WeatherCacheData deserializeWeatherCacheData(final String json) {
		try {
			return objectMapper.readValue(json, WeatherCacheData.class);
		} catch (JsonProcessingException e) {
			log.error("WeatherCacheData deserialization failed: {}", json, e);
			return null;
		}
	}

	public Map<String, String> serializeWeatherCacheDataToHash(final Map<String, WeatherCacheData> hourlyData) {
		Map<String, String> hashData = new HashMap<>();

		for (Map.Entry<String, WeatherCacheData> entry : hourlyData.entrySet()) {
			try {
				String json = objectMapper.writeValueAsString(entry.getValue());
				hashData.put(entry.getKey(), json);
			} catch (JsonProcessingException e) {
				log.error("WeatherCacheData Hash serialization failed: {}", entry.getKey(), e);
			}
		}
		return hashData;
	}

	public WeatherCacheData deserializeWeatherCacheDataFromHash(final String json) {
		try {
			return objectMapper.readValue(json, WeatherCacheData.class);
		} catch (JsonProcessingException e) {
			log.error("WeatherCacheData Hash deserialization failed: {}", json, e);
			return null;
		}
	}

}
