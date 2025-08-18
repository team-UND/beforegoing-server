package com.und.server.notification.constants;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NotificationMethodType {

	PUSH, ALARM;

	@JsonCreator
	public static NotificationMethodType fromValue(final String value) {
		if (value == null) {
			return null;
		}
		return NotificationMethodType.valueOf(value.toUpperCase());
	}

}
