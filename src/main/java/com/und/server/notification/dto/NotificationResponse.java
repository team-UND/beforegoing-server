package com.und.server.notification.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.und.server.notification.entity.NotifType;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "notificationType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = TimeNotifResponse.class, name = "TIME")
})
public interface NotificationResponse {

	NotifType getNotificationType();

}
