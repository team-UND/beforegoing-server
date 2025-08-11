package com.und.server.notification.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationErrorResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class NotificationConditionSelector {

	private final List<NotificationConditionService> services;


	private NotificationConditionService findServiceByNotifType(NotifType notifType) {
		return services.stream()
			.filter(service -> service.supports(notifType))
			.findFirst()
			.orElseThrow(() -> new ServerException(NotificationErrorResult.UNSUPPORTED_NOTIF));
	}


	public NotificationInfoDto findNotifByNotifType(Notification notification) {
		NotificationConditionService service = findServiceByNotifType(notification.getNotificationType());

		return service.findNotifByNotifType(notification);
	}


	public void addNotif(
		Notification notification,
		List<Integer> dayOfWeekOrdinalList,
		NotificationConditionRequest notifConditionInfo
	) {
		NotificationConditionService service = findServiceByNotifType(notification.getNotificationType());
		service.addNotif(notification, dayOfWeekOrdinalList, notifConditionInfo);
	}


	public void updateNotif(
		Notification notification,
		List<Integer> dayOfWeekOrdinalList,
		NotificationConditionRequest notifConditionInfo
	) {
		NotificationConditionService service = findServiceByNotifType(notification.getNotificationType());
		service.updateNotif(notification, dayOfWeekOrdinalList, notifConditionInfo);
	}


	public void deleteNotif(NotifType notfType, Long notificationId) {
		NotificationConditionService service = findServiceByNotifType(notfType);
		service.deleteNotif(notificationId);
	}

}
