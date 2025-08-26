package com.und.server.notification.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.notification.dto.cache.NotificationCacheData;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.exception.NotificationCacheErrorResult;
import com.und.server.notification.exception.NotificationCacheException;

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
			throw new NotificationCacheException(NotificationCacheErrorResult.SERIALIZE_FAILED);
		}
	}

	public NotificationCacheData deserialize(String json) {
		try {
			return objectMapper.readValue(json, NotificationCacheData.class);
		} catch (JsonProcessingException e) {
			throw new NotificationCacheException(NotificationCacheErrorResult.DESERIALIZE_FAILED);
		}
	}

	public NotificationConditionResponse parseCondition(NotificationCacheData data) {
		try {
			return objectMapper.readValue(data.conditionJson(), NotificationConditionResponse.class);
		} catch (JsonProcessingException e) {
			throw new NotificationCacheException(NotificationCacheErrorResult.CONDITION_PARSE_FAILED);
		}
	}

	public String serializeCondition(NotificationConditionResponse condition) {
		try {
			return objectMapper.writeValueAsString(condition);
		} catch (JsonProcessingException e) {
			throw new NotificationCacheException(NotificationCacheErrorResult.CONDITION_SERIALIZE_FAILED);
		}
	}

}
