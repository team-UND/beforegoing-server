package com.und.server.scenario.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MissionTypeTest {

	@Test
	void fromValue_null_returnsNull() {
		assertThat(MissionType.fromValue(null)).isNull();
	}

	@Test
	void fromValue_lowercase_returnsBasic() {
		assertThat(MissionType.fromValue("basic")).isEqualTo(MissionType.BASIC);
	}

	@Test
	void fromValue_uppercase_returnsToday() {
		assertThat(MissionType.fromValue("TODAY")).isEqualTo(MissionType.TODAY);
	}

	@Test
	void fromValue_invalid_throwsException() {
		assertThrows(IllegalArgumentException.class, () -> MissionType.fromValue("invalid"));
	}

}


