package com.und.server.weather.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
import com.und.server.weather.dto.cache.TimeSlotWeatherCacheData;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.exception.WeatherErrorResult;
import com.und.server.weather.util.GridConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 데이터 처리 서비스
 * API 호출 및 데이터 변환 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherDataProcessor {

    private final KmaWeatherClient kmaWeatherClient;
    private final OpenMeteoClient openMeteoClient;
    private final WeatherProperties weatherProperties;
    private final KmaWeatherExtractor kmaWeatherExtractor;
    private final FineDustExtractor fineDustExtractor;
    private final UvIndexExtractor uvIndexExtractor;

	private final FutureWeatherDecisionSelector futureWeatherDecisionSelector;

    /**
     * 오늘 슬롯 데이터 처리
     */
    public TimeSlotWeatherCacheData fetchTodaySlotData(Double latitude, Double longitude, TimeSlot currentSlot) {
        log.info("오늘 슬롯 데이터 처리 시작: slot={}", currentSlot);

        GridPoint gridPoint = GridConverter.convertToApiGrid(latitude, longitude);
        LocalDate today = LocalDate.now();

        try {
            // 동시 API 호출
            CompletableFuture<KmaWeatherResponse> weatherFuture =
                CompletableFuture.supplyAsync(() -> callKmaWeatherAPI(gridPoint, currentSlot, today));
            CompletableFuture<OpenMeteoResponse> openMeteoFuture =
                CompletableFuture.supplyAsync(() -> callOpenMeteoAPI(latitude, longitude, today));

            // 결과 대기
            KmaWeatherResponse weatherData = weatherFuture.get();
            OpenMeteoResponse dustUvData = openMeteoFuture.get();

            // 현재 슬롯 시간대 데이터 처리
            List<Integer> slotHours = currentSlot.getForecastHours();

			System.out.println(slotHours);
            Map<String, WeatherCacheData> hourlyData = processHourlyData(
                weatherData, dustUvData, slotHours, today);


			for(Map.Entry<String, WeatherCacheData> entry : hourlyData.entrySet()) {
				System.out.println(entry.getKey() + ":" + entry.getValue());
			}




            return TimeSlotWeatherCacheData.builder()
				.hours(hourlyData)
                .build();

        } catch (Exception e) {
            log.error("오늘 슬롯 데이터 처리 중 오류 발생", e);
            throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
        }
    }

    /**
     * 미래 하루 데이터 처리 (24시간 전체)
     */
    public WeatherCacheData fetchFutureDayData(Double latitude, Double longitude, LocalDate targetDate, TimeSlot currentSlot) {
        log.info("미래 하루 데이터 처리 시작: date={}, currentSlot={}", targetDate, currentSlot);

        GridPoint gridPoint = GridConverter.convertToApiGrid(latitude, longitude);
		LocalDate today = LocalDate.now();

        try {
            // 동시 API 호출
            CompletableFuture<KmaWeatherResponse> weatherFuture =
                CompletableFuture.supplyAsync(() -> callKmaWeatherAPI(gridPoint, currentSlot, today));
            CompletableFuture<OpenMeteoResponse> openMeteoFuture =
                CompletableFuture.supplyAsync(() -> callOpenMeteoAPI(latitude, longitude, targetDate));

            // 결과 대기
            KmaWeatherResponse weatherData = weatherFuture.get();
            OpenMeteoResponse dustUvData = openMeteoFuture.get();

			// 하루 전체 시간대 데이터 처리 (0~23시, 계산용)
			List<Integer> allHours = TimeSlot.getAllDayHours();

			Map<Integer, WeatherType> weatherMap = kmaWeatherExtractor.extractWeatherForHours(weatherData, allHours, targetDate);
			Map<Integer, FineDustType> dustMap = fineDustExtractor.extractDustForHours(dustUvData, allHours, targetDate);
			Map<Integer, UvType> uvMap = uvIndexExtractor.extractUvForHours(dustUvData, allHours, targetDate);

			System.out.println(weatherMap);
			System.out.println(dustMap);
			System.out.println(uvMap);

			WeatherType worstWeather = futureWeatherDecisionSelector.calculateWorstWeather(weatherMap.values().stream().toList());
			FineDustType avgDust = futureWeatherDecisionSelector.calculateWorstFineDust(dustMap.values().stream().toList());
			UvType avgUv = futureWeatherDecisionSelector.calculateWorstUv(uvMap.values().stream().toList());

			System.out.println("가장 최악의 날씨");
			System.out.println(worstWeather);
			System.out.println(avgDust);
			System.out.println(avgUv);

			return WeatherCacheData.builder()
				.weather(worstWeather)
				.dust(avgDust)
				.uv(avgUv)
				.build();

		} catch (Exception e) {
			log.error("미래 하루 전체 데이터 처리 중 오류 발생", e);
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
		}
    }

    /**
     * 시간별 데이터 처리 (배치 최적화)
     */
    private Map<String, WeatherCacheData> processHourlyData( // 얘도............Extractor 아닌가?
        KmaWeatherResponse weatherData,
        OpenMeteoResponse dustUvData,
        List<Integer> targetHours,
        LocalDate date
    ) {
        Map<String, WeatherCacheData> hourlyData = new HashMap<>();

        try {
            // 한 번에 모든 시간대 데이터 추출 (성능 최적화)
            Map<Integer, WeatherType> weatherMap = kmaWeatherExtractor.extractWeatherForHours(weatherData, targetHours, date);
            Map<Integer, FineDustType> dustMap = fineDustExtractor.extractDustForHours(dustUvData, targetHours, date);
            Map<Integer, UvType> uvMap = uvIndexExtractor.extractUvForHours(dustUvData, targetHours, date);

            // 시간별로 조합
            for (int hour : targetHours) {
                WeatherType weather = weatherMap.getOrDefault(hour, WeatherType.DEFAULT);
                FineDustType dust = dustMap.getOrDefault(hour, FineDustType.DEFAULT);
                UvType uv = uvMap.getOrDefault(hour, UvType.DEFAULT);

				WeatherCacheData hourInfo = WeatherCacheData.builder()
                    .weather(weather)
                    .dust(dust)
                    .uv(uv)
                    .build();

                hourlyData.put(String.format("%02d", hour), hourInfo);
                log.debug("시간별 데이터 조합 완료: {}시 - 날씨:{}, 미세먼지:{}, UV:{}",
                    hour, weather, dust, uv);
            }
            log.info("배치 데이터 처리 완료: 총 {}개 시간대 처리", targetHours.size());

        } catch (Exception e) {
			throw new ServerException(WeatherErrorResult.WEATHER_SERVICE_ERROR, e);
        }
        return hourlyData;
    }

    /**
     * 기상청 API 호출
     */
    private KmaWeatherResponse callKmaWeatherAPI(GridPoint gridPoint, TimeSlot slot, LocalDate date) {
        try {
            String baseDate = slot.getBaseDate(date).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime = slot.getBaseTime();

            log.debug("기상청 API 호출: baseDate={}, baseTime={}, nx={}, ny={}",
                baseDate, baseTime, gridPoint.x(), gridPoint.y());

            return kmaWeatherClient.getVilageForecast(
                weatherProperties.getKma().getServiceKey(),
                1,
                1000,
                "JSON",
                baseDate,
                baseTime,
                gridPoint.x(),
                gridPoint.y()
            );
        } catch (Exception e) {
            log.error("기상청 API 호출 실패", e);
            throw new ServerException(WeatherErrorResult.KMA_API_ERROR, e);
        }
    }

    /**
     * Open-Meteo API 호출
     */
    private OpenMeteoResponse callOpenMeteoAPI(Double latitude, Double longitude, LocalDate date) {
        try {
            return openMeteoClient.getForecast(
                latitude,
                longitude,
                "pm2_5,pm10,uv_index",
                date.toString(),
                date.toString(),
                "Asia/Seoul"
            );
        } catch (Exception e) {
            log.error("Open-Meteo API 호출 실패", e);
            throw new ServerException(WeatherErrorResult.OPEN_METEO_API_ERROR, e);
        }
    }

}
