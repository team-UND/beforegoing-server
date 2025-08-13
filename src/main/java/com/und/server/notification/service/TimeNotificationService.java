package com.und.server.notification.service;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotification;
import com.und.server.notification.repository.TimeNotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeNotificationService implements NotificationConditionService {

	public static final int EVERYDAY = 7;
	private final TimeNotificationRepository timeNotificationRepository;


	@Override
	public boolean supports(final NotificationType notificationType) {
		return notificationType == NotificationType.TIME;
	}


	@Override
	public NotificationInfoDto findNotificationInfoByType(final Notification notification) {
		if (!notification.isActive()) {
			return null;
		}

		List<TimeNotification> timeNotifications =
			timeNotificationRepository.findByNotificationId(notification.getId());

		TimeNotification baseTimeNotification = timeNotifications.get(0);

		List<Integer> daysOfWeekOrdinal = timeNotifications.stream()
			.map(tn -> tn.getDayOfWeek().ordinal())
			.toList();

		boolean isEveryDay = daysOfWeekOrdinal.size() == EVERYDAY;
		NotificationConditionResponse timeNotificationResponse = TimeNotificationResponse.from(baseTimeNotification);

		return new NotificationInfoDto(isEveryDay, daysOfWeekOrdinal, timeNotificationResponse);
	}


	@Override
	public void addNotificationCondition(
		final Notification notification,
		final List<Integer> daysOfWeekOrdinal,
		final NotificationConditionRequest notificationConditionRequest
	) {
		if (!notification.isActive()) {
			return;
		}

		TimeNotificationRequest timeNotificationRequest = (TimeNotificationRequest) notificationConditionRequest;

		List<TimeNotification> timeNotifications = daysOfWeekOrdinal.stream()
			.map(ordinal -> DayOfWeek.values()[ordinal])
			.map(dayOfWeek -> timeNotificationRequest.toEntity(notification, dayOfWeek))
			.toList();
		timeNotificationRepository.saveAll(timeNotifications);
	}


	@Override
	public void updateNotificationCondition(
		final Notification oldNotification,
		final List<Integer> daysOfWeekOrdinal,
		final NotificationConditionRequest notificationConditionRequest
	) {
		TimeNotificationRequest timeNotificationInfo = (TimeNotificationRequest) notificationConditionRequest;
		List<TimeNotification> oldTimeNotifications =
			timeNotificationRepository.findByNotificationId(oldNotification.getId());

		Set<Integer> oldOrdinals = oldTimeNotifications.stream()
			.map(tn -> tn.getDayOfWeek().ordinal())
			.collect(Collectors.toSet());

		Set<Integer> newOrdinals = new HashSet<>(daysOfWeekOrdinal);

		Set<Integer> toDeleteOrdinals = oldOrdinals.stream()
			.filter(ordinal -> !newOrdinals.contains(ordinal))
			.collect(Collectors.toSet());

		Set<Integer> toAddOrdinals = newOrdinals.stream()
			.filter(ordinal -> !oldOrdinals.contains(ordinal))
			.collect(Collectors.toSet());

		Set<Integer> toUpdateOrdinals = oldOrdinals.stream()
			.filter(newOrdinals::contains)
			.collect(Collectors.toSet());

		if (!toDeleteOrdinals.isEmpty()) {
			List<TimeNotification> toDelete = oldTimeNotifications.stream()
				.filter(tn -> toDeleteOrdinals.contains(tn.getDayOfWeek().ordinal()))
				.toList();
			timeNotificationRepository.deleteAll(toDelete);
		}

		if (!toAddOrdinals.isEmpty()) {
			List<TimeNotification> toAdd = toAddOrdinals.stream()
				.map(ordinal -> DayOfWeek.values()[ordinal])
				.map(dayOfWeek -> timeNotificationInfo.toEntity(oldNotification, dayOfWeek))
				.toList();
			timeNotificationRepository.saveAll(toAdd);
		}

		if (!toUpdateOrdinals.isEmpty()) {
			List<TimeNotification> toUpdate = oldTimeNotifications.stream()
				.filter(tn -> toUpdateOrdinals.contains(tn.getDayOfWeek().ordinal()))
				.peek(tn -> {
					tn.updateTimeCondition(
						timeNotificationInfo.startHour(), timeNotificationInfo.startMinute());
				})
				.toList();
			timeNotificationRepository.saveAll(toUpdate);
		}
	}


	@Override
	public void deleteNotificationCondition(final Long notificationId) {
		List<TimeNotification> timeNotifications =
			timeNotificationRepository.findByNotificationId(notificationId);
		timeNotificationRepository.deleteAll(timeNotifications);
	}

}
