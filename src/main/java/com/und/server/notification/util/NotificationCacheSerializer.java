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

	public String serialize(final NotificationCacheData notificationCacheData) {
		try {
			return objectMapper.writeValueAsString(notificationCacheData);
		} catch (JsonProcessingException e) {
			throw new NotificationCacheException(NotificationCacheErrorResult.SERIALIZE_FAILED);
		}
	}

	public NotificationCacheData deserialize(final String json) {
		try {
			return objectMapper.readValue(json, NotificationCacheData.class);
		} catch (JsonProcessingException e) {
			throw new NotificationCacheException(NotificationCacheErrorResult.DESERIALIZE_FAILED);
		}
	}

	public NotificationConditionResponse parseCondition(final NotificationCacheData notificationCacheData) {
		try {
			return objectMapper.readValue(
				notificationCacheData.conditionJson(), NotificationConditionResponse.class);
		} catch (JsonProcessingException e) {
			throw new NotificationCacheException(NotificationCacheErrorResult.CONDITION_PARSE_FAILED);
		}
	}

	public String serializeCondition(final NotificationConditionResponse notificationConditionResponse) {
		try {
			return objectMapper.writeValueAsString(notificationConditionResponse);
		} catch (JsonProcessingException e) {
			throw new NotificationCacheException(NotificationCacheErrorResult.CONDITION_SERIALIZE_FAILED);
		}
	}

}
