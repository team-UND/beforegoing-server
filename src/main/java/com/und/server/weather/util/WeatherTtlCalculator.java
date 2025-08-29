package com.und.server.weather.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.TimeSlot;

@Component
public class WeatherTtlCalculator {

	public Duration calculateTtl(final TimeSlot timeSlot, final LocalDateTime nowDateTime) {
		LocalTime currentTime = nowDateTime.toLocalTime();

		int endHour = timeSlot.getEndHour();
		LocalTime deleteTime;

		if (endHour == 24) {
			deleteTime = LocalTime.of(0, 0);
		} else {
			deleteTime = LocalTime.of(endHour, 0);
		}

		if (timeSlot == TimeSlot.SLOT_21_24 && currentTime.getHour() >= 21) {
			LocalDateTime nextDayMidnight = nowDateTime.toLocalDate()
				.plusDays(1)
				.atTime(0, 0);
			return Duration.between(nowDateTime, nextDayMidnight);
		}

		if (currentTime.isBefore(deleteTime)) {
			return Duration.between(currentTime, deleteTime);
		} else {
			return Duration.ZERO;
		}
	}

}
