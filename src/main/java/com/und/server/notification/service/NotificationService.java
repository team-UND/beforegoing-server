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

@RequiredArgsConstructor
@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final NotificationConditionSelector notificationConditionSelector;


	@Transactional(readOnly = true)
	public NotificationInfoDto findNotificationDetails(Notification notification) {
		return notificationConditionSelector.findNotificationInfoByType(notification);
	}


	@Transactional
	public Notification addNotification(
		NotificationRequest notificationInfo,
		NotificationConditionRequest notificationConditionRequest
	) {
		Notification notification = notificationInfo.toEntity();
		List<Integer> dayOfWeekOrdinalList = notificationInfo.getDayOfWeekOrdinalList();

		notificationRepository.save(notification);
		notificationConditionSelector.addNotificationCondition(
			notification, dayOfWeekOrdinalList, notificationConditionRequest);

		return notification;
	}


	@Transactional
	public Notification updateNotification(
		Notification oldNotification,
		NotificationRequest notificationInfo,
		NotificationConditionRequest notificationConditionRequest
	) {
		List<Integer> dayOfWeekOrdinalList = notificationInfo.getDayOfWeekOrdinalList();
		boolean isChangeNotificationType =
			oldNotification.getNotificationType() != notificationInfo.getNotificationType();
		NotificationType oldNotificationType = oldNotification.getNotificationType();

		oldNotification.updateNotification(
			notificationInfo.getNotificationType(),
			notificationInfo.getNotificationMethodType()
		);
		oldNotification.updateActiveStatus(notificationInfo.getIsActive());

		if (!notificationInfo.getIsActive()) {
			notificationConditionSelector.deleteNotificationCondition(
				notificationInfo.getNotificationType(), oldNotification.getId());
			return oldNotification;
		}

		if (isChangeNotificationType) {
			notificationConditionSelector.deleteNotificationCondition(oldNotificationType, oldNotification.getId());
			notificationConditionSelector.addNotificationCondition(
				oldNotification, dayOfWeekOrdinalList, notificationConditionRequest);
			return oldNotification;
		}

		notificationConditionSelector.updateNotificationCondition(
			oldNotification, dayOfWeekOrdinalList, notificationConditionRequest);

		return oldNotification;
	}


	@Transactional
	public void deleteNotification(Notification notification) {
		notificationConditionSelector.deleteNotificationCondition(
			notification.getNotificationType(),
			notification.getId()
		);
	}

}
