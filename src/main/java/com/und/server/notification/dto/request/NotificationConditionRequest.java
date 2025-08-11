package com.und.server.notification.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "notificationType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = TimeNotificationRequest.class, name = "TIME")
})
public interface NotificationConditionRequest { }
