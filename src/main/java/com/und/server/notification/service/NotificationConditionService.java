package com.und.server.notification.service;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.entity.Notification;

public interface NotificationConditionService {

	boolean supports(final NotificationType notificationType);

	NotificationConditionResponse findNotificationInfoByType(final Notification notification);

	void addNotificationCondition(
		final Notification notification,
		final NotificationConditionRequest notificationConditionRequest);

	void updateNotificationCondition(
		final Notification notification,
		final NotificationConditionRequest notificationConditionRequest);

	void deleteNotificationCondition(final Long notificationId);

}
