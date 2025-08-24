package com.und.server.weather.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.api.KmaWeatherResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KmaWeatherExtractor {

	public Map<Integer, WeatherType> extractWeatherForHours(
		KmaWeatherResponse response,
		List<Integer> targetHours,
		LocalDate date
	) {
		Map<Integer, WeatherType> result = new HashMap<>();

		if (!isValidResponse(response)) {
			return result;
		}

		KmaWeatherResponse.Response responseObj = response.response();
		KmaWeatherResponse.Body body = responseObj.body();
		KmaWeatherResponse.Items bodies = body.items();
		List<KmaWeatherResponse.WeatherItem> weatherItems = bodies.item();

		if (weatherItems == null || weatherItems.isEmpty()) {
			return result;
		}

		String targetDateStr = date.format(WeatherType.KMA_DATE_FORMATTER);

		for (KmaWeatherResponse.WeatherItem item : weatherItems) {
			String category = item.category();

			if (!"PTY".equals(category) && !"SKY".equals(category)) {
				continue;
			}
			if (!targetDateStr.equals(item.fcstDate())) {
				continue;
			}

			try {
				int hour = Integer.parseInt(item.fcstTime()) / 100;
				if (targetHours.contains(hour)) {
					System.out.println(item);
					WeatherType weather = convertToWeatherType(category, item.fcstValue());

					if (weather != null) {
						if ("PTY".equals(category)) {
							if (weather != WeatherType.DEFAULT) {
								result.put(hour, weather);
							}
							continue;
						}
						if (!result.containsKey(hour) && "SKY".equals(category)) {
							result.put(hour, weather);
						}
					}

				}
			} catch (NumberFormatException e) {
				log.warn("시간 파싱 실패: {}", item.fcstTime());
			}
		}
		log.debug("배치 날씨 추출 완료: {} (총 {}개 시간)", result.size(), targetHours.size());
		return result;
	}

	private WeatherType convertToWeatherType(String category, String fcstValue) {
		try {
			int value = Integer.parseInt(fcstValue);

			return switch (category) {
				case "PTY" -> WeatherType.fromPtyValue(value);
				case "SKY" -> WeatherType.fromSkyValue(value);
				default -> WeatherType.DEFAULT;
			};

		} catch (NumberFormatException e) {
			return WeatherType.DEFAULT;
		}
	}


	private boolean isValidResponse(KmaWeatherResponse response) {
		return response != null
			&& response.response() != null
			&& response.response().body() != null
			&& response.response().body().items() != null;
	}

}
