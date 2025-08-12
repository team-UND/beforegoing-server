package com.und.server.scenario.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ScenarioErrorResult;

@ExtendWith(MockitoExtension.class)
class MissionValidatorTest {

	@InjectMocks
	private MissionValidator missionValidator;

	@Test
	void Given_TodayDate_When_ValidateTodayMissionDateRange_Then_NoException() {
		// given
		LocalDate today = LocalDate.now();
		LocalDate requestDate = LocalDate.now();

		// when & then
		assertDoesNotThrow(() -> missionValidator.validateTodayMissionDateRange(today, requestDate));
	}

	@Test
	void Given_FutureDate_When_ValidateTodayMissionDateRange_Then_NoException() {
		// given
		LocalDate today = LocalDate.now();
		LocalDate futureDate = LocalDate.now().plusDays(1);

		// when & then
		assertDoesNotThrow(() -> missionValidator.validateTodayMissionDateRange(today, futureDate));
	}

	@Test
	void Given_PastDate_When_ValidateTodayMissionDateRange_Then_ThrowException() {
		// given
		LocalDate today = LocalDate.now();
		LocalDate pastDate = LocalDate.now().minusDays(1);

		// when & then
		assertThatThrownBy(() -> missionValidator.validateTodayMissionDateRange(today, pastDate))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.INVALID_TODAY_MISSION_DATE.getMessage());
	}

	@Test
	void Given_SameMemberId_When_ValidateMissionAccessibleMember_Then_NoException() {
		// given
		Long memberId = 1L;
		Member member = Member.builder().id(memberId).build();
		Scenario scenario = Scenario.builder().member(member).build();
		Mission mission = Mission.builder().scenario(scenario).build();

		// when & then
		assertDoesNotThrow(() -> missionValidator.validateMissionAccessibleMember(mission, memberId));
	}

	@Test
	void Given_DifferentMemberId_When_ValidateMissionAccessibleMember_Then_ThrowException() {
		// given
		Long memberId = 1L;
		Long otherMemberId = 2L;
		Member member = Member.builder().id(memberId).build();
		Scenario scenario = Scenario.builder().member(member).build();
		Mission mission = Mission.builder().scenario(scenario).build();

		// when & then
		assertThatThrownBy(() -> missionValidator.validateMissionAccessibleMember(mission, otherMemberId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.UNAUTHORIZED_ACCESS.getMessage());
	}

	@Test
	void Given_BasicMissionListBelowMaxCount_When_ValidateMaxBasicMissionCount_Then_NoException() {
		// given
		List<Mission> missionList = List.of(
			Mission.builder().build(),
			Mission.builder().build(),
			Mission.builder().build()
		); // 3개 (20개 미만)

		// when & then
		assertDoesNotThrow(() -> missionValidator.validateMaxBasicMissionCount(missionList));
	}

	@Test
	void Given_BasicMissionListAtMaxCount_When_ValidateMaxBasicMissionCount_Then_ThrowException() {
		// given
		List<Mission> missionList = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			missionList.add(Mission.builder().build());
		} // 20개 (최대값)

		// when & then
		assertThatThrownBy(() -> missionValidator.validateMaxBasicMissionCount(missionList))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.MAX_MISSION_COUNT_EXCEEDED.getMessage());
	}

	@Test
	void Given_TodayMissionListBelowMaxCount_When_ValidateMaxTodayMissionCount_Then_NoException() {
		// given
		List<Mission> missionList = List.of(
			Mission.builder().build(),
			Mission.builder().build()
		); // 2개 (20개 미만)

		// when & then
		assertDoesNotThrow(() -> missionValidator.validateMaxTodayMissionCount(missionList));
	}

	@Test
	void Given_TodayMissionListAtMaxCount_When_ValidateMaxTodayMissionCount_Then_ThrowException() {
		// given
		List<Mission> missionList = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			missionList.add(Mission.builder().build());
		} // 20개 (최대값)

		// when & then
		assertThatThrownBy(() -> missionValidator.validateMaxTodayMissionCount(missionList))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.MAX_MISSION_COUNT_EXCEEDED.getMessage());
	}

	@Test
	void Given_EmptyMissionList_When_ValidateMaxBasicMissionCount_Then_NoException() {
		// given
		List<Mission> missionList = List.of();

		// when & then
		assertDoesNotThrow(() -> missionValidator.validateMaxBasicMissionCount(missionList));
	}

	@Test
	void Given_EmptyMissionList_When_ValidateMaxTodayMissionCount_Then_NoException() {
		// given
		List<Mission> missionList = List.of();

		// when & then
		assertDoesNotThrow(() -> missionValidator.validateMaxTodayMissionCount(missionList));
	}

}
