package com.und.server.weather.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TimeSlot 테스트")
class TimeSlotTest {

	@Test
	@DisplayName("현재 시간대를 가져올 수 있다")
	void Given_DateTime_When_GetCurrentSlot_Then_ReturnsTimeSlot() {
		// given
		LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30); // 14:30

		// when
		TimeSlot result = TimeSlot.getCurrentSlot(dateTime);

		// then
		assertThat(result).isEqualTo(TimeSlot.SLOT_12_15);
	}


	@Test
	@DisplayName("LocalTime으로 시간대를 가져올 수 있다")
	void Given_LocalTime_When_From_Then_ReturnsTimeSlot() {
		// given
		LocalTime time = LocalTime.of(14, 30); // 14:30

		// when
		TimeSlot result = TimeSlot.from(time);

		// then
		assertThat(result).isEqualTo(TimeSlot.SLOT_12_15);
	}


	@Test
	@DisplayName("자정 시간대를 올바르게 처리한다")
	void Given_MidnightTime_When_From_Then_ReturnsSlot00_03() {
		// given
		LocalTime midnight = LocalTime.of(0, 0); // 00:00
		LocalTime earlyMorning = LocalTime.of(2, 30); // 02:30

		// when
		TimeSlot midnightSlot = TimeSlot.from(midnight);
		TimeSlot earlyMorningSlot = TimeSlot.from(earlyMorning);

		// then
		assertThat(midnightSlot).isEqualTo(TimeSlot.SLOT_00_03);
		assertThat(earlyMorningSlot).isEqualTo(TimeSlot.SLOT_00_03);
	}


	@Test
	@DisplayName("시간대 경계값을 올바르게 처리한다")
	void Given_BoundaryTimes_When_From_Then_ReturnsCorrectTimeSlot() {
		// given
		LocalTime startTime = LocalTime.of(12, 0); // 12:00
		LocalTime endTime = LocalTime.of(14, 59); // 14:59

		// when
		TimeSlot startSlot = TimeSlot.from(startTime);
		TimeSlot endSlot = TimeSlot.from(endTime);

		// then
		assertThat(startSlot).isEqualTo(TimeSlot.SLOT_12_15);
		assertThat(endSlot).isEqualTo(TimeSlot.SLOT_12_15);
	}


	@Test
	@DisplayName("시간대 경계를 벗어나면 다음 시간대로 처리한다")
	void Given_BoundaryOverflowTime_When_From_Then_ReturnsNextTimeSlot() {
		// given
		LocalTime boundaryTime = LocalTime.of(15, 0); // 15:00

		// when
		TimeSlot result = TimeSlot.from(boundaryTime);

		// then
		assertThat(result).isEqualTo(TimeSlot.SLOT_15_18);
	}


	@Test
	@DisplayName("자정 직전 시간대를 올바르게 처리한다")
	void Given_BeforeMidnightTime_When_From_Then_ReturnsSlot21_24() {
		// given
		LocalTime beforeMidnight = LocalTime.of(23, 30); // 23:30

		// when
		TimeSlot result = TimeSlot.from(beforeMidnight);

		// then
		assertThat(result).isEqualTo(TimeSlot.SLOT_21_24);
	}


	@Test
	@DisplayName("예보 시간 목록을 가져올 수 있다")
	void Given_TimeSlot_When_GetForecastHours_Then_ReturnsHourList() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_12_15; // 12:00-15:00

		// when
		List<Integer> forecastHours = timeSlot.getForecastHours();

		// then
		assertThat(forecastHours).containsExactly(12, 13, 14);
	}


	@Test
	@DisplayName("00-03 시간대의 예보 시간을 가져올 수 있다")
	void Given_Slot00_03_When_GetForecastHours_Then_ReturnsHourList() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_00_03; // 00:00-03:00

		// when
		List<Integer> forecastHours = timeSlot.getForecastHours();

		// then
		assertThat(forecastHours).containsExactly(0, 1, 2);
	}


	@Test
	@DisplayName("21-24 시간대의 예보 시간을 가져올 수 있다")
	void Given_Slot21_24_When_GetForecastHours_Then_ReturnsHourList() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_21_24; // 21:00-24:00

		// when
		List<Integer> forecastHours = timeSlot.getForecastHours();

		// then
		assertThat(forecastHours).containsExactly(21, 22, 23);
	}


	@Test
	@DisplayName("전체 하루 시간 목록을 가져올 수 있다")
	void Given_Request_When_GetAllDayHours_Then_Returns24HourList() {
		// when
		List<Integer> allDayHours = TimeSlot.getAllDayHours();

		// then
		assertThat(allDayHours).hasSize(24);
		assertThat(allDayHours.get(0)).isEqualTo(0);
		assertThat(allDayHours.get(23)).isEqualTo(23);
	}


	@Test
	@DisplayName("모든 시간대에 대해 현재 시간대를 올바르게 반환한다")
	void Given_AllTimeSlots_When_GetCurrentSlot_Then_ReturnsCorrectTimeSlot() {
		// given
		TimeSlot[] timeSlots = TimeSlot.values();

		// when & then
		for (TimeSlot timeSlot : timeSlots) {
			LocalTime middleTime = LocalTime.of(timeSlot.getStartHour() + 1, 30);
			TimeSlot result = TimeSlot.from(middleTime);
			assertThat(result).isEqualTo(timeSlot);
		}
	}


	@Test
	@DisplayName("시간대의 시작 시간과 종료 시간이 올바르다")
	void Given_TimeSlots_When_GetBoundaries_Then_ReturnsCorrectHours() {
		// given & when & then
		assertThat(TimeSlot.SLOT_00_03.getStartHour()).isEqualTo(0);
		assertThat(TimeSlot.SLOT_00_03.getEndHour()).isEqualTo(3);

		assertThat(TimeSlot.SLOT_03_06.getStartHour()).isEqualTo(3);
		assertThat(TimeSlot.SLOT_03_06.getEndHour()).isEqualTo(6);

		assertThat(TimeSlot.SLOT_06_09.getStartHour()).isEqualTo(6);
		assertThat(TimeSlot.SLOT_06_09.getEndHour()).isEqualTo(9);

		assertThat(TimeSlot.SLOT_09_12.getStartHour()).isEqualTo(9);
		assertThat(TimeSlot.SLOT_09_12.getEndHour()).isEqualTo(12);

		assertThat(TimeSlot.SLOT_12_15.getStartHour()).isEqualTo(12);
		assertThat(TimeSlot.SLOT_12_15.getEndHour()).isEqualTo(15);

		assertThat(TimeSlot.SLOT_15_18.getStartHour()).isEqualTo(15);
		assertThat(TimeSlot.SLOT_15_18.getEndHour()).isEqualTo(18);

		assertThat(TimeSlot.SLOT_18_21.getStartHour()).isEqualTo(18);
		assertThat(TimeSlot.SLOT_18_21.getEndHour()).isEqualTo(21);

		assertThat(TimeSlot.SLOT_21_24.getStartHour()).isEqualTo(21);
		assertThat(TimeSlot.SLOT_21_24.getEndHour()).isEqualTo(24);
	}


	@Test
	@DisplayName("시간대 경계에서 정확한 시간대를 반환한다")
	void Given_ExactBoundaryTimes_When_From_Then_ReturnsCorrectTimeSlots() {
		// given & when & then
		assertThat(TimeSlot.from(LocalTime.of(0, 0))).isEqualTo(TimeSlot.SLOT_00_03);
		assertThat(TimeSlot.from(LocalTime.of(3, 0))).isEqualTo(TimeSlot.SLOT_03_06);
		assertThat(TimeSlot.from(LocalTime.of(6, 0))).isEqualTo(TimeSlot.SLOT_06_09);
		assertThat(TimeSlot.from(LocalTime.of(9, 0))).isEqualTo(TimeSlot.SLOT_09_12);
		assertThat(TimeSlot.from(LocalTime.of(12, 0))).isEqualTo(TimeSlot.SLOT_12_15);
		assertThat(TimeSlot.from(LocalTime.of(15, 0))).isEqualTo(TimeSlot.SLOT_15_18);
		assertThat(TimeSlot.from(LocalTime.of(18, 0))).isEqualTo(TimeSlot.SLOT_18_21);
		assertThat(TimeSlot.from(LocalTime.of(21, 0))).isEqualTo(TimeSlot.SLOT_21_24);
	}

}
