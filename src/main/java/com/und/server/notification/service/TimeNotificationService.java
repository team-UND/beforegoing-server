package com.und.server.notification.service;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotification;
import com.und.server.notification.exception.NotificationErrorResult;
import com.und.server.notification.repository.TimeNotificationRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TimeNotificationService implements NotificationConditionService {

	public static final int EVERYDAY = 7;
	private final TimeNotificationRepository timeNotifRepository;


	@Override
	public boolean supports(NotifType notifType) {
		return notifType == NotifType.TIME;
	}


	@Override
	public NotificationInfoDto findNotifByNotifType(Notification notification) {
		if (!notification.isActive()) {
			return null;
		}

		List<TimeNotification> timeNotifList = timeNotifRepository.findByNotificationId(notification.getId());

		if (timeNotifList.isEmpty()) {
			throw new ServerException(NotificationErrorResult.NOT_FOUND_NOTIF);
		}

		TimeNotification baseTimeNotif = timeNotifList.get(0);
		boolean isEveryDay = isEveryDay(timeNotifList, baseTimeNotif);

		List<Integer> dayOfWeekOrdinalList = getNofitDayOfWeekList(isEveryDay, timeNotifList);
		NotificationConditionResponse notificationDetail = TimeNotificationResponse.of(baseTimeNotif);

		return new NotificationInfoDto(isEveryDay, dayOfWeekOrdinalList, notificationDetail);
	}

	private List<Integer> getNofitDayOfWeekList(
		boolean isEveryDay,
		List<TimeNotification> timeNotifList
	) {
		if (isEveryDay) {
			return List.of();
		}
		return timeNotifList.stream()
			.map(tn -> tn.getDayOfWeek().ordinal())
			.toList();
	}

	private boolean isEveryDay(List<TimeNotification> timeNotifList, TimeNotification timeNotifInfo) {
		return timeNotifList.size() == 1 && timeNotifInfo.getDayOfWeek() == null;
	}


	@Override
	public void addNotif(
		Notification notification,
		List<Integer> dayOfWeekOrdinalList,
		NotificationConditionRequest notifDetailInfo
	) {
		if (!notification.isActive()) {
			return;
		}

		TimeNotificationRequest timeNotifInfo = (TimeNotificationRequest) notifDetailInfo;

		boolean isEveryday = dayOfWeekOrdinalList.size() == EVERYDAY;
		if (isEveryday) {
			TimeNotification timeNotif = timeNotifInfo.toEntity(notification, null);
			timeNotifRepository.save(timeNotif);
			return;
		}

		List<TimeNotification> timeNotifList = dayOfWeekOrdinalList.stream()
			.map(DayOfWeek::of)
			.map(dayOfWeek -> timeNotifInfo.toEntity(notification, dayOfWeek))
			.toList();
		timeNotifRepository.saveAll(timeNotifList);
	}


	@Override
	public void deleteNotif(Long notificationId) {
		List<TimeNotification> timeNotifList = timeNotifRepository.findByNotificationId(notificationId);
		timeNotifRepository.deleteAll(timeNotifList);
	}


	@Override
	public void updateNotif(
		Notification oldNotification,
		List<Integer> newDayOfWeekOrdinalList,
		NotificationConditionRequest notifDetailInfo
	) {
		TimeNotificationRequest timeNotifInfo = (TimeNotificationRequest) notifDetailInfo;
		List<TimeNotification> oldTimeNotifList = timeNotifRepository.findByNotificationId(oldNotification.getId());

		if (newDayOfWeekOrdinalList.size() == EVERYDAY) {
			updateEveryDayNotif(oldNotification, oldTimeNotifList, timeNotifInfo);
			return;
		}

		Set<Integer> oldOrdinals = oldTimeNotifList.stream()
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
			List<TimeNotification> toDelete = oldTimeNotifList.stream()
				.filter(tn -> toDeleteOrdinals.contains(tn.getDayOfWeek().ordinal()))
				.toList();
			timeNotifRepository.deleteAll(toDelete);
		}

		if (!toAddOrdinals.isEmpty()) {
			List<TimeNotification> toAdd = toAddOrdinals.stream()
				.map(DayOfWeek::of)
				.map(dayOfWeek -> timeNotifInfo.toEntity(oldNotification, dayOfWeek))
				.toList();
			timeNotifRepository.saveAll(toAdd);
		}

		if (!toUpdateOrdinals.isEmpty()) {
			List<TimeNotification> toUpdate = oldTimeNotifList.stream()
				.filter(tn -> toUpdateOrdinals.contains(tn.getDayOfWeek().ordinal()))
				.peek(tn -> {
					tn.setHour(timeNotifInfo.getHour());
					tn.setMinute(timeNotifInfo.getMinute());
				})
				.toList();
			timeNotifRepository.saveAll(toUpdate);
		}
	}

	private void updateEveryDayNotif(
		Notification oldNotification,
		List<TimeNotification> oldTimeNotifList,
		TimeNotificationRequest timeNotifInfo
	) {
		boolean isAlreadyEveryDay =
			oldTimeNotifList.size() == 1 && oldTimeNotifList.get(0).getDayOfWeek() == null;
		if (isAlreadyEveryDay) {
			TimeNotification timeNotif = oldTimeNotifList.get(0);
			timeNotif.setHour(timeNotifInfo.getHour());
			timeNotif.setMinute(timeNotifInfo.getMinute());
			timeNotifRepository.save(timeNotif);
			return;
		}
		timeNotifRepository.deleteAll(oldTimeNotifList);

		TimeNotification timeNotif = timeNotifInfo.toEntity(oldNotification, null);
		timeNotifRepository.save(timeNotif);
	}

}
