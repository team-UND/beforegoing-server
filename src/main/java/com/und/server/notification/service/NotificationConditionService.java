package com.und.server.notification.service;

import java.util.List;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.entity.Notification;

public interface NotificationConditionService {

	boolean supports(NotificationType notificationType);

	NotificationInfoDto findNotificationInfoByType(Notification notification);

	void addNotificationCondition(
		Notification notification,
		List<Integer> daysOfWeekOrdinal,
		NotificationConditionRequest notificationConditionRequest);

	void updateNotificationCondition(
		Notification oldNotification,
		List<Integer> daysOfWeekOrdinal,
		NotificationConditionRequest notificationConditionRequest);

	void deleteNotificationCondition(Long notificationId);

}
