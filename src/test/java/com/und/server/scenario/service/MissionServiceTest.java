package com.und.server.scenario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.dto.request.BasicMissionRequest;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.MissionRepository;
import com.und.server.scenario.util.MissionTypeGroupSorter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MissionServiceTest {

	@Mock
	private MissionRepository missionRepository;

	@Mock
	private MissionTypeGroupSorter missionTypeGrouper;

	@Mock
	private com.und.server.scenario.util.ScenarioValidator scenarioValidator;

	@Mock
	private com.und.server.scenario.util.MissionValidator missionValidator;

	@Mock
	private Clock clock;

	@InjectMocks
	private MissionService missionService;

	@BeforeEach
	void setUp() {
		// Clock 설정
		when(clock.withZone(ZoneId.of("Asia/Seoul"))).thenReturn(Clock.fixed(
			LocalDate.of(2024, 1, 15).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(),
			ZoneId.of("Asia/Seoul")
		));
	}

	@Test
	void Given_ValidScenarioId_When_FindMissionsByScenarioId_Then_ReturnMissionGroupResponse() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.of(2024, 1, 15);

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

		when(missionRepository.findTodayAndFutureMissions(memberId, scenarioId, date)).thenReturn(
			missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).isNotEmpty())
			.satisfies(r -> assertThat(r.todayMissions()).isNotEmpty());
		verify(missionRepository).findTodayAndFutureMissions(memberId, scenarioId, date);
		verify(missionTypeGrouper).groupAndSortByType(missionList, MissionType.BASIC);
		verify(missionTypeGrouper).groupAndSortByType(missionList, MissionType.TODAY);
	}


	@Test
	void Given_EmptyMissionList_When_FindMissionsByScenarioId_Then_ReturnEmptyResponse() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.of(2024, 1, 15);

		when(missionRepository.findTodayAndFutureMissions(memberId, scenarioId, date)).thenReturn(
			List.of());

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).isEmpty())
			.satisfies(r -> assertThat(r.todayMissions()).isEmpty());
		verify(missionRepository).findTodayAndFutureMissions(memberId, scenarioId, date);
	}


	@Test
	void Given_UnauthorizedMember_When_FindMissionsByScenarioId_Then_ThrowServerException() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.of(2024, 1, 15);

		when(missionRepository.findTodayAndFutureMissions(memberId, scenarioId, date)).thenReturn(
			List.of());

		// when & then
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).isEmpty())
			.satisfies(r -> assertThat(r.todayMissions()).isEmpty());
	}


	@Test
	void Given_ScenarioAndTodayMissionRequest_When_AddTodayMission_Then_SaveMission() {
		// given
		Scenario scenario = Scenario.builder()
			.id(1L)
			.build();

		TodayMissionRequest missionAddInfo = new TodayMissionRequest("오늘 미션");
		LocalDate date = LocalDate.of(2024, 1, 15);

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

		when(missionRepository.findByIdAndScenarioMemberId(missionId, memberId)).thenReturn(
			java.util.Optional.of(mission));

		// when
		missionService.deleteTodayMission(memberId, missionId);

		// then
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, memberId);
		verify(missionRepository).delete(mission);
	}

	@Test
	void Given_NonExistentMissionId_When_DeleteTodayMission_Then_ThrowNotFoundException() {
		// given
		Long memberId = 1L;
		Long missionId = 999L;

		when(missionRepository.findByIdAndScenarioMemberId(missionId, memberId)).thenReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> missionService.deleteTodayMission(memberId, missionId))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ScenarioErrorResult.NOT_FOUND_MISSION);
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, memberId);
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

		when(missionRepository.findByIdAndScenarioMemberId(missionId, unauthorizedMemberId))
			.thenReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> missionService.deleteTodayMission(unauthorizedMemberId, missionId))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ScenarioErrorResult.NOT_FOUND_MISSION);
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, unauthorizedMemberId);
		verify(missionRepository, org.mockito.Mockito.never()).delete(any());
	}

	@Test
	void Given_ValidMissionIdAndAuthorizedMember_When_UpdateMissionCheck_Then_UpdateMissionCheckStatus() {
		// given
		Long memberId = 1L;
		Long missionId = 1L;
		Boolean isChecked = true;
		LocalDate date = LocalDate.of(2024, 1, 15);

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

		when(missionRepository.findByIdAndScenarioMemberId(missionId, memberId)).thenReturn(
			java.util.Optional.of(mission));

		// when
		missionService.updateMissionCheck(memberId, missionId, isChecked, date);

		// then
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, memberId);
		assertThat(mission.getIsChecked()).isEqualTo(isChecked);
	}

	@Test
	void Given_NonExistentMissionId_When_UpdateMissionCheck_Then_ThrowNotFoundException() {
		// given
		Long memberId = 1L;
		Long missionId = 999L;
		Boolean isChecked = true;
		LocalDate date = LocalDate.of(2024, 1, 15);

		when(missionRepository.findByIdAndScenarioMemberId(missionId, memberId)).thenReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> missionService.updateMissionCheck(memberId, missionId, isChecked, date))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ScenarioErrorResult.NOT_FOUND_MISSION);
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, memberId);
	}

	@Test
	void Given_UnauthorizedMember_When_UpdateMissionCheck_Then_ThrowUnauthorizedException() {
		// given
		Long authorizedMemberId = 1L;
		Long unauthorizedMemberId = 2L;
		Long missionId = 1L;
		Boolean isChecked = true;
		LocalDate date = LocalDate.of(2024, 1, 15);

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

		when(missionRepository.findByIdAndScenarioMemberId(missionId, unauthorizedMemberId))
			.thenReturn(java.util.Optional.empty());

		// when & then
		assertThatThrownBy(() -> missionService.updateMissionCheck(unauthorizedMemberId, missionId, isChecked, date))
			.isInstanceOf(ServerException.class)
			.hasFieldOrPropertyWithValue("errorResult", ScenarioErrorResult.NOT_FOUND_MISSION);
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, unauthorizedMemberId);
	}


	@Test
	void Given_ValidMissionIdAndUncheck_When_UpdateMissionCheck_Then_UpdateMissionToUnchecked() {
		// given
		Long memberId = 1L;
		Long missionId = 1L;
		Boolean isChecked = false;
		LocalDate date = LocalDate.of(2024, 1, 15);

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

		when(missionRepository.findByIdAndScenarioMemberId(missionId, memberId))
			.thenReturn(Optional.of(mission));

		// when
		missionService.updateMissionCheck(memberId, missionId, isChecked, date);

		// then
		assertThat(mission.getIsChecked()).isFalse();
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, memberId);
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

		when(missionRepository.findTodayAndFutureMissions(any(Long.class),
			any(Long.class), any())).thenReturn(missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, nullDate);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).isNotEmpty())
			.satisfies(r -> assertThat(r.todayMissions()).isEmpty());
		verify(missionRepository).findTodayAndFutureMissions(any(Long.class), any(Long.class), any());
	}


	@Test
	void Given_PastDate_When_FindMissionsByScenarioId_Then_UsePastDate() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate pastDate = LocalDate.of(2024, 1, 14);

		Mission mission = Mission.builder()
			.id(1L)
			.content("과거 미션")
			.missionType(MissionType.TODAY)
			.useDate(pastDate)
			.build();

		List<Mission> missionList = List.of(mission);
		List<Mission> groupedBasicMissions = List.of();
		List<Mission> groupedTodayMissions = List.of(mission);

		when(missionRepository.findPastMissionsByDate(memberId, scenarioId, pastDate)).thenReturn(missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, pastDate);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).isEmpty())
			.satisfies(r -> assertThat(r.todayMissions()).isNotEmpty());
		verify(missionRepository).findPastMissionsByDate(memberId, scenarioId, pastDate);
	}


	@Test
	void Given_FutureDate_When_FindMissionsByScenarioId_Then_UseFutureDate() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate futureDate = LocalDate.of(2024, 1, 16);

		Mission mission = Mission.builder()
			.id(1L)
			.content("미래 미션")
			.missionType(MissionType.TODAY)
			.useDate(futureDate)
			.build();

		List<Mission> missionList = List.of(mission);
		List<Mission> groupedBasicMissions = List.of();
		List<Mission> groupedTodayMissions = List.of(mission);

		when(missionRepository.findTodayAndFutureMissions(memberId, scenarioId, futureDate)).thenReturn(missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, futureDate);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).isEmpty())
			.satisfies(r -> assertThat(r.todayMissions()).isNotEmpty());
		verify(missionRepository).findTodayAndFutureMissions(memberId, scenarioId, futureDate);
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

	@Test
	void Given_ScenarioId_When_DeleteMissions_Then_DeleteAllMissions() {
		// given
		Long scenarioId = 1L;

		// when
		missionService.deleteMissions(scenarioId);

		// then
		verify(missionRepository).deleteByScenarioId(scenarioId);
	}


	@Test
	void Given_EmptyMissions_When_FindMissionsByScenarioId_Then_ReturnEmptyResponse() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.of(2024, 1, 15);

		when(missionRepository.findTodayAndFutureMissions(memberId, scenarioId, date))
			.thenReturn(List.of());

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).isEmpty())
			.satisfies(r -> assertThat(r.todayMissions()).isEmpty());
		verify(missionRepository).findTodayAndFutureMissions(memberId, scenarioId, date);
	}

	@Test
	void Given_NullMissions_When_FindMissionsByScenarioId_Then_ReturnEmptyResponse() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate date = LocalDate.of(2024, 1, 15);

		when(missionRepository.findTodayAndFutureMissions(memberId, scenarioId, date))
			.thenReturn(null);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).isEmpty())
			.satisfies(r -> assertThat(r.todayMissions()).isEmpty());
		verify(missionRepository).findTodayAndFutureMissions(memberId, scenarioId, date);
	}

	@Test
	void Given_EmptyBasicMissionRequests_When_AddBasicMission_Then_ReturnEmptyList() {
		// given
		Scenario scenario = Scenario.builder()
			.id(1L)
			.build();

		List<BasicMissionRequest> emptyRequests = List.of();

		// when
		List<Mission> result = missionService.addBasicMission(scenario, emptyRequests);

		// then
		assertThat(result).isEmpty();
		verify(missionRepository, never()).saveAll(anyList());
	}

	@Test
	void Given_NewMissionRequest_When_UpdateBasicMission_Then_AddNewMission() {
		// given
		Mission existingMission = Mission.builder()
			.id(1L)
			.content("기존 미션")
			.missionType(MissionType.BASIC)
			.build();

		List<Mission> missions = new java.util.ArrayList<>();
		missions.add(existingMission);

		Scenario scenario = Scenario.builder()
			.id(1L)
			.missions(missions)
			.build();

		BasicMissionRequest newMissionRequest = BasicMissionRequest.builder()
			.missionId(null) // 새로운 미션
			.content("새로운 미션")
			.build();

		List<BasicMissionRequest> requests = List.of(newMissionRequest);

		when(missionTypeGrouper.groupAndSortByType(scenario.getMissions(), MissionType.BASIC))
			.thenReturn(List.of(existingMission));

		// when
		missionService.updateBasicMission(scenario, requests);

		// then
		verify(missionRepository).saveAll(anyList());
	}

	@Test
	void Given_NonExistentMissionId_When_UpdateBasicMission_Then_SkipMission() {
		// given
		Mission existingMission = Mission.builder()
			.id(1L)
			.content("기존 미션")
			.missionType(MissionType.BASIC)
			.build();

		List<Mission> missions = new java.util.ArrayList<>();
		missions.add(existingMission);

		Scenario scenario = Scenario.builder()
			.id(1L)
			.missions(missions)
			.build();

		BasicMissionRequest nonExistentRequest = BasicMissionRequest.builder()
			.missionId(999L) // 존재하지 않는 미션 ID
			.content("존재하지 않는 미션")
			.build();

		List<BasicMissionRequest> requests = List.of(nonExistentRequest);

		when(missionTypeGrouper.groupAndSortByType(scenario.getMissions(), MissionType.BASIC))
			.thenReturn(List.of(existingMission));

		// when
		missionService.updateBasicMission(scenario, requests);

		// then
		verify(missionRepository).saveAll(anyList());
	}

	@Test
	void Given_BasicMissionAndFutureDate_When_UpdateMissionCheck_Then_UpdateFutureBasicMission() {
		// given
		Long memberId = 1L;
		Long missionId = 1L;
		Boolean isChecked = true;
		LocalDate futureDate = LocalDate.of(2024, 1, 16);

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
			.content("기본 미션")
			.isChecked(false)
			.missionType(MissionType.BASIC)
			.build();

		when(missionRepository.findByIdAndScenarioMemberId(missionId, memberId)).thenReturn(Optional.of(mission));
		when(missionRepository.findByParentMissionIdAndUseDate(missionId, futureDate))
			.thenReturn(Optional.empty());

		// when
		missionService.updateMissionCheck(memberId, missionId, isChecked, futureDate);

		// then
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, memberId);
		verify(missionRepository).findByParentMissionIdAndUseDate(missionId, futureDate);
		verify(missionRepository).save(any(Mission.class));
	}

	@Test
	void Given_BasicMissionAndFutureDateWithExistingChild_When_UpdateMissionCheckToTrue_Then_UpdateChildMission() {
		// given
		Long memberId = 1L;
		Long missionId = 1L;
		Boolean isChecked = true;
		LocalDate futureDate = LocalDate.of(2024, 1, 16);

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
			.content("기본 미션")
			.isChecked(false)
			.missionType(MissionType.BASIC)
			.build();

		Mission childMission = Mission.builder()
			.id(2L)
			.parentMissionId(missionId)
			.useDate(futureDate)
			.isChecked(false)
			.missionType(MissionType.BASIC)
			.build();

		when(missionRepository.findByIdAndScenarioMemberId(missionId, memberId)).thenReturn(Optional.of(mission));
		when(missionRepository.findByParentMissionIdAndUseDate(missionId, futureDate))
			.thenReturn(Optional.of(childMission));

		// when
		missionService.updateMissionCheck(memberId, missionId, isChecked, futureDate);

		// then
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, memberId);
		verify(missionRepository).findByParentMissionIdAndUseDate(missionId, futureDate);
		assertThat(childMission.getIsChecked()).isTrue();
	}

	@Test
	void Given_BasicMissionAndFutureDateWithExistingChild_When_UpdateMissionCheckToFalse_Then_DeleteChildMission() {
		// given
		Long memberId = 1L;
		Long missionId = 1L;
		Boolean isChecked = false;
		LocalDate futureDate = LocalDate.of(2024, 1, 16);

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
			.content("기본 미션")
			.isChecked(false)
			.missionType(MissionType.BASIC)
			.build();

		Mission childMission = Mission.builder()
			.id(2L)
			.parentMissionId(missionId)
			.useDate(futureDate)
			.isChecked(true)
			.missionType(MissionType.BASIC)
			.build();

		when(missionRepository.findByIdAndScenarioMemberId(missionId, memberId)).thenReturn(Optional.of(mission));
		when(missionRepository.findByParentMissionIdAndUseDate(missionId, futureDate))
			.thenReturn(Optional.of(childMission));

		// when
		missionService.updateMissionCheck(memberId, missionId, isChecked, futureDate);

		// then
		verify(missionRepository).findByIdAndScenarioMemberId(missionId, memberId);
		verify(missionRepository).findByParentMissionIdAndUseDate(missionId, futureDate);
		verify(missionRepository).delete(childMission);
	}

	@Test
	void Given_PastDate_When_FindMissionsByScenarioId_Then_UsePastMissions() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate pastDate = LocalDate.of(2024, 1, 10);

		Mission pastMission = Mission.builder()
			.id(1L)
			.content("과거 미션")
			.missionType(MissionType.BASIC)
			.useDate(pastDate)
			.build();

		List<Mission> missionList = List.of(pastMission);
		List<Mission> groupedBasicMissions = List.of(pastMission);
		List<Mission> groupedTodayMissions = List.of();

		when(missionRepository.findPastMissionsByDate(memberId, scenarioId, pastDate))
			.thenReturn(missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, pastDate);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).hasSize(1));
		verify(missionRepository).findPastMissionsByDate(memberId, scenarioId, pastDate);
	}

	@Test
	void Given_BasicMissionsWithOverlay_When_GetFutureCheckStatusMissions_Then_ReturnOverlayStatus() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate futureDate = LocalDate.of(2024, 1, 16);

		Mission parentMission = Mission.builder()
			.id(1L)
			.content("부모 미션")
			.missionType(MissionType.BASIC)
			.parentMissionId(null)
			.useDate(null)
			.build();

		Mission overlayMission = Mission.builder()
			.id(2L)
			.content("부모 미션")
			.missionType(MissionType.BASIC)
			.parentMissionId(1L)
			.useDate(futureDate)
			.isChecked(true)
			.build();

		List<Mission> missionList = List.of(parentMission, overlayMission);
		List<Mission> groupedBasicMissions = List.of(parentMission, overlayMission);
		List<Mission> groupedTodayMissions = List.of();

		when(missionRepository.findTodayAndFutureMissions(memberId, scenarioId, futureDate))
			.thenReturn(missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, futureDate);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).hasSize(1))
			.satisfies(r -> assertThat(r.basicMissions().get(0).isChecked()).isTrue());
		verify(missionRepository).findTodayAndFutureMissions(memberId, scenarioId, futureDate);
	}

	@Test
	void Given_BasicMissionsWithoutOverlay_When_GetFutureCheckStatusMissions_Then_ReturnUncheckedMissions() {
		// given
		Long memberId = 1L;
		Long scenarioId = 1L;
		LocalDate futureDate = LocalDate.of(2024, 1, 16);

		Mission parentMission = Mission.builder()
			.id(1L)
			.content("부모 미션")
			.missionType(MissionType.BASIC)
			.parentMissionId(null)
			.useDate(null)
			.build();

		List<Mission> missionList = List.of(parentMission);
		List<Mission> groupedBasicMissions = List.of(parentMission);
		List<Mission> groupedTodayMissions = List.of();

		when(missionRepository.findTodayAndFutureMissions(memberId, scenarioId, futureDate))
			.thenReturn(missionList);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.BASIC))
			.thenReturn(groupedBasicMissions);
		when(missionTypeGrouper.groupAndSortByType(missionList, MissionType.TODAY))
			.thenReturn(groupedTodayMissions);

		// when
		MissionGroupResponse result = missionService.findMissionsByScenarioId(memberId, scenarioId, futureDate);

		// then
		assertThat(result)
			.isNotNull()
			.satisfies(r -> assertThat(r.basicMissions()).hasSize(1))
			.satisfies(r -> assertThat(r.basicMissions().get(0).isChecked()).isFalse());
		verify(missionRepository).findTodayAndFutureMissions(memberId, scenarioId, futureDate);
	}

	// Mission entity 커버리지 향상을 위한 테스트
	@Test
	void Given_Mission_When_UpdateCheckStatus_Then_UpdateIsChecked() {
		// given
		Mission mission = Mission.builder()
			.id(1L)
			.content("테스트 미션")
			.isChecked(false)
			.missionType(MissionType.BASIC)
			.build();

		// when
		mission.updateCheckStatus(true);

		// then
		assertThat(mission.getIsChecked()).isTrue();
	}

	@Test
	void Given_Mission_When_UpdateMissionOrder_Then_UpdateOrder() {
		// given
		Mission mission = Mission.builder()
			.id(1L)
			.content("테스트 미션")
			.missionOrder(1)
			.missionType(MissionType.BASIC)
			.build();

		// when
		mission.updateMissionOrder(5);

		// then
		assertThat(mission.getMissionOrder()).isEqualTo(5);
	}

	@Test
	void Given_Mission_When_CreateFutureChildMission_Then_CreateChildMission() {
		// given
		Scenario scenario = Scenario.builder()
			.id(1L)
			.build();

		Mission parentMission = Mission.builder()
			.id(1L)
			.scenario(scenario)
			.content("부모 미션")
			.isChecked(false)
			.missionType(MissionType.BASIC)
			.build();

		LocalDate futureDate = LocalDate.of(2024, 1, 16);

		// when
		Mission childMission = parentMission.createFutureChildMission(true, futureDate);

		// then
		assertThat(childMission.getParentMissionId()).isEqualTo(parentMission.getId());
		assertThat(childMission.getUseDate()).isEqualTo(futureDate);
		assertThat(childMission.getIsChecked()).isTrue();
		assertThat(childMission.getContent()).isEqualTo(parentMission.getContent());
		assertThat(childMission.getMissionType()).isEqualTo(parentMission.getMissionType());
		assertThat(childMission.getScenario()).isEqualTo(parentMission.getScenario());
	}

	// MissionResponse 커버리지 향상을 위한 테스트
	@Test
	void Given_Mission_When_From_Then_CreateMissionResponse() {
		// given
		Mission mission = Mission.builder()
			.id(1L)
			.content("테스트 미션")
			.isChecked(true)
			.missionType(MissionType.BASIC)
			.build();

		// when
		MissionResponse response = MissionResponse.from(mission);

		// then
		assertThat(response)
			.satisfies(r -> assertThat(r.missionId()).isEqualTo(mission.getId()))
			.satisfies(r -> assertThat(r.content()).isEqualTo(mission.getContent()))
			.satisfies(r -> assertThat(r.isChecked()).isEqualTo(mission.getIsChecked()))
			.satisfies(r -> assertThat(r.missionType()).isEqualTo(mission.getMissionType()));
	}

	@Test
	void Given_MissionAndOverrideChecked_When_FromWithOverride_Then_CreateMissionResponseWithOverride() {
		// given
		Mission mission = Mission.builder()
			.id(1L)
			.content("테스트 미션")
			.isChecked(false)
			.missionType(MissionType.BASIC)
			.build();

		Boolean overrideChecked = true;

		// when
		MissionResponse response = MissionResponse.fromWithOverride(mission, overrideChecked);

		// then
		assertThat(response)
			.satisfies(r -> assertThat(r.missionId()).isEqualTo(mission.getId()))
			.satisfies(r -> assertThat(r.content()).isEqualTo(mission.getContent()))
			.satisfies(r -> assertThat(r.isChecked()).isEqualTo(overrideChecked))
			.satisfies(r -> assertThat(r.missionType()).isEqualTo(mission.getMissionType()));
	}

	@Test
	void Given_MissionList_When_ListFrom_Then_CreateMissionResponseList() {
		// given
		Mission mission1 = Mission.builder()
			.id(1L)
			.content("미션1")
			.isChecked(true)
			.missionType(MissionType.BASIC)
			.build();

		Mission mission2 = Mission.builder()
			.id(2L)
			.content("미션2")
			.isChecked(false)
			.missionType(MissionType.TODAY)
			.build();

		List<Mission> missionList = List.of(mission1, mission2);

		// when
		List<MissionResponse> responseList = MissionResponse.listFrom(missionList);

		// then
		assertThat(responseList)
			.hasSize(2)
			.satisfies(list -> assertThat(list.get(0).missionId()).isEqualTo(mission1.getId()))
			.satisfies(list -> assertThat(list.get(1).missionId()).isEqualTo(mission2.getId()));
	}

	@Test
	void Given_EmptyMissionList_When_ListFrom_Then_ReturnEmptyList() {
		// given
		List<Mission> emptyList = List.of();

		// when
		List<MissionResponse> responseList = MissionResponse.listFrom(emptyList);

		// then
		assertThat(responseList).isEmpty();
	}

	@Test
	void Given_NullMissionList_When_ListFrom_Then_ReturnEmptyList() {
		// given
		List<Mission> nullList = null;

		// when
		List<MissionResponse> responseList = MissionResponse.listFrom(nullList);

		// then
		assertThat(responseList).isEmpty();
	}

	// MissionGroupResponse 커버리지 향상을 위한 테스트
	@Test
	void Given_MissionLists_When_From_Then_CreateMissionGroupResponse() {
		// given
		Mission basicMission = Mission.builder()
			.id(1L)
			.content("기본 미션")
			.missionType(MissionType.BASIC)
			.build();

		Mission todayMission = Mission.builder()
			.id(2L)
			.content("오늘 미션")
			.missionType(MissionType.TODAY)
			.build();

		List<Mission> basicMissions = List.of(basicMission);
		List<Mission> todayMissions = List.of(todayMission);

		// when
		MissionGroupResponse response = MissionGroupResponse.from(basicMissions, todayMissions);

		// then
		assertThat(response)
			.satisfies(r -> assertThat(r.basicMissions()).hasSize(1))
			.satisfies(r -> assertThat(r.todayMissions()).hasSize(1))
			.satisfies(r -> assertThat(r.basicMissions().get(0).missionId()).isEqualTo(basicMission.getId()))
			.satisfies(r -> assertThat(r.todayMissions().get(0).missionId()).isEqualTo(todayMission.getId()));
	}

	@Test
	void Given_ScenarioIdAndMissionLists_When_From_Then_CreateMissionGroupResponseWithScenarioId() {
		// given
		Long scenarioId = 1L;
		Mission basicMission = Mission.builder()
			.id(1L)
			.content("기본 미션")
			.missionType(MissionType.BASIC)
			.build();

		Mission todayMission = Mission.builder()
			.id(2L)
			.content("오늘 미션")
			.missionType(MissionType.TODAY)
			.build();

		List<Mission> basicMissions = List.of(basicMission);
		List<Mission> todayMissions = List.of(todayMission);

		// when
		MissionGroupResponse response = MissionGroupResponse.from(scenarioId, basicMissions, todayMissions);

		// then
		assertThat(response)
			.satisfies(r -> assertThat(r.scenarioId()).isEqualTo(scenarioId))
			.satisfies(r -> assertThat(r.basicMissions()).hasSize(1))
			.satisfies(r -> assertThat(r.todayMissions()).hasSize(1));
	}

	@Test
	void Given_ScenarioIdAndFutureBasicAndTodayMissions_When_FutureFrom_Then_CreateFutureMissionGroupResponse() {
		// given
		Long scenarioId = 1L;
		MissionResponse futureBasicResponse = MissionResponse.builder()
			.missionId(1L)
			.content("미래 기본 미션")
			.isChecked(true)
			.missionType(MissionType.BASIC)
			.build();

		Mission todayMission = Mission.builder()
			.id(2L)
			.content("오늘 미션")
			.missionType(MissionType.TODAY)
			.build();

		List<MissionResponse> futureBasicResponses = List.of(futureBasicResponse);
		List<Mission> todayMissions = List.of(todayMission);

		// when
		MissionGroupResponse response =
			MissionGroupResponse.futureFrom(scenarioId, futureBasicResponses, todayMissions);

		// then
		assertThat(response)
			.satisfies(r -> assertThat(r.scenarioId()).isEqualTo(scenarioId))
			.satisfies(r -> assertThat(r.basicMissions()).hasSize(1))
			.satisfies(r -> assertThat(r.basicMissions().get(0).missionId()).isEqualTo(futureBasicResponse.missionId()))
			.satisfies(r -> assertThat(r.todayMissions()).hasSize(1))
			.satisfies(r -> assertThat(r.todayMissions().get(0).missionId()).isEqualTo(todayMission.getId()));
	}

}
