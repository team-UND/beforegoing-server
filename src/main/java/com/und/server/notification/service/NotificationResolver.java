package com.und.server.notification.service;

import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.entity.Notification;

import java.util.List;

public interface NotificationResolver {

	boolean supports(NotifType notifType);

	List<NotificationDetailResponse> resolve(Notification notification);

}
