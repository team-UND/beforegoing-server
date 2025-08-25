package com.und.server.notification.service;

import org.springframework.stereotype.Service;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotification;
import com.und.server.notification.repository.TimeNotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeNotificationService implements NotificationConditionService {

	private final TimeNotificationRepository timeNotificationRepository;


	@Override
	public boolean supports(final NotificationType notificationType) {
		return notificationType == NotificationType.TIME;
	}


	@Override
	public NotificationConditionResponse findNotificationInfoByType(final Notification notification) {
		if (!notification.isActive()) {
			return null;
		}

		TimeNotification timeNotifications =
			timeNotificationRepository.findByNotificationId(notification.getId());

		return TimeNotificationResponse.from(timeNotifications);
	}


	@Override
	public void addNotificationCondition(
		final Notification notification,
		final NotificationConditionRequest notificationConditionRequest
	) {
		if (!notification.isActive()) {
			return;
		}

		TimeNotificationRequest timeNotificationRequest = (TimeNotificationRequest) notificationConditionRequest;

		TimeNotification timeNotification = timeNotificationRequest.toEntity(notification);
		timeNotificationRepository.save(timeNotification);
	}


	@Override
	public void updateNotificationCondition(
		final Notification notification,
		final NotificationConditionRequest notificationConditionRequest
	) {
		TimeNotificationRequest timeNotificationRequest = (TimeNotificationRequest) notificationConditionRequest;
		TimeNotification oldTimeNotifications =
			timeNotificationRepository.findByNotificationId(notification.getId());

		if (oldTimeNotifications == null) {
			addNotificationCondition(notification, notificationConditionRequest);
			return;
		}

		oldTimeNotifications.updateTimeCondition(
			timeNotificationRequest.startHour(),
			timeNotificationRequest.startMinute()
		);
	}


	@Override
	public void deleteNotificationCondition(final Long notificationId) {
		timeNotificationRepository.deleteByNotificationId(notificationId);
	}

}
