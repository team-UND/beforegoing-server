package com.und.server.weather.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.cache.HourlyWeatherInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * 날씨 데이터 계산 서비스
 * 최악값, 평균값 등의 계산 로직을 담당
 */
@Slf4j
@Service
public class WeatherCalculationService {

    /**
     * 시간별 날씨 데이터에서 최악 날씨 계산
     */
    public WeatherType calculateWorstWeather(Collection<HourlyWeatherInfo> hours) {
        if (hours == null || hours.isEmpty()) {
            log.debug("시간별 날씨 데이터가 비어있음, 기본값 반환");
            return WeatherType.DEFAULT;
        }

        WeatherType worst = WeatherType.DEFAULT;
        for (HourlyWeatherInfo hour : hours) {
            if (hour != null && hour.getWeather() != null) {
                if (hour.getWeather().compareToSeverity(worst) > 0) {
                    worst = hour.getWeather();
                }
            }
        }

        log.debug("최악 날씨 계산 완료: {} (총 {}개 데이터)", worst, hours.size());
        return worst;
    }

    /**
     * 시간별 미세먼지 데이터에서 평균값 계산
     */
    public FineDustType calculateAverageDust(Collection<HourlyWeatherInfo> hours) {
        if (hours == null || hours.isEmpty()) {
            log.debug("시간별 미세먼지 데이터가 비어있음, 기본값 반환");
            return FineDustType.DEFAULT;
        }

        List<FineDustType> validDustData = hours.stream()
            .filter(hour -> hour != null && hour.getDust() != null && hour.getDust() != FineDustType.UNKNOWN)
            .map(HourlyWeatherInfo::getDust)
            .toList();

        if (validDustData.isEmpty()) {
            log.debug("유효한 미세먼지 데이터가 없음, 기본값 반환");
            return FineDustType.DEFAULT;
        }

        double averageValue = validDustData.stream()
            .mapToDouble(FineDustType::getAverageValue)
            .average()
            .orElse(0.0);

        FineDustType result = FineDustType.fromAverageValue(averageValue);
        log.debug("평균 미세먼지 계산 완료: {} (평균값: {}, 총 {}개 데이터)", 
            result, averageValue, validDustData.size());
        return result;
    }

    /**
     * 시간별 UV 데이터에서 평균값 계산
     */
    public UvType calculateAverageUv(Collection<HourlyWeatherInfo> hours) {
        if (hours == null || hours.isEmpty()) {
            log.debug("시간별 UV 데이터가 비어있음, 기본값 반환");
            return UvType.DEFAULT;
        }

        List<UvType> validUvData = hours.stream()
            .filter(hour -> hour != null && hour.getUv() != null && hour.getUv() != UvType.UNKNOWN)
            .map(HourlyWeatherInfo::getUv)
            .toList();

        if (validUvData.isEmpty()) {
            log.debug("유효한 UV 데이터가 없음, 기본값 반환");
            return UvType.DEFAULT;
        }

        double averageValue = validUvData.stream()
            .mapToDouble(UvType::getAverageValue)
            .average()
            .orElse(0.0);

        UvType result = UvType.fromAverageValue(averageValue);
        log.debug("평균 UV 지수 계산 완료: {} (평균값: {}, 총 {}개 데이터)", 
            result, averageValue, validUvData.size());
        return result;
    }
}
