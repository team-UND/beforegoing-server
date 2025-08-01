package com.und.server.notification.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "notificationType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = TimeNotifResponse.class, name = "TIME")
})
public abstract class NotificationDetailResponse {
}
