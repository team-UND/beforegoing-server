package com.und.server.notification.service;

import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class NotificationResolverManager {

	private final List<NotificationResolver> resolvers;

	public List<NotificationDetailResponse> resolve(Notification notification) {
		for (NotificationResolver resolver : resolvers) {
			if (resolver.supports(notification.getNotifType())) {
				return resolver.resolve(notification);
			}
		}
		throw new IllegalArgumentException();
	}

}
