package com.und.server.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "notificationType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = TimeNotificationResponse.class, name = "TIME")
})
@Schema(
	description =
		"Notification condition request. The request body structure changes depending on the 'notificationType'.",
	discriminatorProperty = "notificationType"
)
public interface NotificationConditionResponse { }
