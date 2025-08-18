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
	public NotificationInfoDto findNotificationDetails(final Notification notification) {
		return notificationConditionSelector.findNotificationCondition(notification);
	}


	@Transactional
	public Notification addNotification(
		final NotificationRequest notificationRequest,
		final NotificationConditionRequest notificationConditionRequest
	) {
		Notification notification = notificationRequest.toEntity();
		List<Integer> daysOfWeekOrdinal = notificationRequest.daysOfWeekOrdinal();

		notificationRepository.save(notification);
		notificationConditionSelector.addNotificationCondition(
			notification, daysOfWeekOrdinal, notificationConditionRequest);

		return notification;
	}


	@Transactional
	public Notification addWithoutNotification(final NotificationRequest notificationRequest) {
		Notification notification = notificationRequest.toEntity();
		notificationRepository.save(notification);

		return notification;
	}


	@Transactional
	public void updateNotification(
		final Notification notification,
		final NotificationRequest notificationRequest,
		final NotificationConditionRequest notificationConditionRequest
	) {
		List<Integer> daysOfWeekOrdinal = notificationRequest.daysOfWeekOrdinal();

		NotificationType oldNotificationType = notification.getNotificationType();
		NotificationType newNotificationtype = notificationRequest.notificationType();
		boolean isChangeNotificationType = oldNotificationType != newNotificationtype;

		notification.updateNotification(
			newNotificationtype,
			notificationRequest.notificationMethodType()
		);
		notification.updateActiveStatus(true);

		if (isChangeNotificationType) {
			notificationConditionSelector.deleteNotificationCondition(oldNotificationType, notification.getId());
			notificationConditionSelector.addNotificationCondition(
				notification, daysOfWeekOrdinal, notificationConditionRequest);
			return;
		}

		notificationConditionSelector.updateNotificationCondition(
			notification, daysOfWeekOrdinal, notificationConditionRequest);
	}


	@Transactional
	public void updateWithoutNotification(final Notification oldNotification) {
		notificationConditionSelector.deleteNotificationCondition(
			oldNotification.getNotificationType(), oldNotification.getId());

		oldNotification.updateActiveStatus(false);
		oldNotification.deleteNotificationMethodType();
	}


	@Transactional
	public void deleteNotification(final Notification notification) {
		notificationConditionSelector.deleteNotificationCondition(
			notification.getNotificationType(),
			notification.getId()
		);
	}

}
