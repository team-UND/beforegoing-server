package com.und.server.notification.service;

import java.util.List;

import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.entity.Notification;

public interface NotificationResolver {

	boolean supports(NotifType notifType);

	List<NotificationDetailResponse> resolve(Notification notification);

}
