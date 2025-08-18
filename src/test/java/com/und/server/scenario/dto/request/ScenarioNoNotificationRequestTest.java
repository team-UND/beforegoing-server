package com.und.server.scenario.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.und.server.notification.constants.NotificationType;

class ScenarioNoNotificationRequestTest {

	@Test
	void construct_holdsValues() {
		ScenarioNoNotificationRequest req = new ScenarioNoNotificationRequest(
			"name",
			"memo",
			List.of(BasicMissionRequest.builder().content("A").build()),
			NotificationType.TIME
		);

		assertThat(req.scenarioName()).isEqualTo("name");
		assertThat(req.memo()).isEqualTo("memo");
		assertThat(req.basicMissions()).hasSize(1);
		assertThat(req.notificationType()).isEqualTo(NotificationType.TIME);
	}

}


