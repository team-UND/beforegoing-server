package com.und.server.weather.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.und.server.weather.dto.api.OpenMeteoResponse;

@FeignClient(
	name = "openMeteoClient",
	url = "${weather.open-meteo.base-url}"
)
public interface OpenMeteoClient {

	/**
	 * Open-Meteo 대기질 및 자외선 예보 조회
	 *
	 * @param latitude  위도
	 * @param longitude 경도
	 * @param hourly    시간별 데이터 변수들 (pm2_5,pm10,uv_index)
	 * @param startDate 시작 날짜 (YYYY-MM-DD)
	 * @param endDate   종료 날짜 (YYYY-MM-DD)
	 * @param timezone  시간대 (Asia/Seoul)
	 * @return Open-Meteo 대기질 및 자외선 응답
	 */
	@GetMapping("/air-quality")
	OpenMeteoResponse getForecast(

		@RequestParam("latitude") Double latitude,
		@RequestParam("longitude") Double longitude,
		@RequestParam("hourly") String hourly,
		@RequestParam("start_date") String startDate,
		@RequestParam("end_date") String endDate,
		@RequestParam("timezone") String timezone

	);

}
