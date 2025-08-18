package com.und.server.notification.constants;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NotificationType {

	TIME, LOCATION;

	@JsonCreator
	public static NotificationType fromValue(final String value) {
		if (value == null) {
			return null;
		}
		return NotificationType.valueOf(value.toUpperCase());
	}

}
