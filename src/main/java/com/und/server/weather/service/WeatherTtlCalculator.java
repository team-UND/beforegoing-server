package com.und.server.weather.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.TimeSlot;

import lombok.extern.slf4j.Slf4j;

/**
 * 슬롯 기반 동적 TTL 계산 서비스
 */
@Slf4j
@Component
public class WeatherTtlCalculator {

	/**
	 * 날씨 TTL 계산 (오늘/미래 공통)
	 * 현재 슬롯의 마지막 시간 + 1시간까지 유효
	 */
	public Duration calculateTtl(TimeSlot slot) {
		LocalDateTime now = LocalDateTime.now();
		LocalTime currentTime = now.toLocalTime();

		// 슬롯의 끝 시간 (예: SLOT_04_08 -> 8시, SLOT_20_00 -> 0시)
		int endHour = slot.getEndHour();
		LocalTime deleteTime;
		
		if (endHour == 24) {
			// 24시는 다음날 00시로 처리
			deleteTime = LocalTime.of(0, 0);
		} else {
			deleteTime = LocalTime.of(endHour, 0);
		}

		// SLOT_20_00의 경우 특별 처리 (현재 시간이 20시 이후이고 삭제 시간이 00시인 경우)
		if (slot == TimeSlot.SLOT_20_00 && currentTime.getHour() >= 20) {
			// 다음날 00시까지의 시간 계산
			LocalDateTime nextDayMidnight = now.toLocalDate().plusDays(1).atTime(0, 0);
			Duration ttl = Duration.between(now, nextDayMidnight);
			log.debug("TTL 계산 (SLOT_20_00): 현재 {}시 -> 다음날 00시 = {}분",
				currentTime.getHour(), ttl.toMinutes());
			return ttl;
		}

		if (currentTime.isBefore(deleteTime)) {
			// 현재 시간이 삭제 시간 전 -> 삭제 시간까지
			Duration ttl = Duration.between(currentTime, deleteTime);
			log.debug("TTL 계산: 현재 {}시 -> 삭제 시간 {}시 = {}분",
				currentTime.getHour(), deleteTime.getHour(), ttl.toMinutes());
			return ttl;
		} else {
			return Duration.ZERO;
		}
	}

}
