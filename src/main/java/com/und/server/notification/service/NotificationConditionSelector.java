package com.und.server.notification.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationErrorResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationConditionSelector {

	private final List<NotificationConditionService> services;


	public NotificationInfoDto findNotificationCondition(Notification notification) {
		NotificationConditionService service = findServiceByNotificationType(notification.getNotificationType());

		return service.findNotificationInfoByType(notification);
	}


	public void addNotificationCondition(
		Notification notification,
		List<Integer> daysOfWeekOrdinal,
		NotificationConditionRequest notificationConditionRequest
	) {
		NotificationConditionService service = findServiceByNotificationType(notification.getNotificationType());
		service.addNotificationCondition(notification, daysOfWeekOrdinal, notificationConditionRequest);
	}


	public void updateNotificationCondition(
		Notification notification,
		List<Integer> daysOfWeekOrdinal,
		NotificationConditionRequest notificationConditionRequest
	) {
		NotificationConditionService service = findServiceByNotificationType(notification.getNotificationType());
		service.updateNotificationCondition(notification, daysOfWeekOrdinal, notificationConditionRequest);
	}


	public void deleteNotificationCondition(NotificationType notificationType, Long notificationId) {
		NotificationConditionService service = findServiceByNotificationType(notificationType);
		service.deleteNotificationCondition(notificationId);
	}


	private NotificationConditionService findServiceByNotificationType(NotificationType notificationType) {
		return services.stream()
			.filter(service -> service.supports(notificationType))
			.findAny()
			.orElseThrow(() -> new ServerException(NotificationErrorResult.UNSUPPORTED_NOTIFICATION));
	}

}
