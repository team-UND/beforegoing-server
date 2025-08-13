package com.und.server.notification.service;

import java.util.List;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.entity.Notification;

public interface NotificationConditionService {

	boolean supports(final NotificationType notificationType);

	NotificationInfoDto findNotificationInfoByType(final Notification notification);

	void addNotificationCondition(
		final Notification notification,
		final List<Integer> daysOfWeekOrdinal,
		final NotificationConditionRequest notificationConditionRequest);

	void updateNotificationCondition(
		final Notification oldNotification,
		final List<Integer> daysOfWeekOrdinal,
		final NotificationConditionRequest notificationConditionRequest);

	void deleteNotificationCondition(final Long notificationId);

}
