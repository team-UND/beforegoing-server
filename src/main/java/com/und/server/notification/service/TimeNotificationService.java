package com.und.server.notification.service;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.stereotype.Service;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.NotificationDayOfWeekResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotification;
import com.und.server.notification.exception.NotificationErrorResult;
import com.und.server.notification.repository.TimeNotificationRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TimeNotificationService implements NotificationConditionService {

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

		List<NotificationDayOfWeekResponse> dayOfWeekOrdinalList = getNofitDayOfWeekList(isEveryDay, timeNotifList);
		NotificationConditionResponse notificationDetail = TimeNotificationResponse.of(baseTimeNotif);

		return new NotificationInfoDto(isEveryDay, dayOfWeekOrdinalList, notificationDetail);
	}

	private List<NotificationDayOfWeekResponse> getNofitDayOfWeekList(
		boolean isEveryDay,
		List<TimeNotification> timeNotifList
	) {
		if (isEveryDay) {
			return List.of();
		}
		return timeNotifList.stream()
			.map(tn -> new NotificationDayOfWeekResponse(
				tn.getId(),
				tn.getDayOfWeek().ordinal()
			))
			.toList();
	}

	private boolean isEveryDay(List<TimeNotification> timeNotifList, TimeNotification timeNotifInfo) {
		return timeNotifList.size() == 1 && timeNotifInfo.getDayOfWeek() == null;
	}


	@Override
	public void addNotifDetail(
		Notification notification,
		List<Integer> dayOfWeekOrdinalList,
		NotificationConditionRequest notifDetailInfo
	) {
		if (!notification.isActive()) {
			return;
		}

		TimeNotificationRequest timeNotifInfo = (TimeNotificationRequest) notifDetailInfo;

		boolean isEveryday = dayOfWeekOrdinalList.size() == NotificationService.EVERYDAY;
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

}
