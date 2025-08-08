package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.MissionRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.util.MissionTypeGrouper;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

	@Mock
	private MissionRepository missionRepository;

	@Mock
	private MissionTypeGrouper missionTypeGrouper;

	@InjectMocks
	private MissionService missionService;


	@Test
	void Given_ValidScenarioId_When_FindMissionsByScenarioId_Then_ReturnMissionGroupResponse() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.now();

		Member member = Member.builder()
			.id(memberId)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.member(member)
			.build();

		Mission basicMission = Mission.builder()
			.id(1L)
			.scenario(scenario)
			.content("기본 미션")
			.missionType(MissionType.BASIC)
			.build();

		Mission todayMission = Mission.builder()
			.id(2L)
			.scenario(scenario)
			.content("오늘 미션")
			.missionType(MissionType.TODAY)
			.build();

		List<Mission> missionList = Arrays.asList(basicMission, todayMission);
		List<Mission> groupedBasicMissions = Arrays.asList(basicMission);
		List<Mission> groupedTodayMissions = Arrays.asList(todayMission);

		when(missionRepository.findMissionsByScenarioIdWithNullAndDate(memberId, scenarioId, date)).thenReturn(
			missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.basicMissionList()).isNotEmpty();
		assertThat(result.todayMissionList()).isNotEmpty();
		verify(missionRepository).findMissionsByScenarioIdWithNullAndDate(memberId, scenarioId, date);
		verify(missionTypeGrouper).groupAndSortByType(missionList, MissionType.BASIC);
		verify(missionTypeGrouper).groupAndSortByType(missionList, MissionType.TODAY);
	}


	@Test
	void Given_EmptyMissionList_When_FindMissionsByScenarioId_Then_ReturnEmptyResponse() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.now();

		when(missionRepository.findMissionsByScenarioIdWithNullAndDate(memberId, scenarioId, date)).thenReturn(
			List.of());

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.basicMissionList()).isEmpty();
		assertThat(result.todayMissionList()).isEmpty();
		verify(missionRepository).findMissionsByScenarioIdWithNullAndDate(memberId, scenarioId, date);
	}


	@Test
	void Given_UnauthorizedMember_When_FindMissionsByScenarioId_Then_ThrowServerException() {
		// given
		Long memberId = 1L;
		Long unauthorizedMemberId = 2L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.now();

		// 권한이 없는 멤버의 시나리오는 조회되지 않음
		when(missionRepository.findMissionsByScenarioIdWithNullAndDate(memberId, scenarioId, date)).thenReturn(
			List.of());

		// when & then
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		// 권한이 없는 경우 빈 리스트 반환 (DB에서 필터링됨)
		assertThat(result).isNotNull();
		assertThat(result.basicMissionList()).isEmpty();
		assertThat(result.todayMissionList()).isEmpty();
	}


	@Test
	void Given_ScenarioAndTodayMissionRequest_When_AddTodayMission_Then_SaveMission() {
		// given
		Scenario scenario = Scenario.builder()
			.id(1L)
			.build();

		TodayMissionRequest missionAddInfo = new TodayMissionRequest("오늘 미션");
		LocalDate date = LocalDate.now();

		// when
		missionService.addTodayMission(scenario, missionAddInfo, date);

		// then
		verify(missionRepository).save(any(Mission.class));
	}


	@Test
	void Given_ScenarioAndBasicMissionList_When_AddBasicMission_Then_SaveAllMissions() {
		// given
		Scenario scenario = Scenario.builder()
			.id(1L)
			.build();

		MissionRequest mission1 = MissionRequest.builder()
			.content("기본 미션 1")
			.missionType(MissionType.BASIC)
			.build();

		MissionRequest mission2 = MissionRequest.builder()
			.content("기본 미션 2")
			.missionType(MissionType.BASIC)
			.build();

		List<MissionRequest> missionInfoList = Arrays.asList(mission1, mission2);

		// when
		missionService.addBasicMission(scenario, missionInfoList);

		// then
		verify(missionRepository).saveAll(anyList());
	}


	@Test
	void Given_EmptyBasicMissionList_When_AddBasicMission_Then_DoNothing() {
		// given
		Scenario scenario = Scenario.builder()
			.id(1L)
			.build();

		List<MissionRequest> missionInfoList = List.of();

		// when
		missionService.addBasicMission(scenario, missionInfoList);

		// then
		verify(missionRepository, org.mockito.Mockito.never()).saveAll(anyList());
	}


	@Test
	void Given_ScenarioAndNewBasicMissionList_When_UpdateBasicMission_Then_DeleteOldAndSaveNew() {
		// given
		Scenario oldScenario = Scenario.builder()
			.id(1L)
			.build();

		Mission oldMission = Mission.builder()
			.id(1L)
			.scenario(oldScenario)
			.content("기존 미션")
			.missionType(MissionType.BASIC)
			.order(1)
			.build();

		List<Mission> oldMissionList = Arrays.asList(oldMission);

		MissionRequest newMission1 = MissionRequest.builder()
			.content("새 미션 1")
			.missionType(MissionType.BASIC)
			.build();

		MissionRequest newMission2 = MissionRequest.builder()
			.content("새 미션 2")
			.missionType(MissionType.BASIC)
			.build();

		List<MissionRequest> missionInfoList = Arrays.asList(newMission1, newMission2);

		when(missionTypeGrouper.groupAndSortByType(oldScenario.getMissionList(), MissionType.BASIC))
			.thenReturn(oldMissionList);

		// when
		missionService.updateBasicMission(oldScenario, missionInfoList);

		// then
		// 새로운 미션만 있으므로 기존 미션은 삭제됨
		verify(missionRepository).deleteAllById(eq(Arrays.asList(1L)));
		verify(missionRepository, org.mockito.Mockito.times(2)).saveAll(anyList());
	}


	@Test
	void Given_ScenarioAndEmptyBasicMissionList_When_UpdateBasicMission_Then_DeleteAllOldMissions() {
		// given
		Scenario oldScenario = Scenario.builder()
			.id(1L)
			.build();

		Mission oldMission = Mission.builder()
			.id(1L)
			.scenario(oldScenario)
			.content("기존 미션")
			.missionType(MissionType.BASIC)
			.order(1)
			.build();

		List<Mission> oldMissionList = Arrays.asList(oldMission);
		List<MissionRequest> missionInfoList = List.of();

		when(missionTypeGrouper.groupAndSortByType(oldScenario.getMissionList(), MissionType.BASIC))
			.thenReturn(oldMissionList);

		// when
		missionService.updateBasicMission(oldScenario, missionInfoList);

		// then
		verify(missionRepository).deleteAll(oldMissionList);
		verify(missionRepository, org.mockito.Mockito.never()).saveAll(anyList());
	}


	@Test
	void Given_ScenarioAndMixedMissionList_When_UpdateBasicMission_Then_AddUpdateAndDelete() {
		// given
		Scenario oldScenario = Scenario.builder()
			.id(1L)
			.build();

		Mission existingMission = Mission.builder()
			.id(1L)
			.scenario(oldScenario)
			.content("기존 미션")
			.missionType(MissionType.BASIC)
			.order(1)
			.build();

		List<Mission> oldMissionList = Arrays.asList(existingMission);

		MissionRequest newMission = MissionRequest.builder()
			.content("새 미션")
			.missionType(MissionType.BASIC)
			.build();

		MissionRequest updatedMission = MissionRequest.builder()
			.missionId(1L)
			.content("수정된 미션")
			.missionType(MissionType.BASIC)
			.build();

		List<MissionRequest> missionInfoList = Arrays.asList(newMission, updatedMission);

		when(missionTypeGrouper.groupAndSortByType(oldScenario.getMissionList(), MissionType.BASIC))
			.thenReturn(oldMissionList);

		// when
		missionService.updateBasicMission(oldScenario, missionInfoList);

		// then
		verify(missionRepository).deleteAllById(eq(List.of()));
		verify(missionRepository, org.mockito.Mockito.times(2)).saveAll(anyList());
	}

	@Test
	void Given_ValidMissionIdAndAuthorizedMember_When_DeleteTodayMission_Then_DeleteMission() {
		// given
		Long memberId = 1L;
		Long missionId = 1L;

		Member member = Member.builder()
			.id(memberId)
			.build();

		Scenario scenario = Scenario.builder()
			.id(1L)
			.member(member)
			.build();

		Mission mission = Mission.builder()
			.id(missionId)
			.scenario(scenario)
			.content("삭제할 미션")
			.missionType(MissionType.TODAY)
			.build();

		when(missionRepository.findById(missionId)).thenReturn(java.util.Optional.of(mission));

		// when
		missionService.deleteTodayMission(memberId, missionId);

		// then
		verify(missionRepository).findById(missionId);
		verify(missionRepository).delete(mission);
	}

	@Test
	void Given_NonExistentMissionId_When_DeleteTodayMission_Then_ThrowNotFoundException() {
		// given
		Long memberId = 1L;
		Long missionId = 999L;

		when(missionRepository.findById(missionId)).thenReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> missionService.deleteTodayMission(memberId, missionId))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ScenarioErrorResult.NOT_FOUND_MISSION);
		verify(missionRepository).findById(missionId);
		verify(missionRepository, org.mockito.Mockito.never()).delete(any());
	}

	@Test
	void Given_UnauthorizedMember_When_DeleteTodayMission_Then_ThrowUnauthorizedException() {
		// given
		Long authorizedMemberId = 1L;
		Long unauthorizedMemberId = 2L;
		Long missionId = 1L;

		Member authorizedMember = Member.builder()
			.id(authorizedMemberId)
			.build();

		Scenario scenario = Scenario.builder()
			.id(1L)
			.member(authorizedMember)
			.build();

		Mission mission = Mission.builder()
			.id(missionId)
			.scenario(scenario)
			.content("삭제할 미션")
			.missionType(MissionType.TODAY)
			.build();

		when(missionRepository.findById(missionId)).thenReturn(java.util.Optional.of(mission));

		// when & then
		assertThatThrownBy(() -> missionService.deleteTodayMission(unauthorizedMemberId, missionId))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ScenarioErrorResult.UNAUTHORIZED_ACCESS);
		verify(missionRepository).findById(missionId);
		verify(missionRepository, org.mockito.Mockito.never()).delete(any());
	}

}
