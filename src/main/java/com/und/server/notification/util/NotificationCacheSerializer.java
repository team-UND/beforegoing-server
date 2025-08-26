package com.und.server.notification.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.notification.dto.cache.NotificationCacheData;
import com.und.server.notification.dto.response.NotificationConditionResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCacheSerializer {

	private final ObjectMapper objectMapper;

	public String serialize(NotificationCacheData data) {
		try {
			return objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize NotificationCacheData: {}", data, e);
			throw new RuntimeException("Failed to serialize cache data", e);
		}
	}

	public NotificationCacheData deserialize(String json) {
		try {
			return objectMapper.readValue(json, NotificationCacheData.class);
		} catch (JsonProcessingException e) {
			log.error("Failed to deserialize JSON to NotificationCacheData: {}", json, e);
			throw new RuntimeException("Failed to deserialize cache data", e);
		}
	}

	public NotificationConditionResponse parseCondition(NotificationCacheData data) {
		try {
			return objectMapper.readValue(data.conditionJson(), NotificationConditionResponse.class);
		} catch (JsonProcessingException e) {
			log.error("Failed to deserialize condition from cache: {}", data.conditionJson(), e);
			throw new RuntimeException("Failed to parse condition from cache", e);
		}
	}

	public String serializeCondition(NotificationConditionResponse condition) {
		try {
			return objectMapper.writeValueAsString(condition);
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize condition: {}", condition, e);
			throw new RuntimeException("Failed to serialize condition", e);
		}
	}

}
