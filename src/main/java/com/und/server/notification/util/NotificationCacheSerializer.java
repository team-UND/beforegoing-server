package com.und.server.notification.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.cache.NotificationCacheData;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.repository.NotificationRepository;
import com.und.server.notification.service.NotificationConditionSelector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Redis 캐시 데이터 직렬화/역직렬화 유틸리티
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCacheSerializer {

    private final ObjectMapper objectMapper;
    private final NotificationConditionSelector notificationConditionSelector;
    private final NotificationRepository notificationRepository;

    /**
     * NotificationCacheData를 JSON 문자열로 직렬화
     */
    public String serialize(NotificationCacheData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize NotificationCacheData: {}", data, e);
            throw new RuntimeException("Failed to serialize cache data", e);
        }
    }

    /**
     * JSON 문자열을 NotificationCacheData로 역직렬화
     */
    public NotificationCacheData deserialize(String json) {
        try {
            return objectMapper.readValue(json, NotificationCacheData.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to NotificationCacheData: {}", json, e);
            throw new RuntimeException("Failed to deserialize cache data", e);
        }
    }

    /**
     * NotificationCacheData를 NotificationConditionResponse로 변환
     */
    public NotificationConditionResponse parseCondition(NotificationCacheData data) {
        try {
            // 캐시에 저장된 conditionJson을 직접 역직렬화
            return objectMapper.readValue(data.conditionJson(), NotificationConditionResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize condition from cache: {}", data.conditionJson(), e);
            throw new RuntimeException("Failed to parse condition from cache", e);
        }
    }

    /**
     * NotificationConditionResponse를 JSON으로 직렬화
     */
    public String serializeCondition(NotificationConditionResponse condition) {
        try {
            return objectMapper.writeValueAsString(condition);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize condition: {}", condition, e);
            throw new RuntimeException("Failed to serialize condition", e);
        }
    }
}
