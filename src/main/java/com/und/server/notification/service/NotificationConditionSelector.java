package com.und.server.notification.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationErrorResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class NotificationConditionSelector {

	private final List<NotificationConditionService> services;


	public NotificationInfoDto findNotifByNotifType(Notification notification) {
		for (NotificationConditionService service : services) {
			if (service.supports(notification.getNotifType())) {
				return service.findNotifByNotifType(notification);
			}
		}
		throw new ServerException(NotificationErrorResult.UNSUPPORTED_NOTIF);
	}


	public void addNotif(
		Notification notification,
		List<Integer> dayOfWeekOrdinalList,
		NotificationConditionRequest notifConditionInfo
	) {
		for (NotificationConditionService service : services) {
			if (service.supports(notification.getNotifType())) {
				service.addNotifDetail(notification, dayOfWeekOrdinalList, notifConditionInfo);
				return;
			}
		}
		throw new ServerException(NotificationErrorResult.UNSUPPORTED_NOTIF);
	}

}
