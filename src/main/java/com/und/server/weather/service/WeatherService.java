package com.und.server.weather.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.und.server.common.exception.ServerException;
import com.und.server.weather.client.KmaWeatherClient;
import com.und.server.weather.client.OpenMeteoClient;
import com.und.server.weather.config.WeatherProperties;
import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.GridPoint;
import com.und.server.weather.dto.api.KmaWeatherResponse;
import com.und.server.weather.dto.api.OpenMeteoResponse;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.dto.response.WeatherResponse;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.util.GridConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 정보 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

	private final KmaWeatherClient kmaWeatherClient;
	private final OpenMeteoClient openMeteoClient;
	private final WeatherProperties weatherProperties;

	// 전문 데이터 추출 컴포넌트들
	private final KmaWeatherExtractor kmaWeatherExtractor;
	private final FineDustExtractor fineDustExtractor;
	private final UvIndexExtractor uvIndexExtractor;

	/**
	 * 날씨 정보 조회 (오늘/미래 자동 분기 처리)
	 */
	public WeatherResponse getWeatherInfo(WeatherRequest request) {
		log.info("날씨 정보 조회 시작: lat={}, lng={}, date={}",
			request.latitude(), request.longitude(), request.date());

		// 입력값 검증
		validateRequest(request);

		LocalDate today = LocalDate.now();
		boolean isToday = request.date().equals(today);

		try {
			if (isToday) {
				return getTodayWeather(request.latitude(), request.longitude());
			} else {
				return getFutureWeather(request.latitude(), request.longitude(), request.date());
			}
		} catch (Exception e) {
			log.error("날씨 정보 조회 중 예상치 못한 오류 발생", e);
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
	}

	/**
	 * 요청 데이터 검증
	 */
	private void validateRequest(WeatherRequest request) {
		// 위도 경도 범위 검증
		if (request.longitude() < -90 || request.latitude() > 90 ||
			request.longitude() < -180 || request.latitude() > 180) {
			throw new ServerException(WeatherErrorResult.INVALID_COORDINATES);
		}

		// 날짜 범위 검증 (오늘부터 +3일까지)
		LocalDate today = LocalDate.now();
		LocalDate maxDate = today.plusDays(3);

		if (request.date().isBefore(today) || request.date().isAfter(maxDate)) {
			throw new ServerException(WeatherErrorResult.DATE_OUT_OF_RANGE);
		}
	}

	/**
	 * 오늘 날씨 조회 (현재 4시간 구간의 최악 시나리오)
	 */
	private WeatherResponse getTodayWeather(Double latitude, Double longitude) {
		log.info("오늘 날씨 조회 시작");

		LocalDateTime now = LocalDateTime.now();
		TimeSlot currentSlot = TimeSlot.getCurrentSlot(now);
		log.info("타임슬록 {}", currentSlot);

		// 기상청 API로 날씨 조회
		WeatherType weather = getWorstWeatherForSlot(latitude, longitude, currentSlot, now.toLocalDate());

		// Open-Meteo API로 미세먼지 및 자외선 통합 조회
		OpenMeteoResponse openMeteoResponse = getOpenMeteoData(latitude, longitude, now.toLocalDate());
		List<Integer> targetHours = currentSlot.getForecastHoursFromStart();

		FineDustType fineDust =
			fineDustExtractor.extractWorstFineDust(openMeteoResponse, targetHours, now.toLocalDate());
		UvType uvIndex = uvIndexExtractor.extractWorstUvIndex(openMeteoResponse, targetHours, now.toLocalDate());

		return WeatherResponse.from(weather, fineDust, uvIndex);
	}

	/**
	 * 미래 날씨 조회 (하루 전체의 최악 시나리오)
	 */
	private WeatherResponse getFutureWeather(Double latitude, Double longitude, LocalDate date) {
		log.info("미래 날씨 조회 시작: {}", date);

		LocalDateTime now = LocalDateTime.now();
		TimeSlot currentSlot = TimeSlot.getCurrentSlot(now);

		// 기상청 API로 하루 전체 날씨 조회
		WeatherType weather = getWorstWeatherForDay(latitude, longitude, currentSlot, date);

		// Open-Meteo API로 미세먼지 및 자외선 통합 조회
		OpenMeteoResponse openMeteoResponse = getOpenMeteoData(latitude, longitude, date);

		FineDustType fineDust = fineDustExtractor.extractWorstFineDust(openMeteoResponse, null, date);
		UvType uvIndex = uvIndexExtractor.extractWorstUvIndex(openMeteoResponse, null, date);

		return WeatherResponse.from(weather, fineDust, uvIndex);
	}

	/**
	 * Open-Meteo API 통합 조회 (미세먼지 + 자외선)
	 */
	private OpenMeteoResponse getOpenMeteoData(Double latitude, Double longitude, LocalDate date) {
		try {
			return openMeteoClient.getForecast(
				latitude,
				longitude,
				"pm2_5,pm10,uv_index", // 모든 데이터 한 번에 조회
				date.toString(), // start_date
				date.toString(), // end_date
				"Asia/Seoul"
			);
		} catch (Exception e) {
			log.error("Open-Meteo API 호출 실패", e);
			throw new ServerException(WeatherErrorResult.OPEN_METEO_API_ERROR, e);
		}
	}

	/**
	 * 특정 시간 구간의 최악 날씨 조회
	 */
	private WeatherType getWorstWeatherForSlot(Double latitude, Double longitude, TimeSlot slot, LocalDate date) {
		try {
			GridPoint gridPoint = GridConverter.convertToGrid(latitude, longitude);

			// baseDate는 슬롯에 따라 조정 (SLOT_00_04는 전날 23시 발표)
			LocalDate today = LocalDate.now();
			String baseDate = slot.getBaseDate(today).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			String baseTime = slot.getBaseTime();

			log.info("기상청 API 호출 파라미터: baseDate={}, baseTime={}, nx={}, ny={}",
				baseDate, baseTime, gridPoint.x(), gridPoint.y());

			KmaWeatherResponse response = kmaWeatherClient.getVilageForecast(
				weatherProperties.getKma().getServiceKey(),
				1,      // pageNo
				1000,   // numOfRows
				"JSON", // dataType
				baseDate, // baseDate = 현재 날짜
				baseTime, // baseTime
				gridPoint.x(),
				gridPoint.y()
			);

			// PTY 데이터를 추출하여 최악 시나리오 계산 (시작시간부터 23시까지)
			List<Integer> targetHours = slot.getForecastHoursFromStart();
			return kmaWeatherExtractor.extractWorstWeather(response, targetHours, date);

		} catch (Exception e) {
			log.error("기상청 API 호출 실패", e);
			throw new ServerException(WeatherErrorResult.KMA_API_ERROR, e);
		}
	}

	/**
	 * 하루 전체의 최악 날씨 조회
	 */
	private WeatherType getWorstWeatherForDay(Double latitude, Double longitude, TimeSlot slot, LocalDate date) {
		try {
			GridPoint gridPoint = GridConverter.convertToGrid(latitude, longitude);

			// baseDate는 현재 날짜로 설정 (예보 발표 기준일)
			LocalDate today = LocalDate.now();
			String baseDate = slot.getBaseDate(today).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			String baseTime = slot.getBaseTime();

			KmaWeatherResponse response = kmaWeatherClient.getVilageForecast(
				weatherProperties.getKma().getServiceKey(),
				1,      // pageNo
				1000,   // numOfRows
				"JSON", // dataType
				baseDate, // baseDate = 현재 날짜
				baseTime, // baseTime (02시 발표)
				gridPoint.x(),
				gridPoint.y()
			);

			// 해당 날짜의 모든 시간대에서 최악 시나리오 계산
			return kmaWeatherExtractor.extractWorstWeather(response, null, date); // null = 전체 시간

		} catch (Exception e) {
			log.error("기상청 API 호출 실패", e);
			throw new ServerException(WeatherErrorResult.KMA_API_ERROR);
		}
	}


}
