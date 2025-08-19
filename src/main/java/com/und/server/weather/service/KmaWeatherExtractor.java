package com.und.server.weather.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.api.KmaWeatherResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KmaWeatherExtractor {

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
