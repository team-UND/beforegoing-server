package com.und.server.weather.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.und.server.weather.dto.cache.TimeSlotWeatherCacheData;
import com.und.server.weather.dto.cache.WeatherCacheData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CacheSerializer {

	private final ObjectMapper objectMapper;

	public CacheSerializer() {
		this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	}

	public String serializeWeatherCacheData(final WeatherCacheData data) {
		try {
			return objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			log.error("WeatherCacheData 직렬화 실패", e);
			return null;
		}
	}

	public String serializeTimeSlotWeatherCacheData(final TimeSlotWeatherCacheData data) {
		try {
			return objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			log.error("TimeSlotWeatherCacheData 직렬화 실패", e);
			return null;
		}
	}

	public WeatherCacheData deserializeWeatherCacheData(final String json) {
		try {
			return objectMapper.readValue(json, WeatherCacheData.class);
		} catch (JsonProcessingException e) {
			log.error("WeatherCacheData 역직렬화 실패: {}", json, e);
			return null;
		}
	}

	public TimeSlotWeatherCacheData deserializeTimeSlotWeatherCacheData(final String json) {
		try {
			return objectMapper.readValue(json, TimeSlotWeatherCacheData.class);
		} catch (JsonProcessingException e) {
			log.error("TimeSlotWeatherCacheData 역직렬화 실패: {}", json, e);
			return null;
		}
	}

}
