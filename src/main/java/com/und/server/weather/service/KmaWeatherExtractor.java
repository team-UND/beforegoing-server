package com.und.server.weather.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.api.KmaWeatherResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
/**
 * 날씨
 * - 오늘 : 해당 시간 : response, LocalDate, LocalTime, Slot
 * 	- 슬롯시간대별로 뽑아오고 redis올리고 LocalTime꺼 반환
 * - 미래 : 가장 최악 : response, LocalDate
 * 	- 다 받아와서 최악의 값
 */
public class KmaWeatherExtractor {


	/**
	 * 특정 시간의 날씨 추출
	 */
	public WeatherType extractWeatherForHour(
		KmaWeatherResponse response,
		int hour,
		LocalDate date
	) {
		if (!isValidResponse(response)) {
			log.warn("기상청 응답 데이터가 비어있음. response: {}", response);
			return WeatherType.DEFAULT;
		}

		KmaWeatherResponse.Response responseObj = response.getResponse();
		KmaWeatherResponse.Body body = responseObj.getBody();
		KmaWeatherResponse.Items bodies = body.getItems();
		List<KmaWeatherResponse.WeatherItem> weatherItems = bodies.getItem();

		if (weatherItems == null || weatherItems.isEmpty()) {
			log.warn("기상청 응답 아이템이 비어있음");
			return WeatherType.DEFAULT;
		}

		// 특정 시간만 추출
		List<Integer> singleHour = List.of(hour);
		List<WeatherType> weatherList = extractWeatherTypes(weatherItems, singleHour, date);

		if (weatherList.isEmpty()) {
			log.debug("강수 데이터 없음 - 날짜: {}, 시간: {}", date, hour);
			return WeatherType.DEFAULT;
		}

		WeatherType weather = weatherList.get(0);
		log.debug("시간별 날씨 추출 완료: {} (시간: {})", weather, hour);
		return weather;
	}

	/**
	 * 여러 시간의 날씨를 한 번에 추출 (성능 최적화)
	 */
	public Map<Integer, WeatherType> extractWeatherForHours(
		KmaWeatherResponse response,
		List<Integer> targetHours,
		LocalDate date
	) {
		Map<Integer, WeatherType> result = new HashMap<>();
		
		if (!isValidResponse(response)) {
			log.warn("기상청 응답 데이터가 비어있음. response: {}", response);
			return result;
		}

		KmaWeatherResponse.Response responseObj = response.getResponse();
		KmaWeatherResponse.Body body = responseObj.getBody();
		KmaWeatherResponse.Items bodies = body.getItems();
		List<KmaWeatherResponse.WeatherItem> weatherItems = bodies.getItem();

		if (weatherItems == null || weatherItems.isEmpty()) {
			log.warn("기상청 응답 아이템이 비어있음");
			return result;
		}

		String targetDateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		// 한 번만 파싱해서 시간별로 매핑
		for (KmaWeatherResponse.WeatherItem item : weatherItems) {
			if (!"PTY".equals(item.getCategory())) {
				continue;
			}
			if (!targetDateStr.equals(item.getFcstDate())) {
				continue;
			}

			try {
				int hour = Integer.parseInt(item.getFcstTime()) / 100;
				if (targetHours.contains(hour)) {
					WeatherType weather = convertToWeatherType(item.getFcstValue());
					if (weather != null) {
						result.put(hour, weather);
						log.debug("날씨 데이터 매핑: {}시 -> {}", hour, weather);
					}
				}
			} catch (NumberFormatException e) {
				log.warn("시간 파싱 실패: {}", item.getFcstTime());
			}
		}

		log.debug("배치 날씨 추출 완료: {} (총 {}개 시간)", result.size(), targetHours.size());
		return result;
	}

	public WeatherType extractWorstWeather(
		KmaWeatherResponse response,
		List<Integer> targetHours,
		LocalDate date
	) {
		if (!isValidResponse(response)) {
			log.warn("기상청 응답 데이터가 비어있음. response: {}", response);
			return WeatherType.DEFAULT;
		}

		KmaWeatherResponse.Response responseObj = response.getResponse();
		KmaWeatherResponse.Body body = responseObj.getBody();
		KmaWeatherResponse.Items bodies = body.getItems();
		List<KmaWeatherResponse.WeatherItem> weatherItems = bodies.getItem();

		if (weatherItems == null || weatherItems.isEmpty()) {
			log.warn("기상청 응답 아이템이 비어있음");
			return WeatherType.DEFAULT;
		}

		List<WeatherType> weatherList = extractWeatherTypes(weatherItems, targetHours, date);

		if (weatherList.isEmpty()) {
			log.debug("강수 데이터 없음 - 날짜: {}, 시간대: {}", date, targetHours);
			return WeatherType.DEFAULT;
		}

		WeatherType worst = WeatherType.getWorst(weatherList);
		log.debug("최악 날씨 추출 완료: {} (총 {}개 데이터)", worst, weatherList.size());
		return worst;
	}


	private List<WeatherType> extractWeatherTypes(
		List<KmaWeatherResponse.WeatherItem> items,
		List<Integer> targetHours,
		LocalDate date
	) {
		String targetDateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		List<WeatherType> weatherList = new ArrayList<>();

		for (KmaWeatherResponse.WeatherItem item : items) {

			if (!"PTY".equals(item.getCategory())) {
				continue;
			}
			if (!targetDateStr.equals(item.getFcstDate())) {
				continue;
			}
			if (targetHours != null && !isTargetHour(item.getFcstTime(), targetHours)) {
				continue;
			}

			WeatherType weather = convertToWeatherType(item.getFcstValue());
			if (weather != null) {
				System.out.println(item);
				weatherList.add(weather);
				log.debug("강수 데이터 추가: {} (시간: {}, 값: {})",
					weather, item.getFcstTime(), item.getFcstValue());
			}
		}
		return weatherList;
	}

	private WeatherType convertToWeatherType(String fcstValue) {
		try {
			int ptyValue = Integer.parseInt(fcstValue);
			return WeatherType.fromPtyValue(ptyValue);
		} catch (NumberFormatException e) {
			log.warn("PTY 값 파싱 실패: {}", fcstValue);
			return null;
		}
	}

	private boolean isTargetHour(String fcstTime, List<Integer> targetHours) {
		try {
			int hour = Integer.parseInt(fcstTime) / 100;
			return targetHours.contains(hour);
		} catch (NumberFormatException e) {
			log.warn("시간 파싱 실패: {}", fcstTime);
			return false;
		}
	}

	private boolean isValidResponse(KmaWeatherResponse response) {
		return response != null
			&& response.getResponse() != null
			&& response.getResponse().getBody() != null
			&& response.getResponse().getBody().getItems() != null;
	}

}
