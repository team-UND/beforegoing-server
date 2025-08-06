package com.und.server.notification.service;

import java.util.List;

import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationInfoDto;
import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.entity.Notification;

public interface NotificationConditionService {

	boolean supports(NotifType notifType);

	NotificationInfoDto findNotifByNotifType(Notification notification);

	void addNotifDetail(Notification notification,
						List<Integer> dayOfWeekOrdinalList,
						NotificationConditionRequest notifDetailInfo);

}
