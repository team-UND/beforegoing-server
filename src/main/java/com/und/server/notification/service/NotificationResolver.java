package com.und.server.notification.service;

import com.und.server.notification.constants.NotifType;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.dto.NotificationInfoDto;

public interface NotificationResolver {

	boolean supports(NotifType notifType);

	NotificationInfoDto resolve(Notification notification);

}
