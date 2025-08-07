package com.und.server.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationConditionSelector notificationConditionSelector;


	@Transactional(readOnly = true)
	public NotificationInfoDto findNotificationDetails(Notification notification) {
		return notificationConditionSelector.findNotifByNotifType(notification);
	}


	@Transactional
	public Notification addNotification(
		NotificationRequest notifInfo,
		NotificationConditionRequest notifConditionInfo
	) {
		Notification notification = notifInfo.toEntity();
		notificationRepository.save(notification);

		List<Integer> dayOfWeekOrdinalList = notifInfo.getDayOfWeekOrdinalList();
		notificationConditionSelector.addNotif(notification, dayOfWeekOrdinalList, notifConditionInfo);

		return notification;
	}


	@Transactional
	public Notification updateNotification(
		Notification oldNotification,
		NotificationRequest notifInfo,
		NotificationConditionRequest notifConditionInfo
	) {
		List<Integer> dayOfWeekOrdinalList = notifInfo.getDayOfWeekOrdinalList();
		boolean isChangeNotifType = oldNotification.getNotifType() != notifInfo.getNotificationType();
		NotifType oldNotifType = oldNotification.getNotifType();

		oldNotification.setNotifType(notifInfo.getNotificationType());
		oldNotification.setNotifMethodType(notifInfo.getNotificationMethodType());
		oldNotification.setIsActive(notifInfo.getIsActive());

		if (!notifInfo.getIsActive()) {
			notificationConditionSelector.deleteNotif(notifInfo.getNotificationType(), oldNotification.getId());
			return oldNotification;
		}

		if (isChangeNotifType) {
			notificationConditionSelector.deleteNotif(oldNotifType, oldNotification.getId());
			notificationConditionSelector.addNotif(oldNotification, dayOfWeekOrdinalList, notifConditionInfo);
			return oldNotification;
		}

		notificationConditionSelector.updateNotif(oldNotification, dayOfWeekOrdinalList, notifConditionInfo);

		return oldNotification;
	}

}
