package com.und.server.weather.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.api.KmaWeatherResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 기상청 날씨 데이터 전용 추출 컴포넌트
 */
@Slf4j
@Component
public class KmaWeatherExtractor {

	/**
	 * 기상청 응답에서 최악 날씨 추출
	 *
	 * @param response 기상청 API 응답
	 * @param targetHours 대상 시간대 (null이면 전체 시간)
	 * @param date 조회 날짜
	 * @return 최악 날씨 (강수 없으면 null)
	 */
	public WeatherType extractWorstWeather(KmaWeatherResponse response, List<Integer> targetHours, LocalDate date) {
		if (!isValidResponse(response)) {
			log.warn("기상청 응답 데이터가 비어있음");
			return null;
		}

		List<KmaWeatherResponse.WeatherItem> items = response.getResponse().getBody().getItems().getItem();
		if (items == null || items.isEmpty()) {
			log.warn("기상청 응답 아이템이 비어있음");
			return null;
		}

		List<WeatherType> weatherList = extractPrecipitationData(items, targetHours, date);

		if (weatherList.isEmpty()) {
			log.debug("강수 데이터 없음 - 날짜: {}, 시간대: {}", date, targetHours);
			return null; // 강수 없음
		}

		WeatherType worst = WeatherType.getWorst(weatherList);
		log.debug("최악 날씨 추출 완료: {} (총 {}개 데이터)", worst, weatherList.size());
		return worst;
	}

	/**
	 * 응답 유효성 검사
	 */
	private boolean isValidResponse(KmaWeatherResponse response) {
		return response != null
			&& response.getResponse() != null
			&& response.getResponse().getBody() != null
			&& response.getResponse().getBody().getItems() != null;
	}

	/**
	 * PTY(강수형태) 데이터 추출 및 변환
	 */
	private List<WeatherType> extractPrecipitationData(
		List<KmaWeatherResponse.WeatherItem> items,
		List<Integer> targetHours,
		LocalDate date
	) {
		String targetDateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		List<WeatherType> weatherList = new ArrayList<>();

		for (KmaWeatherResponse.WeatherItem item : items) {
			// PTY 카테고리만 처리
			if (!"PTY".equals(item.getCategory())) {
				continue;
			}

			// 날짜 필터링
			if (!targetDateStr.equals(item.getFcstDate())) {
				continue;
			}

			// 시간 필터링 (targetHours가 null이면 전체 시간 포함)
			if (targetHours != null && !isTargetHour(item.getFcstTime(), targetHours)) {
				continue;
			}

			// PTY 값을 WeatherType으로 변환
			WeatherType weather = convertToWeatherType(item.getFcstValue());
			if (weather != null) {
				weatherList.add(weather);
				log.debug("강수 데이터 추가: {} (시간: {}, 값: {})",
					weather, item.getFcstTime(), item.getFcstValue());
			}
		}

		return weatherList;
	}

	/**
	 * 시간 필터링 확인
	 */
	private boolean isTargetHour(String fcstTime, List<Integer> targetHours) {
		try {
			int hour = Integer.parseInt(fcstTime) / 100; // HHMM -> HH
			return targetHours.contains(hour);
		} catch (NumberFormatException e) {
			log.warn("시간 파싱 실패: {}", fcstTime);
			return false;
		}
	}

	/**
	 * PTY 값을 WeatherType으로 변환
	 */
	private WeatherType convertToWeatherType(String fcstValue) {
		try {
			int ptyValue = Integer.parseInt(fcstValue);
			return WeatherType.fromPtyValue(ptyValue);
		} catch (NumberFormatException e) {
			log.warn("PTY 값 파싱 실패: {}", fcstValue);
			return null;
		}
	}
}
