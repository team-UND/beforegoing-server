package com.und.server.weather.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.TimeSlot;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WeatherTtlCalculator {

	public Duration calculateTtl(TimeSlot timeSlot) {
		LocalDateTime now = LocalDateTime.now();
		LocalTime currentTime = now.toLocalTime();

		int endHour = timeSlot.getEndHour();
		LocalTime deleteTime;

		if (endHour == 24) {
			deleteTime = LocalTime.of(0, 0);
		} else {
			deleteTime = LocalTime.of(endHour, 0);
		}

		if (timeSlot == TimeSlot.SLOT_20_00 && currentTime.getHour() >= 20) {
			LocalDateTime nextDayMidnight = now.toLocalDate()
				.plusDays(1)
				.atTime(0, 0);
			return Duration.between(now, nextDayMidnight);
		}

		if (currentTime.isBefore(deleteTime)) {
			return Duration.between(currentTime, deleteTime);
		} else {
			return Duration.ZERO;
		}
	}

}
