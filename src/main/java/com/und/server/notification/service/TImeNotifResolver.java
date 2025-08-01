package com.und.server.notification.service;

import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.dto.TimeNotifResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotif;
import com.und.server.notification.repository.TimeNotifRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class TImeNotifResolver implements NotificationResolver {

	private final TimeNotifRepository timeNotifRepository;


	@Override
	public boolean supports(NotifType notifType) {
		return notifType == NotifType.TIME;
	}

	@Override
	public List<NotificationDetailResponse> resolve(Notification notification) {
		List<TimeNotif> timeNotifList = timeNotifRepository.findByNotificationId(notification.getId());
		return TimeNotifResponse.listOf(timeNotifList);
	}

}
