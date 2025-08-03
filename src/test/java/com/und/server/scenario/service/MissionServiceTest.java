package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

	@InjectMocks
	private MissionService missionService;

	@Mock
	private MissionRepository missionRepository;


	@Test
	void Given_ValidMemberAndScenarioId_When_FindMissionsByScenarioId_Then_ReturnMissionResponses() {
		// given
		Long memberId = 1L;
		Long scenarioId = 10L;

		Member member = Member.builder().id(memberId).build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.build();

		Mission mission1 = Mission.builder()
			.id(101L)
			.scenario(scenario)
			.content("Wake up")
			.order(1)
			.isChecked(false)
			.missionType(MissionType.BASIC)
			.build();

		Mission mission2 = Mission.builder()
			.id(102L)
			.scenario(scenario)
			.content("Drink water")
			.order(2)
			.isChecked(true)
			.missionType(MissionType.TODAY)
			.build();

		List<Mission> missionList = List.of(mission1, mission2);

		Mockito.when(missionRepository.findAllByScenarioIdOrderByOrder(scenarioId))
			.thenReturn(missionList);

		// when
		List<MissionResponse> result = missionService.findMissionsByScenarioId(memberId, scenarioId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getContent()).isEqualTo("Wake up");
		assertThat(result.get(1).getContent()).isEqualTo("Drink water");
	}

	@Test
	void givenUnauthorizedUser_whenFindMissionsByScenarioId_thenThrowUnauthorizedException() {
		// given
		Long requestMemberId = 1L;
		Long scenarioId = 10L;

		Member owner = Member.builder().id(999L).build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(owner)
			.build();

		Mission mission = Mission.builder()
			.id(201L)
			.scenario(scenario)
			.content("Exercise")
			.order(1)
			.isChecked(false)
			.missionType(MissionType.BASIC)
			.build();

		Mockito.when(missionRepository.findAllByScenarioIdOrderByOrder(scenarioId))
			.thenReturn(List.of(mission));

		// when & then
		assertThatThrownBy(() -> missionService.findMissionsByScenarioId(requestMemberId, scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.UNAUTHORIZED_ACCESS.getMessage());
	}

}
