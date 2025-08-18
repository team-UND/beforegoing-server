package com.und.server.notification.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NotificationTypeTest {

	@Test
	void fromValue_null_returnsNull() {
		assertThat(NotificationType.fromValue(null)).isNull();
	}

	@Test
	void fromValue_lowercase_returnsTime() {
		assertThat(NotificationType.fromValue("time")).isEqualTo(NotificationType.TIME);
	}

	@Test
	void fromValue_uppercase_returnsLocation() {
		assertThat(NotificationType.fromValue("LOCATION")).isEqualTo(NotificationType.LOCATION);
	}

	@Test
	void fromValue_invalid_throwsException() {
		assertThrows(IllegalArgumentException.class, () -> NotificationType.fromValue("invalid"));
	}

}


