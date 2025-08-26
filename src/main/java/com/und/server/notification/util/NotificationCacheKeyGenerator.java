package com.und.server.notification.util;

import org.springframework.stereotype.Component;

@Component
public class NotificationCacheKeyGenerator {

	private static final String NOTIFICATION_CACHE_PREFIX = "notif";
	private static final String ETAG_PREFIX = "etag";

	public String generateNotificationCacheKey(Long memberId) {
		return String.format("%s:%d", NOTIFICATION_CACHE_PREFIX, memberId);
	}

	public String generateEtagKey(Long memberId) {
		return String.format("%s:%s:%d", NOTIFICATION_CACHE_PREFIX, ETAG_PREFIX, memberId);
	}

}
