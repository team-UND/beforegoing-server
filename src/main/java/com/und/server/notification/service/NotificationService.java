package com.und.server.notification.service;

import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationService {

	private final NotificationResolverManager notificationResolverManager;

	@Transactional(readOnly = true)
	public List<NotificationDetailResponse> findNotificationDetails(Notification notification) {
		return notificationResolverManager.resolve(notification);
	}

}
