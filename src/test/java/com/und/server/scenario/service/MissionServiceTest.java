package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.BasicMissionRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.util.MissionTypeGroupSorter;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

	@Mock
	private MissionRepository missionRepository;

	@Mock
	private MissionTypeGroupSorter missionTypeGrouper;

	@Mock
	private com.und.server.scenario.util.ScenarioValidator scenarioValidator;

	@Mock
	private com.und.server.scenario.util.MissionValidator missionValidator;

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

		when(missionRepository.findDefaultMissions(memberId, scenarioId, date)).thenReturn(
			missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.basicMissions()).isNotEmpty();
		assertThat(result.todayMissions()).isNotEmpty();
		verify(missionRepository).findDefaultMissions(memberId, scenarioId, date);
		verify(missionTypeGrouper).groupAndSortByType(missionList, MissionType.BASIC);
		verify(missionTypeGrouper).groupAndSortByType(missionList, MissionType.TODAY);
	}


	@Test
	void Given_EmptyMissionList_When_FindMissionsByScenarioId_Then_ReturnEmptyResponse() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.now();

		when(missionRepository.findDefaultMissions(memberId, scenarioId, date)).thenReturn(
			List.of());

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		// then
		assertThat(result).isNotNull();
		assertThat(result.basicMissions()).isEmpty();
		assertThat(result.todayMissions()).isEmpty();
		verify(missionRepository).findDefaultMissions(memberId, scenarioId, date);
	}


	@Test
	void Given_UnauthorizedMember_When_FindMissionsByScenarioId_Then_ThrowServerException() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.now();

		when(missionRepository.findDefaultMissions(memberId, scenarioId, date)).thenReturn(
			List.of());

		// when & then
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		assertThat(result).isNotNull();
		assertThat(result.basicMissions()).isEmpty();
		assertThat(result.todayMissions()).isEmpty();
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

		BasicMissionRequest mission1 = BasicMissionRequest.builder()
			.content("기본 미션 1")
			.build();

		BasicMissionRequest mission2 = BasicMissionRequest.builder()
			.content("기본 미션 2")
			.build();

		List<BasicMissionRequest> missionInfoList = Arrays.asList(mission1, mission2);

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

		List<BasicMissionRequest> missionInfoList = List.of();

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
			.missions(new java.util.ArrayList<>())
			.build();

		Mission oldMission = Mission.builder()
			.id(1L)
			.scenario(oldScenario)
			.content("기존 미션")
			.missionType(MissionType.BASIC)
			.missionOrder(1)
			.build();

		List<Mission> oldMissionList = Arrays.asList(oldMission);

		BasicMissionRequest newMission1 = BasicMissionRequest.builder()
			.content("새 미션 1")
			.build();

		BasicMissionRequest newMission2 = BasicMissionRequest.builder()
			.content("새 미션 2")
			.build();

		List<BasicMissionRequest> missionInfoList = Arrays.asList(newMission1, newMission2);

		when(missionTypeGrouper.groupAndSortByType(oldScenario.getMissions(), MissionType.BASIC))
			.thenReturn(oldMissionList);

		// when
		missionService.updateBasicMission(oldScenario, missionInfoList);

		// then
		verify(missionRepository, org.mockito.Mockito.times(1)).saveAll(anyList());
	}


	@Test
	void Given_ScenarioAndEmptyBasicMissionList_When_UpdateBasicMission_Then_DeleteAllOldMissions() {
		// given
		Scenario oldScenario = Scenario.builder()
			.id(1L)
			.missions(new java.util.ArrayList<>())
			.build();

		Mission oldMission = Mission.builder()
			.id(1L)
			.scenario(oldScenario)
			.content("기존 미션")
			.missionType(MissionType.BASIC)
			.missionOrder(1)
			.build();

		List<Mission> oldMissionList = Arrays.asList(oldMission);
		List<BasicMissionRequest> missionInfoList = List.of();

		when(missionTypeGrouper.groupAndSortByType(oldScenario.getMissions(), MissionType.BASIC))
			.thenReturn(oldMissionList);

		// when
		missionService.updateBasicMission(oldScenario, missionInfoList);

		// then
		verify(missionRepository, org.mockito.Mockito.never()).saveAll(anyList());
	}


	@Test
	void Given_ScenarioAndMixedMissionList_When_UpdateBasicMission_Then_AddUpdateAndDelete() {
		// given
		Scenario oldScenario = Scenario.builder()
			.id(1L)
			.missions(new java.util.ArrayList<>())
			.build();

		Mission existingMission = Mission.builder()
			.id(1L)
			.scenario(oldScenario)
			.content("기존 미션")
			.missionType(MissionType.BASIC)
			.missionOrder(1)
			.build();

		List<Mission> oldMissionList = Arrays.asList(existingMission);

		BasicMissionRequest newMission = BasicMissionRequest.builder()
			.content("새 미션")
			.build();

		BasicMissionRequest updatedMission = BasicMissionRequest.builder()
			.missionId(1L)
			.content("수정된 미션")
			.build();

		List<BasicMissionRequest> missionInfoList = Arrays.asList(newMission, updatedMission);

		when(missionTypeGrouper.groupAndSortByType(oldScenario.getMissions(), MissionType.BASIC))
			.thenReturn(oldMissionList);

		// when
		missionService.updateBasicMission(oldScenario, missionInfoList);

		// then
		verify(missionRepository, org.mockito.Mockito.times(1)).saveAll(anyList());
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
		doThrow(new ServerException(ScenarioErrorResult.UNAUTHORIZED_ACCESS))
			.when(missionValidator).validateMissionAccessibleMember(mission, unauthorizedMemberId);

		// when & then
		assertThatThrownBy(() -> missionService.deleteTodayMission(unauthorizedMemberId, missionId))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ScenarioErrorResult.UNAUTHORIZED_ACCESS);
		verify(missionRepository).findById(missionId);
		verify(missionRepository, org.mockito.Mockito.never()).delete(any());
	}

	@Test
	void Given_ValidMissionIdAndAuthorizedMember_When_UpdateMissionCheck_Then_UpdateMissionCheckStatus() {
		// given
		Long memberId = 1L;
		Long missionId = 1L;
		Boolean isChecked = true;

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
			.content("체크할 미션")
			.isChecked(false)
			.missionType(MissionType.TODAY)
			.build();

		when(missionRepository.findById(missionId)).thenReturn(java.util.Optional.of(mission));

		// when
		missionService.updateMissionCheck(memberId, missionId, isChecked);

		// then
		verify(missionRepository).findById(missionId);
		assertThat(mission.getIsChecked()).isEqualTo(isChecked);
	}

	@Test
	void Given_NonExistentMissionId_When_UpdateMissionCheck_Then_ThrowNotFoundException() {
		// given
		Long memberId = 1L;
		Long missionId = 999L;
		Boolean isChecked = true;

		when(missionRepository.findById(missionId)).thenReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> missionService.updateMissionCheck(memberId, missionId, isChecked))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ScenarioErrorResult.NOT_FOUND_MISSION);
		verify(missionRepository).findById(missionId);
	}

	@Test
	void Given_UnauthorizedMember_When_UpdateMissionCheck_Then_ThrowUnauthorizedException() {
		// given
		Long authorizedMemberId = 1L;
		Long unauthorizedMemberId = 2L;
		Long missionId = 1L;
		Boolean isChecked = true;

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
			.content("체크할 미션")
			.isChecked(false)
			.missionType(MissionType.TODAY)
			.build();

		when(missionRepository.findById(missionId)).thenReturn(java.util.Optional.of(mission));
		doThrow(new ServerException(ScenarioErrorResult.UNAUTHORIZED_ACCESS))
			.when(missionValidator).validateMissionAccessibleMember(mission, unauthorizedMemberId);

		// when & then
		assertThatThrownBy(() -> missionService.updateMissionCheck(unauthorizedMemberId, missionId, isChecked))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ScenarioErrorResult.UNAUTHORIZED_ACCESS);
		verify(missionRepository).findById(missionId);
	}


	@Test
	void Given_ValidMissionIdAndUncheck_When_UpdateMissionCheck_Then_UpdateMissionToUnchecked() {
		// given
		Long memberId = 1L;
		Long missionId = 1L;
		Boolean isChecked = false;

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
			.content("미션")
			.isChecked(true)
			.build();

		when(missionRepository.findById(missionId))
			.thenReturn(Optional.of(mission));

		// when
		missionService.updateMissionCheck(memberId, missionId, isChecked);

		// then
		assertThat(mission.getIsChecked()).isFalse();
		verify(missionRepository).findById(missionId);
	}


	@Test
	void Given_NullDate_When_FindMissionsByScenarioId_Then_UseCurrentDate() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate nullDate = null;

		Mission mission = Mission.builder()
			.id(1L)
			.content("미션")
			.missionType(MissionType.BASIC)
			.build();

		List<Mission> missionList = List.of(mission);
		List<Mission> groupedBasicMissions = List.of(mission);
		List<Mission> groupedTodayMissions = List.of();

		when(missionRepository.findDefaultMissions(any(Long.class), any(Long.class), any())).thenReturn(missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, nullDate);

		// then
		assertThat(result).isNotNull();
		assertThat(result.basicMissions()).isNotEmpty();
		assertThat(result.todayMissions()).isEmpty();
		verify(missionRepository).findDefaultMissions(any(Long.class), any(Long.class), any());
	}


	@Test
	void Given_PastDate_When_FindMissionsByScenarioId_Then_UsePastDate() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate pastDate = LocalDate.now().minusDays(1);

		Mission mission = Mission.builder()
			.id(1L)
			.content("과거 미션")
			.missionType(MissionType.TODAY)
			.useDate(pastDate)
			.build();

		List<Mission> missionList = List.of(mission);
		List<Mission> groupedBasicMissions = List.of();
		List<Mission> groupedTodayMissions = List.of(mission);

		when(missionRepository.findMissionsByDate(memberId, scenarioId, pastDate)).thenReturn(missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, pastDate);

		// then
		assertThat(result).isNotNull();
		assertThat(result.basicMissions()).isEmpty();
		assertThat(result.todayMissions()).isNotEmpty();
		verify(missionRepository).findMissionsByDate(memberId, scenarioId, pastDate);
	}


	@Test
	void Given_FutureDate_When_FindMissionsByScenarioId_Then_UseFutureDate() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate futureDate = LocalDate.now().plusDays(1);

		Mission mission = Mission.builder()
			.id(1L)
			.content("미래 미션")
			.missionType(MissionType.TODAY)
			.useDate(futureDate)
			.build();

		List<Mission> missionList = List.of(mission);
		List<Mission> groupedBasicMissions = List.of();
		List<Mission> groupedTodayMissions = List.of(mission);

		when(missionRepository.findMissionsByDate(memberId, scenarioId, futureDate)).thenReturn(missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, futureDate);

		// then
		assertThat(result).isNotNull();
		assertThat(result.basicMissions()).isEmpty();
		assertThat(result.todayMissions()).isNotEmpty();
		verify(missionRepository).findMissionsByDate(memberId, scenarioId, futureDate);
	}


	@Test
	void Given_ScenarioAndMissionRequestWithNullMissionId_When_UpdateBasicMission_Then_AddNewMission() {
		// given
		Scenario oldScenario = Scenario.builder()
			.id(1L)
			.missions(new java.util.ArrayList<>())
			.build();

		BasicMissionRequest newMissionRequest = BasicMissionRequest.builder()
			.content("새 미션")
			.build();

		List<BasicMissionRequest> missionInfoList = List.of(newMissionRequest);

		when(missionTypeGrouper.groupAndSortByType(oldScenario.getMissions(), MissionType.BASIC))
			.thenReturn(List.of());

		// when
		missionService.updateBasicMission(oldScenario, missionInfoList);

		// then
		verify(missionRepository, org.mockito.Mockito.times(1)).saveAll(anyList());
	}


	@Test
	void Given_ScenarioAndMissionRequestWithNonExistentMissionId_When_UpdateBasicMission_Then_IgnoreMission() {
		// given
		Scenario oldScenario = Scenario.builder()
			.id(1L)
			.missions(new java.util.ArrayList<>())
			.build();

		Mission existingMission = Mission.builder()
			.id(1L)
			.content("기존 미션")
			.missionType(MissionType.BASIC)
			.build();

		BasicMissionRequest nonExistentMissionRequest = BasicMissionRequest.builder()
			.missionId(99L) // 존재하지 않는 ID
			.content("존재하지 않는 미션")
			.build();

		List<BasicMissionRequest> missionInfoList = List.of(nonExistentMissionRequest);
		List<Mission> oldMissionList = List.of(existingMission);

		when(missionTypeGrouper.groupAndSortByType(oldScenario.getMissions(), MissionType.BASIC))
			.thenReturn(oldMissionList);

		// when
		missionService.updateBasicMission(oldScenario, missionInfoList);

		// then
		verify(missionRepository, org.mockito.Mockito.times(1)).saveAll(anyList());
	}

}
