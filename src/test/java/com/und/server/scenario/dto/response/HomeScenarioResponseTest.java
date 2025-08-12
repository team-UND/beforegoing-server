package com.und.server.scenario.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.und.server.member.entity.Member;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.entity.Scenario;

class HomeScenarioResponseTest {

	@Test
	void from_mapsFields() {
		Scenario scenario = Scenario.builder()
			.id(1L)
			.member(Member.builder().id(1L).build())
			.scenarioName("집앞")
			.memo("메모")
			.scenarioOrder(1)
			.notification(Notification.builder().id(1L).build())
			.build();

		HomeScenarioResponse response = HomeScenarioResponse.from(scenario);

		assertThat(response.scenarioId()).isEqualTo(1L);
		assertThat(response.scenarioName()).isEqualTo("집앞");
	}

	@Test
	void listFrom_mapsList() {
		Scenario scenarioA = Scenario.builder()
			.id(1L)
			.scenarioName("A")
			.notification(Notification.builder().id(1L).build())
			.build();
		Scenario scenarioB = Scenario.builder()
			.id(2L)
			.scenarioName("B")
			.notification(Notification.builder().id(2L).build())
			.build();

		List<HomeScenarioResponse> list = HomeScenarioResponse.listFrom(List.of(scenarioA, scenarioB));

		assertThat(list).hasSize(2);
		assertThat(list.get(0).scenarioId()).isEqualTo(1L);
		assertThat(list.get(1).scenarioName()).isEqualTo("B");
	}

}


