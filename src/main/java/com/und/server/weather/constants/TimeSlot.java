package com.und.server.weather.constants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 4시간 단위 시간 구간 enum
 * 각 슬롯의 시작시간부터 23시까지의 최악 시나리오를 계산
 */
@Getter
@RequiredArgsConstructor
public enum TimeSlot {

	SLOT_00_04("00:00~04:00", 0, 4),
	SLOT_04_08("04:00~08:00", 4, 8),
	SLOT_08_12("08:00~12:00", 8, 12),
	SLOT_12_16("12:00~16:00", 12, 16),
	SLOT_16_20("16:00~20:00", 16, 20),
	SLOT_20_00("20:00~00:00", 20, 24);

	private final String description;
	private final int startHour;
	private final int endHour;

	/**
	 * 현재 시간으로부터 해당하는 시간 구간 찾기
	 */
	public static TimeSlot getCurrentSlot(LocalDateTime dateTime) {
		return from(dateTime.toLocalTime());
	}

	/**
	 * LocalTime으로부터 해당하는 시간 구간 찾기
	 */
	public static TimeSlot from(LocalTime localTime) {
		int hour = localTime.getHour();

		for (TimeSlot slot : values()) {
			if (hour >= slot.startHour && hour < slot.endHour) {
				return slot;
			}
		}

		// 예외 상황 (발생하지 않아야 함)
		return SLOT_00_04;
	}

	/**
	 * 기상청 API 호출을 위한 발표시간 계산
	 * 각 구간에 맞는 가장 최근 발표시간 반환
	 */
	public String getBaseTime() {
		// 기상청 발표시간: 02, 05, 08, 11, 14, 17, 20, 23시
		return switch (this) {
			case SLOT_00_04 -> "2300"; // 전날 23시 발표
			case SLOT_04_08 -> "0200"; // 02시 발표
			case SLOT_08_12 -> "0500"; // 05시 발표
			case SLOT_12_16 -> "1100"; // 11시 발표 (08시보다 11시가 더 최근)
			case SLOT_16_20 -> "1400"; // 14시 발표
			case SLOT_20_00 -> "1700"; // 17시 발표 (20시 발표는 20시 이후에만 사용 가능)
			default -> "0200";
		};
	}

	/**
	 * 기상청 API 호출을 위한 발표일자 계산
	 * SLOT_00_04의 경우 전날 23시 발표이므로 전날 날짜 사용
	 */
	public LocalDate getBaseDate(LocalDate currentDate) {
		if(this == SLOT_00_04) {
			return currentDate.minusDays(1);
		}
		return currentDate;
	}

	/**
	 * 시작시간부터 23시까지의 예보 조회 시각 리스트 반환
	 * 예: SLOT_12_16 → [12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]
	 */
	public List<Integer> getForecastHoursFromStart() {
		List<Integer> hours = new ArrayList<>();
		for (int i = startHour; i <= 23; i++) {
			hours.add(i);
		}
		return hours;
	}

	/**
	 * 기존 호환성을 위한 메서드 (4시간 구간만)
	 * @deprecated 새로운 로직에서는 getForecastHoursFromStart() 사용 권장
	 */
	@Deprecated
	public List<Integer> getForecastHours() {
		List<Integer> hours = new ArrayList<>();
		for (int i = startHour; i < endHour; i++) {
			hours.add(i);
		}
		return hours;
	}

}
