package com.und.server.weather.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.und.server.weather.infrastructure.dto.OpenMeteoWeatherResponse;

@FeignClient(
	name = "openMeteoKmaClient",
	url = "${weather.open-meteo-kma.base-url}"
)
public interface OpenMeteoKmaClient {

	/**
	 * Open-Meteo KMA 날씨 예보 조회
	 *
	 * @param latitude  위도
	 * @param longitude 경도
	 * @param hourly    시간별 데이터 변수들 (weathercode,temperature_2m,precipitation_probability)
	 * @param startDate 시작 날짜 (YYYY-MM-DD)
	 * @param endDate   종료 날짜 (YYYY-MM-DD)
	 * @param timezone  시간대 (Asia/Seoul)
	 * @return Open-Meteo KMA 날씨 응답
	 */
	@GetMapping("/forecast")
	OpenMeteoWeatherResponse getWeatherForecast(

		@RequestParam("latitude") Double latitude,
		@RequestParam("longitude") Double longitude,
		@RequestParam("hourly") String hourly,
		@RequestParam("start_date") String startDate,
		@RequestParam("end_date") String endDate,
		@RequestParam("timezone") String timezone

	);

}
