package com.und.server.weather.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import com.und.server.weather.constants.TimeSlot;

import lombok.extern.slf4j.Slf4j;

/**
 * 슬롯 기반 동적 TTL 계산 서비스
 */
@Slf4j
@Component
public class WeatherTtlCalculator {

    /**
     * 날씨 TTL 계산 (오늘/미래 공통)
     * 현재 슬롯의 마지막 시간 + 1시간까지 유효
     */
    public Duration calculateTtl(TimeSlot slot) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        
        // 슬롯의 마지막 시간 + 1시간 (예: SLOT_04_08 -> 9시)
        LocalTime deleteTime = LocalTime.of(slot.getEndHour(), 0).plusHours(1);
        
        if (currentTime.isBefore(deleteTime)) {
            // 현재 시간이 삭제 시간 전 -> 삭제 시간까지
            Duration ttl = Duration.between(currentTime, deleteTime);
            log.debug("TTL 계산: 현재 {}시 -> 삭제 시간 {}시 = {}분", 
                currentTime.getHour(), deleteTime.getHour(), ttl.toMinutes());
            return ttl;
        } else {
            // 현재 시간이 삭제 시간 후 -> 최소 30분
            Duration ttl = Duration.ofMinutes(30);
            log.debug("TTL 계산: 삭제 시간 후 -> 기본 30분");
            return ttl;
        }
    }
    
    /**
     * 최소/최대 TTL 보장
     */
    public Duration ensureTtlBounds(Duration ttl) {
        Duration MIN_TTL = Duration.ofMinutes(30);  // 최소 30분
        Duration MAX_TTL = Duration.ofHours(6);     // 최대 6시간
        
        if (ttl.compareTo(MIN_TTL) < 0) {
            log.debug("TTL이 최소값보다 작음: {}분 -> 30분으로 조정", ttl.toMinutes());
            return MIN_TTL;
        }
        
        if (ttl.compareTo(MAX_TTL) > 0) {
            log.debug("TTL이 최대값보다 큼: {}시간 -> 6시간으로 조정", ttl.toHours());
            return MAX_TTL;
        }
        
        return ttl;
    }
}
