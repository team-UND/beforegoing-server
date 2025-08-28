package com.und.server.weather.constants;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeSlot {

	SLOT_00_03(0, 3),
	SLOT_03_06(3, 6),
	SLOT_06_09(6, 9),
	SLOT_09_12(9, 12),
	SLOT_12_15(12, 15),
	SLOT_15_18(15, 18),
	SLOT_18_21(18, 21),
	SLOT_21_24(21, 24);

	private final int startHour;
	private final int endHour;

	public static TimeSlot getCurrentSlot(final LocalDateTime dateTime) {
		return from(dateTime.toLocalTime());
	}

	public static TimeSlot from(final LocalTime localTime) {
		int hour = localTime.getHour();

		for (TimeSlot slot : values()) {
			if (hour >= slot.startHour && hour < slot.endHour) {
				return slot;
			}
		}
		return SLOT_00_03;
	}

	public List<Integer> getForecastHours() {
		List<Integer> hours = new ArrayList<>();
		for (int i = startHour; i < endHour; i++) {
			hours.add(i);
		}
		return hours;
	}

	public static List<Integer> getAllDayHours() {
		List<Integer> hours = new ArrayList<>();
		for (int hour = 0; hour < 24; hour++) {
			hours.add(hour);
		}
		return hours;
	}

}
