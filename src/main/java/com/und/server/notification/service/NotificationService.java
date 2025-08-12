package com.und.server.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationConditionSelector notificationConditionSelector;


	@Transactional(readOnly = true)
	public NotificationInfoDto findNotificationDetails(Notification notification) {
		return notificationConditionSelector.findNotificationCondition(notification);
	}


	@Transactional
	public Notification addNotification(
		NotificationRequest notificationInfo,
		NotificationConditionRequest notificationConditionRequest
	) {
		Notification notification = notificationInfo.toEntity();
		List<Integer> dayOfWeekOrdinalList = notificationInfo.dayOfWeekOrdinalList();

		notificationRepository.save(notification);
		notificationConditionSelector.addNotificationCondition(
			notification, dayOfWeekOrdinalList, notificationConditionRequest);

		return notification;
	}


	@Transactional
	public Notification addWithoutNotification(NotificationType notificationType) {
		Notification notification = Notification.builder()
			.isActive(false)
			.notificationType(notificationType)
			.build();
		notificationRepository.save(notification);

		return notification;
	}


	@Transactional
	public void updateNotification(
		Notification notification,
		NotificationRequest notificationInfo,
		NotificationConditionRequest notificationConditionRequest
	) {
		List<Integer> dayOfWeekOrdinalList = notificationInfo.dayOfWeekOrdinalList();

		NotificationType oldNotificationType = notification.getNotificationType();
		NotificationType newNotificationtype = notificationInfo.notificationType();
		boolean isChangeNotificationType = oldNotificationType != newNotificationtype;

		notification.updateNotification(
			newNotificationtype,
			notificationInfo.notificationMethodType()
		);
		notification.updateActiveStatus(true);

		if (isChangeNotificationType) {
			notificationConditionSelector.deleteNotificationCondition(oldNotificationType, notification.getId());
			notificationConditionSelector.addNotificationCondition(
				notification, dayOfWeekOrdinalList, notificationConditionRequest);
			return;
		}

		notificationConditionSelector.updateNotificationCondition(
			notification, dayOfWeekOrdinalList, notificationConditionRequest);
	}


	@Transactional
	public void updateWithoutNotification(Notification oldNotification) {
		notificationConditionSelector.deleteNotificationCondition(
			oldNotification.getNotificationType(), oldNotification.getId());

		oldNotification.updateActiveStatus(false);
		oldNotification.deleteNotificationMethodType();
	}


	@Transactional
	public void deleteNotification(Notification notification) {
		notificationConditionSelector.deleteNotificationCondition(
			notification.getNotificationType(),
			notification.getId()
		);
	}

}
