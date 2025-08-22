package com.und.server.weather.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.und.server.weather.dto.api.KmaWeatherResponse;

@FeignClient(
	name = "kmaWeatherClient",
	url = "${weather.kma.base-url}"
)
public interface KmaWeatherClient {

	/**
	 * 기상청 단기예보 조회
	 *
	 * @param serviceKey 공공데이터포털에서 받은 인증키 (디코딩)
	 * @param pageNo     페이지번호 (기본값: 1)
	 * @param numOfRows  한 페이지 결과 수 (기본값: 1000)
	 * @param dataType   요청자료형식 (XML, JSON) (기본값: JSON)
	 * @param baseDate   발표일자 (YYYYMMDD)
	 * @param baseTime   발표시각 (HHMM)
	 * @param nx         예보지점의 X 좌표값
	 * @param ny         예보지점의 Y 좌표값
	 * @return 기상청 단기예보 응답
	 */
	@GetMapping("/getVilageFcst")
	KmaWeatherResponse getVilageForecast(

		@RequestParam("serviceKey") String serviceKey,
		@RequestParam("pageNo") Integer pageNo,
		@RequestParam("numOfRows") Integer numOfRows,
		@RequestParam("dataType") String dataType,
		@RequestParam("base_date") String baseDate,
		@RequestParam("base_time") String baseTime,
		@RequestParam("nx") Integer nx,
		@RequestParam("ny") Integer ny

	);

}
