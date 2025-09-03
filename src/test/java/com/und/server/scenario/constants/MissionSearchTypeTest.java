package com.und.server.scenario.constants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.exception.ScenarioErrorResult;

@DisplayName("MissionSearchType 테스트")
class MissionSearchTypeTest {

	@Test
	@DisplayName("TODAY 타입의 rangeDays가 0인지 확인")
	void Given_TodayType_When_GetRangeDays_Then_ReturnZero() {
		// when
		int rangeDays = MissionSearchType.TODAY.getRangeDays();

		// then
		assertThat(rangeDays).isEqualTo(0);
	}

	@Test
	@DisplayName("PAST 타입의 rangeDays가 7인지 확인")
	void Given_PastType_When_GetRangeDays_Then_ReturnSeven() {
		// when
		int rangeDays = MissionSearchType.PAST.getRangeDays();

		// then
		assertThat(rangeDays).isEqualTo(14);
	}

	@Test
	@DisplayName("FUTURE 타입의 rangeDays가 7인지 확인")
	void Given_FutureType_When_GetRangeDays_Then_ReturnSeven() {
		// when
		int rangeDays = MissionSearchType.FUTURE.getRangeDays();

		// then
		assertThat(rangeDays).isEqualTo(14);
	}

	@Test
	@DisplayName("오늘 날짜로 요청하면 TODAY 타입을 반환")
	void Given_TodayDate_When_GetMissionSearchType_Then_ReturnToday() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);
		LocalDate requestDate = today;

		// when
		MissionSearchType result = MissionSearchType.getMissionSearchType(today, requestDate);

		// then
		assertThat(result).isEqualTo(MissionSearchType.TODAY);
	}

	@Test
	@DisplayName("null 날짜로 요청하면 TODAY 타입을 반환")
	void Given_NullDate_When_GetMissionSearchType_Then_ReturnToday() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);
		LocalDate requestDate = null;

		// when
		MissionSearchType result = MissionSearchType.getMissionSearchType(today, requestDate);

		// then
		assertThat(result).isEqualTo(MissionSearchType.TODAY);
	}

	@Test
	@DisplayName("어제 날짜로 요청하면 PAST 타입을 반환")
	void Given_YesterdayDate_When_GetMissionSearchType_Then_ReturnPast() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);
		LocalDate requestDate = today.minusDays(1);

		// when
		MissionSearchType result = MissionSearchType.getMissionSearchType(today, requestDate);

		// then
		assertThat(result).isEqualTo(MissionSearchType.PAST);
	}

	@Test
	@DisplayName("7일 전 날짜로 요청하면 PAST 타입을 반환")
	void Given_SevenDaysAgoDate_When_GetMissionSearchType_Then_ReturnPast() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);
		LocalDate requestDate = today.minusDays(7);

		// when
		MissionSearchType result = MissionSearchType.getMissionSearchType(today, requestDate);

		// then
		assertThat(result).isEqualTo(MissionSearchType.PAST);
	}

	@Test
	@DisplayName("8일 전 날짜로 요청하면 예외 발생")
	void Given_EightDaysAgoDate_When_GetMissionSearchType_Then_ThrowException() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);
		LocalDate requestDate = today.minusDays(40);

		// when & then
		assertThatThrownBy(() -> MissionSearchType.getMissionSearchType(today, requestDate))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.INVALID_MISSION_FOUND_DATE.getMessage());
	}

	@Test
	@DisplayName("내일 날짜로 요청하면 FUTURE 타입을 반환")
	void Given_TomorrowDate_When_GetMissionSearchType_Then_ReturnFuture() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);
		LocalDate requestDate = today.plusDays(1);

		// when
		MissionSearchType result = MissionSearchType.getMissionSearchType(today, requestDate);

		// then
		assertThat(result).isEqualTo(MissionSearchType.FUTURE);
	}

	@Test
	@DisplayName("7일 후 날짜로 요청하면 FUTURE 타입을 반환")
	void Given_SevenDaysLaterDate_When_GetMissionSearchType_Then_ReturnFuture() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);
		LocalDate requestDate = today.plusDays(7);

		// when
		MissionSearchType result = MissionSearchType.getMissionSearchType(today, requestDate);

		// then
		assertThat(result).isEqualTo(MissionSearchType.FUTURE);
	}

	@Test
	@DisplayName("8일 후 날짜로 요청하면 예외 발생")
	void Given_EightDaysLaterDate_When_GetMissionSearchType_Then_ThrowException() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);
		LocalDate requestDate = today.plusDays(40);

		// when & then
		assertThatThrownBy(() -> MissionSearchType.getMissionSearchType(today, requestDate))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.INVALID_MISSION_FOUND_DATE.getMessage());
	}

	@Test
	@DisplayName("범위 내 과거 날짜들로 요청하면 모두 PAST 타입을 반환")
	void Given_PastDatesInRange_When_GetMissionSearchType_Then_ReturnPast() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);

		// when & then
		for (int i = 1; i <= 7; i++) {
			LocalDate requestDate = today.minusDays(i);
			MissionSearchType result = MissionSearchType.getMissionSearchType(today, requestDate);
			assertThat(result).isEqualTo(MissionSearchType.PAST);
		}
	}

	@Test
	@DisplayName("범위 내 미래 날짜들로 요청하면 모두 FUTURE 타입을 반환")
	void Given_FutureDatesInRange_When_GetMissionSearchType_Then_ReturnFuture() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);

		// when & then
		for (int i = 1; i <= 7; i++) {
			LocalDate requestDate = today.plusDays(i);
			MissionSearchType result = MissionSearchType.getMissionSearchType(today, requestDate);
			assertThat(result).isEqualTo(MissionSearchType.FUTURE);
		}
	}

	@Test
	@DisplayName("범위를 벗어난 과거 날짜들로 요청하면 모두 예외 발생")
	void Given_PastDatesOutOfRange_When_GetMissionSearchType_Then_ThrowException() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);

		// when & then
		for (int i = 15; i <= 17; i++) {
			LocalDate requestDate = today.minusDays(i);
			assertThatThrownBy(() -> MissionSearchType.getMissionSearchType(today, requestDate))
				.isInstanceOf(ServerException.class)
				.hasMessageContaining(ScenarioErrorResult.INVALID_MISSION_FOUND_DATE.getMessage());
		}
	}

	@Test
	@DisplayName("범위를 벗어난 미래 날짜들로 요청하면 모두 예외 발생")
	void Given_FutureDatesOutOfRange_When_GetMissionSearchType_Then_ThrowException() {
		// given
		LocalDate today = LocalDate.of(2024, 1, 15);

		// when & then
		for (int i = 15; i <= 17; i++) {
			LocalDate requestDate = today.plusDays(i);
			assertThatThrownBy(() -> MissionSearchType.getMissionSearchType(today, requestDate))
				.isInstanceOf(ServerException.class)
				.hasMessageContaining(ScenarioErrorResult.INVALID_MISSION_FOUND_DATE.getMessage());
		}
	}

}
