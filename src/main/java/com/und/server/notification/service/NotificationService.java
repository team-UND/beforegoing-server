package com.und.server.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.NotificationDayOfWeekRequest;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationService {

	public static final int EVERYDAY = 7;
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

		List<Integer> dayOfWeekOrdinalList = notifInfo.getDayOfWeekOrdinalList().stream()
			.map(NotificationDayOfWeekRequest::getDayOfWeekOrdinal)
			.toList();
		notificationConditionSelector.addNotif(notification, dayOfWeekOrdinalList, notifConditionInfo);

		return notification;
	}

}
