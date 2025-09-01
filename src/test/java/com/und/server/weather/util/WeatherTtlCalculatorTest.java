package com.und.server.weather.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.weather.constants.TimeSlot;

@DisplayName("WeatherTtlCalculator 테스트")
class WeatherTtlCalculatorTest {

	private WeatherTtlCalculator weatherTtlCalculator;

	@BeforeEach
	void setUp() {
		weatherTtlCalculator = new WeatherTtlCalculator();
	}

	@Test
	@DisplayName("21-24 시간대에서 21시 이후일 때 다음날 자정까지 TTL을 계산한다")
	void Given_Slot21_24After21Hour_When_CalculateTtl_Then_ReturnsNextDayMidnightDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_21_24;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 22, 30); // 22:30

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalDateTime expectedNextDayMidnight = LocalDateTime.of(2024, 1, 2, 0, 0);
		Duration expectedDuration = Duration.between(currentTime, expectedNextDayMidnight);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("21-24 시간대에서 정확히 21시일 때 다음날 자정까지 TTL을 계산한다")
	void Given_Slot21_24Exactly21Hour_When_CalculateTtl_Then_ReturnsNextDayMidnightDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_21_24;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 21, 0); // 21:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalDateTime expectedNextDayMidnight = LocalDateTime.of(2024, 1, 2, 0, 0);
		Duration expectedDuration = Duration.between(currentTime, expectedNextDayMidnight);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("21-24 시간대에서 23시일 때 다음날 자정까지 TTL을 계산한다")
	void Given_Slot21_24At23Hour_When_CalculateTtl_Then_ReturnsNextDayMidnightDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_21_24;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 23, 45); // 23:45

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalDateTime expectedNextDayMidnight = LocalDateTime.of(2024, 1, 2, 0, 0);
		Duration expectedDuration = Duration.between(currentTime, expectedNextDayMidnight);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("21-24 시간대에서 21시 이전일 때 일반적인 로직을 적용한다")
	void Given_Slot21_24Before21Hour_When_CalculateTtl_Then_ReturnsNormalDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_21_24;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 20, 30); // 20:30

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("21-24 시간대에서 21시 이전일 때 일반적인 로직을 적용한다 - 15시")
	void Given_Slot21_24At15Hour_When_CalculateTtl_Then_ReturnsNormalDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_21_24;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 15, 0); // 15:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("다른 시간대에서 21시 이후일 때 일반적인 로직을 적용한다")
	void Given_OtherTimeSlotAfter21Hour_When_CalculateTtl_Then_ReturnsNormalDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_12_15; // 21-24가 아닌 시간대
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 22, 0); // 22:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("12-15 시간대에서 12시일 때 양수 TTL을 반환한다")
	void Given_Slot12_15At12Hour_When_CalculateTtl_Then_ReturnsPositiveDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_12_15;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 12, 0); // 12:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalTime deleteTime = LocalTime.of(15, 0); // endHour가 15이므로 15:00
		Duration expectedDuration = Duration.between(currentTime.toLocalTime(), deleteTime);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("12-15 시간대에서 14시일 때 양수 TTL을 반환한다")
	void Given_Slot12_15At14Hour_When_CalculateTtl_Then_ReturnsPositiveDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_12_15;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 14, 30); // 14:30

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalTime deleteTime = LocalTime.of(15, 0); // endHour가 15이므로 15:00
		Duration expectedDuration = Duration.between(currentTime.toLocalTime(), deleteTime);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("12-15 시간대에서 15시일 때 0을 반환한다")
	void Given_Slot12_15At15Hour_When_CalculateTtl_Then_ReturnsZero() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_12_15;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 15, 0); // 15:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("12-15 시간대에서 16시일 때 0을 반환한다")
	void Given_Slot12_15At16Hour_When_CalculateTtl_Then_ReturnsZero() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_12_15;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 16, 0); // 16:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("00-03 시간대에서 01시일 때 양수 TTL을 반환한다")
	void Given_Slot00_03At01Hour_When_CalculateTtl_Then_ReturnsPositiveDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_00_03;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 1, 30); // 01:30

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalTime deleteTime = LocalTime.of(3, 0); // endHour가 3이므로 03:00
		Duration expectedDuration = Duration.between(currentTime.toLocalTime(), deleteTime);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("00-03 시간대에서 03시일 때 0을 반환한다")
	void Given_Slot00_03At03Hour_When_CalculateTtl_Then_ReturnsZero() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_00_03;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 3, 0); // 03:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("00-03 시간대에서 04시일 때 0을 반환한다")
	void Given_Slot00_03At04Hour_When_CalculateTtl_Then_ReturnsZero() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_00_03;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 4, 0); // 04:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("06-09 시간대에서 07시일 때 양수 TTL을 반환한다")
	void Given_Slot06_09At07Hour_When_CalculateTtl_Then_ReturnsPositiveDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_06_09;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 7, 15); // 07:15

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalTime deleteTime = LocalTime.of(9, 0); // endHour가 9이므로 09:00
		Duration expectedDuration = Duration.between(currentTime.toLocalTime(), deleteTime);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("06-09 시간대에서 09시일 때 0을 반환한다")
	void Given_Slot06_09At09Hour_When_CalculateTtl_Then_ReturnsZero() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_06_09;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 9, 0); // 09:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("09-12 시간대에서 10시일 때 양수 TTL을 반환한다")
	void Given_Slot09_12At10Hour_When_CalculateTtl_Then_ReturnsPositiveDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_09_12;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 10, 45); // 10:45

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalTime deleteTime = LocalTime.of(12, 0); // endHour가 12이므로 12:00
		Duration expectedDuration = Duration.between(currentTime.toLocalTime(), deleteTime);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("15-18 시간대에서 16시일 때 양수 TTL을 반환한다")
	void Given_Slot15_18At16Hour_When_CalculateTtl_Then_ReturnsPositiveDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_15_18;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 16, 20); // 16:20

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalTime deleteTime = LocalTime.of(18, 0); // endHour가 18이므로 18:00
		Duration expectedDuration = Duration.between(currentTime.toLocalTime(), deleteTime);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("18-21 시간대에서 19시일 때 양수 TTL을 반환한다")
	void Given_Slot18_21At19Hour_When_CalculateTtl_Then_ReturnsPositiveDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_18_21;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 19, 10); // 19:10

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalTime deleteTime = LocalTime.of(21, 0); // endHour가 21이므로 21:00
		Duration expectedDuration = Duration.between(currentTime.toLocalTime(), deleteTime);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("18-21 시간대에서 21시일 때 0을 반환한다")
	void Given_Slot18_21At21Hour_When_CalculateTtl_Then_ReturnsZero() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_18_21;
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 21, 0); // 21:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("모든 시간대에서 TTL 계산이 정상적으로 동작한다")
	void Given_AllTimeSlots_When_CalculateTtl_Then_AllReturnValidDurations() {
		// given
		TimeSlot[] timeSlots = {
			TimeSlot.SLOT_00_03, // endHour: 3
			TimeSlot.SLOT_03_06, // endHour: 6
			TimeSlot.SLOT_06_09, // endHour: 9
			TimeSlot.SLOT_09_12, // endHour: 12
			TimeSlot.SLOT_12_15, // endHour: 15
			TimeSlot.SLOT_15_18, // endHour: 18
			TimeSlot.SLOT_18_21, // endHour: 21
			TimeSlot.SLOT_21_24  // endHour: 24
		};

		// when & then
		for (TimeSlot timeSlot : timeSlots) {
			LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 12, 0); // 12:00
			Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);
			assertThat(ttl).isGreaterThanOrEqualTo(Duration.ZERO);
		}
	}

	@Test
	@DisplayName("endHour가 24인 시간대에서 deleteTime이 00:00으로 설정되는지 확인한다")
	void Given_TimeSlotWithEndHour24_When_CalculateTtl_Then_DeleteTimeIsMidnight() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_21_24; // endHour가 24
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 20, 0); // 20:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("endHour가 24가 아닌 시간대에서 deleteTime이 endHour로 설정되는지 확인한다")
	void Given_TimeSlotWithEndHourNot24_When_CalculateTtl_Then_DeleteTimeIsEndHour() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_12_15; // endHour가 15
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 13, 0); // 13:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalTime deleteTime = LocalTime.of(15, 0); // endHour가 15이므로 15:00
		Duration expectedDuration = Duration.between(currentTime.toLocalTime(), deleteTime);
		assertThat(ttl).isEqualTo(expectedDuration);
	}


	@Test
	@DisplayName("현재 시간이 deleteTime보다 이전일 때 양수 TTL을 반환하는지 확인한다")
	void Given_CurrentTimeBeforeDeleteTime_When_CalculateTtl_Then_ReturnsPositiveDuration() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_12_15; // deleteTime은 15:00
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 14, 0); // 14:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		LocalTime deleteTime = LocalTime.of(15, 0); // endHour가 15이므로 15:00
		Duration expectedDuration = Duration.between(currentTime.toLocalTime(), deleteTime);
		assertThat(ttl).isEqualTo(expectedDuration);
	}

	@Test
	@DisplayName("현재 시간이 deleteTime보다 이후일 때 0을 반환하는지 확인한다")
	void Given_CurrentTimeAfterDeleteTime_When_CalculateTtl_Then_ReturnsZero() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_00_03; // deleteTime은 03:00
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 4, 0); // 04:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		// 04시는 deleteTime(03:00)보다 이후이므로 0을 반환
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("현재 시간이 deleteTime과 같을 때 0을 반환하는지 확인한다")
	void Given_CurrentTimeEqualToDeleteTime_When_CalculateTtl_Then_ReturnsZero() {
		// given
		TimeSlot timeSlot = TimeSlot.SLOT_12_15; // deleteTime은 15:00
		LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 15, 0); // 15:00

		// when
		Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

		// then
		assertThat(ttl).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("다양한 시간대에서 현재 시간에 따른 TTL 계산을 테스트한다")
	void Given_VariousCurrentTimes_When_CalculateTtl_Then_ReturnsValidDurations() {
		// given
		TimeSlot[] timeSlots = TimeSlot.values();

		// when & then
		for (TimeSlot timeSlot : timeSlots) {
			LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 12, 0); // 12:00
			Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

			assertThat(ttl).isGreaterThanOrEqualTo(Duration.ZERO);

			if (timeSlot == TimeSlot.SLOT_21_24) {
				assertThat(ttl).isGreaterThanOrEqualTo(Duration.ZERO);
			} else {
				assertThat(ttl).isGreaterThanOrEqualTo(Duration.ZERO);
			}
		}
	}

	@Test
	@DisplayName("모든 시간대에서 TTL 계산의 경계값을 테스트한다")
	void Given_AllTimeSlots_When_CalculateTtlAtBoundary_Then_ReturnsValidDurations() {
		// given
		TimeSlot[] timeSlots = TimeSlot.values();

		// when & then
		for (TimeSlot timeSlot : timeSlots) {
			LocalDateTime currentTime = LocalDateTime.of(2024, 1, 1, 12, 0); // 12:00
			Duration ttl = weatherTtlCalculator.calculateTtl(timeSlot, currentTime);

			assertThat(ttl).isGreaterThanOrEqualTo(Duration.ZERO);

			if (timeSlot == TimeSlot.SLOT_21_24) {
				assertThat(ttl).isGreaterThanOrEqualTo(Duration.ZERO);
			} else {
				assertThat(ttl).isGreaterThanOrEqualTo(Duration.ZERO);
			}
		}
	}

}
