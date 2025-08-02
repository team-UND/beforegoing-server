package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.member.entity.Member;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.repository.ScenarioRepository;

@ExtendWith(MockitoExtension.class)
class ScenarioServiceTest {

	@InjectMocks
	private ScenarioService scenarioService;

	@Mock
	private ScenarioRepository scenarioRepository;

	@Test
	void Given_memberId_When_FindScenarios_Then_ReturnScenarios() {
		//given
		final Long memberId = 1L;

		final Member member = Member.builder()
			.id(memberId)
			.build();

		final Notification notification1 = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();
		final Notification notification2 = Notification.builder()
			.id(2L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		final Scenario scenarioA = Scenario.builder()
			.id(1L)
			.member(member)
			.scenarioName("시나리오A")
			.memo("메모A")
			.order(1)
			.notification(notification1)
			.build();
		final Scenario scenarioB = Scenario.builder()
			.id(1L)
			.member(member)
			.scenarioName("시나리오B")
			.memo("메모B")
			.order(2)
			.notification(notification2)
			.build();

		final List<Scenario> scenarioList = List.of(scenarioA, scenarioB);

		//when
		Mockito
			.when(scenarioRepository.findByMemberIdOrderByOrder(memberId))
			.thenReturn(scenarioList);

		List<ScenarioResponse> result = scenarioService.findScenariosByMemberId(memberId);

		//then
		assertNotNull(result);
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0).getScenarioName()).isEqualTo("시나리오A");
		assertThat(result.get(1).getScenarioName()).isEqualTo("시나리오B");
	}

}
