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

@RequiredArgsConstructor
@Service
public class TimeNotificationService implements NotificationConditionService {

	public static final int EVERYDAY = 7;
	private final TimeNotificationRepository timeNotificationRepository;


	@Override
	public boolean supports(NotificationType notificationType) {
		return notificationType == NotificationType.TIME;
	}


	@Override
	public NotificationInfoDto findNotificationInfoByType(Notification notification) {
		if (!notification.isActive()) {
			return null;
		}

		List<TimeNotification> timeNotificationList =
			timeNotificationRepository.findByNotificationId(notification.getId());
		if (timeNotificationList.isEmpty()) {
			return null;
		}

		TimeNotification baseTimeNotification = timeNotificationList.get(0);

		List<Integer> dayOfWeekOrdinalList = timeNotificationList.stream()
			.map(tn -> tn.getDayOfWeek().ordinal())
			.toList();

		boolean isEveryDay = dayOfWeekOrdinalList.size() == EVERYDAY;
		NotificationConditionResponse timeNotificationResponse = TimeNotificationResponse.of(baseTimeNotification);

		return new NotificationInfoDto(isEveryDay, dayOfWeekOrdinalList, timeNotificationResponse);
	}


	@Override
	public void addNotificationCondition(
		Notification notification,
		List<Integer> dayOfWeekOrdinalList,
		NotificationConditionRequest notificationConditionRequest
	) {
		if (!notification.isActive()) {
			return;
		}

		TimeNotificationRequest timeNotificationRequest = (TimeNotificationRequest) notificationConditionRequest;

		List<TimeNotification> timeNotificationList = dayOfWeekOrdinalList.stream()
			.map(ordinal -> DayOfWeek.values()[ordinal])
			.map(dayOfWeek -> timeNotificationRequest.toEntity(notification, dayOfWeek))
			.toList();
		timeNotificationRepository.saveAll(timeNotificationList);
	}


	@Override
	public void updateNotificationCondition(
		Notification oldNotification,
		List<Integer> newDayOfWeekOrdinalList,
		NotificationConditionRequest notificationConditionRequest
	) {
		TimeNotificationRequest timeNotificationInfo = (TimeNotificationRequest) notificationConditionRequest;
		List<TimeNotification> oldTimeNotificationList =
			timeNotificationRepository.findByNotificationId(oldNotification.getId());

		Set<Integer> oldOrdinals = oldTimeNotificationList.stream()
			.map(tn -> tn.getDayOfWeek().ordinal())
			.collect(Collectors.toSet());

		Set<Integer> newOrdinals = new HashSet<>(newDayOfWeekOrdinalList);

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
			List<TimeNotification> toDelete = oldTimeNotificationList.stream()
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
			List<TimeNotification> toUpdate = oldTimeNotificationList.stream()
				.filter(tn -> toUpdateOrdinals.contains(tn.getDayOfWeek().ordinal()))
				.peek(tn -> {
					tn.updateTimeCondition(
						timeNotificationInfo.getStartHour(), timeNotificationInfo.getStartMinute());
				})
				.toList();
			timeNotificationRepository.saveAll(toUpdate);
		}
	}


	@Override
	public void deleteNotificationCondition(Long notificationId) {
		List<TimeNotification> timeNotificationList =
			timeNotificationRepository.findByNotificationId(notificationId);
		timeNotificationRepository.deleteAll(timeNotificationList);
	}

}
