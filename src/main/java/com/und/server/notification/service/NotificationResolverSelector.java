package com.und.server.notification.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationErrorResult;
import com.und.server.scenario.dto.NotificationInfoDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class NotificationResolverSelector {

	private final List<NotificationResolver> resolvers;

	public NotificationInfoDto resolve(Notification notification) {
		for (NotificationResolver resolver : resolvers) {
			if (resolver.supports(notification.getNotifType())) {
				return resolver.resolve(notification);
			}
		}
		throw new ServerException(NotificationErrorResult.UNSUPPORTED_NOTIF);
	}

}
