package com.und.server.notification.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NofitDayOfWeekResponse;
import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.dto.TimeNotifResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotif;
import com.und.server.notification.exception.NotificationErrorResult;
import com.und.server.notification.repository.TimeNotifRepository;
import com.und.server.scenario.dto.NotificationInfoDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class TImeNotifResolver implements NotificationResolver {

	private final TimeNotifRepository timeNotifRepository;


	@Override
	public boolean supports(NotifType notifType) {
		return notifType == NotifType.TIME;
	}


	@Override
	public NotificationInfoDto resolve(Notification notification) {
		List<TimeNotif> timeNotifList = timeNotifRepository.findByNotificationId(notification.getId());

		if (timeNotifList.isEmpty()) {
			throw new ServerException(NotificationErrorResult.NOT_FOUND_NOTIF);
		}

		TimeNotif baseTimeNotif = timeNotifList.get(0);
		boolean isEveryDay = isEveryDay(timeNotifList, baseTimeNotif);

		List<NofitDayOfWeekResponse> dayOfWeekOrdinalList = getNofitDayOfWeekList(isEveryDay, timeNotifList);
		NotificationDetailResponse notificationDetail = TimeNotifResponse.of(baseTimeNotif);

		return new NotificationInfoDto(
			isEveryDay,
			dayOfWeekOrdinalList,
			notificationDetail
		);
	}

	private List<NofitDayOfWeekResponse> getNofitDayOfWeekList(boolean isEveryDay, List<TimeNotif> timeNotifList) {
		if (isEveryDay) {
			return List.of();
		}
		return timeNotifList.stream()
			.map(tn -> new NofitDayOfWeekResponse(
				tn.getId(),
				tn.getDayOfWeek().ordinal()
			))
			.toList();
	}

	private boolean isEveryDay(List<TimeNotif> timeNotifList, TimeNotif timeNotifInfo) {
		return timeNotifList.size() == 1 && timeNotifInfo.getDayOfWeek() == null;
	}

}
