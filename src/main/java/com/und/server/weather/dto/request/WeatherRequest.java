package com.und.server.weather.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * 날씨 정보 조회 요청 DTO
 */
public record WeatherRequest(
	@Schema(description = "Latitude", example = "37.5663")
	@NotNull(message = "위도는 필수입니다")
	@DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다")
	@DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다")
	Double latitude,  // 위도

	@Schema(description = "Longitude", example = "126.9779")
	@NotNull(message = "경도는 필수입니다")
	@DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다")
	@DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다")
	Double longitude, // 경도

	@NotNull(message = "조회 날짜는 필수입니다")
	LocalDate date    // 조회할 날짜 (오늘 = 4시간 구간, 미래 = 하루 전체)
) {}
