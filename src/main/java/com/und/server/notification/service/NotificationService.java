package com.und.server.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.notification.entity.Notification;
import com.und.server.scenario.dto.NotificationInfoDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationService {

	private final NotificationResolverSelector notificationResolverSelector;

	@Transactional(readOnly = true)
	public NotificationInfoDto findNotificationDetails(Notification notification) {
		return notificationResolverSelector.resolve(notification);
	}

}
