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
public class KmaWeatherExtractor {

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
						System.out.println(item);
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

	private WeatherType convertToWeatherType(String fcstValue) {
		try {
			int ptyValue = Integer.parseInt(fcstValue);
			return WeatherType.fromPtyValue(ptyValue);
		} catch (NumberFormatException e) {
			log.warn("PTY 값 파싱 실패: {}", fcstValue);
			return null;
		}
	}


	private boolean isValidResponse(KmaWeatherResponse response) {
		return response != null
			&& response.getResponse() != null
			&& response.getResponse().getBody() != null
			&& response.getResponse().getBody().getItems() != null;
	}

}
