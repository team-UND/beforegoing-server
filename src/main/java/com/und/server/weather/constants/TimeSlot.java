package com.und.server.weather.constants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeSlot {

	SLOT_00_04(0, 4),
	SLOT_04_08(4, 8),
	SLOT_08_12(8, 12),
	SLOT_12_16(12, 16),
	SLOT_16_20(16, 20),
	SLOT_20_00(20, 24);

	private final int startHour;
	private final int endHour;

	public static TimeSlot getCurrentSlot(LocalDateTime dateTime) {
		return from(dateTime.toLocalTime());
	}

	public static TimeSlot from(LocalTime localTime) {
		int hour = localTime.getHour();

		for (TimeSlot slot : values()) {
			if (hour >= slot.startHour && hour < slot.endHour) {
				return slot;
			}
		}
		return SLOT_00_04;
	}

	public String getBaseTime() {
		return switch (this) {
			case SLOT_00_04 -> "2300";
			case SLOT_04_08 -> "0200";
			case SLOT_08_12 -> "0500";
			case SLOT_12_16 -> "1100";
			case SLOT_16_20 -> "1400";
			case SLOT_20_00 -> "1700";
			default -> "0200";
		};
	}

	public LocalDate getBaseDate(LocalDate currentDate) {
		if (this == SLOT_00_04) {
			return currentDate.minusDays(1);
		}
		return currentDate;
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
