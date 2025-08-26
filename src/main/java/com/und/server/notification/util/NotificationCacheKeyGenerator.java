package com.und.server.notification.util;

import org.springframework.stereotype.Component;

/**
 * Redis 캐시 키 생성 유틸리티
 */
@Component
public class NotificationCacheKeyGenerator {

    private static final String NOTIFICATION_CACHE_PREFIX = "notif";
    private static final String ETAG_PREFIX = "etag";

    /**
     * 사용자별 알림 캐시 키 생성
     * 형식: notif:{memberId}
     */
    public String generateNotificationCacheKey(Long memberId) {
        return String.format("%s:%d", NOTIFICATION_CACHE_PREFIX, memberId);
    }

    /**
     * 사용자별 ETag 키 생성
     * 형식: notif:etag:{memberId}
     */
    public String generateEtagKey(Long memberId) {
        return String.format("%s:%s:%d", NOTIFICATION_CACHE_PREFIX, ETAG_PREFIX, memberId);
    }
}

