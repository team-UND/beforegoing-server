package com.und.server.notification.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NotificationMethodTypeTest {

	@Test
	void fromValue_null_returnsNull() {
		assertThat(NotificationMethodType.fromValue(null)).isNull();
	}

	@Test
	void fromValue_lowercase_returnsAlarm() {
		assertThat(NotificationMethodType.fromValue("alarm")).isEqualTo(NotificationMethodType.ALARM);
	}

	@Test
	void fromValue_uppercase_returnsPush() {
		assertThat(NotificationMethodType.fromValue("PUSH")).isEqualTo(NotificationMethodType.PUSH);
	}

	@Test
	void fromValue_invalid_throwsException() {
		assertThrows(IllegalArgumentException.class, () -> NotificationMethodType.fromValue("invalid"));
	}

}


