package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.MissionRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.util.MissionTypeGrouper;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

	@InjectMocks
	private MissionService missionService;

	@Mock
	private MissionRepository missionRepository;

	@Mock
	private MissionTypeGrouper missionTypeGrouper;


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

		Mockito.when(missionRepository.findAllByScenarioId(scenarioId))
			.thenReturn(missionList);

		Mockito.when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(List.of(mission1));

		Mockito.when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(List.of(mission2));

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId);

		// then
		List<MissionResponse> basicMissions = result.basicMissionList();
		List<MissionResponse> todayMissions = result.todayMissionList();

		assertThat(basicMissions).hasSize(1);
		assertThat(basicMissions.get(0).getContent()).isEqualTo("Wake up");

		assertThat(todayMissions).hasSize(1);
		assertThat(todayMissions.get(0).getContent()).isEqualTo("Drink water");
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

		Mockito.when(missionRepository.findAllByScenarioId(scenarioId))
			.thenReturn(List.of(mission));

		// when & then
		assertThatThrownBy(() -> missionService.findMissionsByScenarioId(requestMemberId, scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.UNAUTHORIZED_ACCESS.getMessage());
	}


	@Test
	void Given_ScenarioAndTodayMissionRequest_When_AddTodayMission_Then_SaveSingleMission() {
		// given
		Scenario scenario = Scenario.builder()
			.id(1L)
			.member(Member.builder().id(1L).build())
			.build();

		TodayMissionRequest request = new TodayMissionRequest("Stretch");

		ArgumentCaptor<Mission> captor = ArgumentCaptor.forClass(Mission.class);

		// when
		missionService.addTodayMission(scenario, request);

		// then
		verify(missionRepository).save(captor.capture());

		Mission saved = captor.getValue();

		assertThat(saved.getScenario()).isEqualTo(scenario);
		assertThat(saved.getContent()).isEqualTo("Stretch");
		assertThat(saved.getMissionType()).isEqualTo(MissionType.TODAY);
		assertThat(saved.getIsChecked()).isFalse();
	}


	@Test
	void Given_ScenarioAndValidBasicMissionRequestList_When_AddBasicMission_Then_SaveAllMissions() {
		Scenario scenario = Scenario.builder()
			.id(2L)
			.member(Member.builder().id(2L).build())
			.build();

		MissionRequest mission1 = new MissionRequest();
		mission1.setContent("Meditate");
		mission1.setMissionType(MissionType.BASIC);

		MissionRequest mission2 = new MissionRequest();
		mission2.setContent("Read");
		mission2.setMissionType(MissionType.BASIC);

		List<MissionRequest> requests = List.of(mission1, mission2);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Mission>> captor = ArgumentCaptor.forClass(List.class);

		missionService.addBasicMission(scenario, requests);

		verify(missionRepository).saveAll(captor.capture());

		List<Mission> savedMissions = captor.getValue();

		assertThat(savedMissions).hasSize(2);
		assertThat(savedMissions.get(0).getContent()).isEqualTo("Meditate");
		assertThat(savedMissions.get(0).getOrder()).isEqualTo(1000);
		assertThat(savedMissions.get(1).getContent()).isEqualTo("Read");
		assertThat(savedMissions.get(1).getOrder()).isEqualTo(2000);
	}


	@Test
	void Given_EmptyBasicMissionList_When_AddBasicMission_Then_DoNothing() {
		// given
		Scenario scenario = Scenario.builder()
			.id(3L)
			.member(Member.builder().id(3L).build())
			.build();

		List<MissionRequest> emptyList = List.of();

		// when
		missionService.addBasicMission(scenario, emptyList);

		// then
		verifyNoInteractions(missionRepository);
	}

}
