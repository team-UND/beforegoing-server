package com.und.server.weather.constants;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 4시간 단위 시간 구간 enum
 */
@Getter
@RequiredArgsConstructor
public enum TimeSlot {

	SLOT_00_04("00:00~04:00", 0, 4, List.of(0, 1, 2, 3)),
	SLOT_04_08("04:00~08:00", 4, 8, List.of(4, 5, 6, 7)),
	SLOT_08_12("08:00~12:00", 8, 12, List.of(8, 9, 10, 11)),
	SLOT_12_16("12:00~16:00", 12, 16, List.of(12, 13, 14, 15)),
	SLOT_16_20("16:00~20:00", 16, 20, List.of(16, 17, 18, 19)),
	SLOT_20_00("20:00~00:00", 20, 24, List.of(20, 21, 22, 23));

	private final String description;
	private final int startHour; // 시작 시간 (포함)
	private final int endHour;   // 종료 시간 (미포함)
	/**
	 * -- GETTER --
	 *  예보 조회 시각 리스트 반환
	 *  예: SLOT_12_16 → [12, 13, 14, 15]
	 */
	@Getter
	private final List<Integer> forecastHours;  // 예보 조회 시각 (1시간 간격)

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
	 * Open-Meteo UV 지수 시간대들 (기존 자외선 API 호환용)
	 * 실제로는 forecastHours와 동일하지만 기존 코드 호환성을 위해 유지
	 */
	public String[] getUvFields() {
		return forecastHours.stream()
			.map(hour -> "h" + hour)
			.toArray(String[]::new);
	}
}
