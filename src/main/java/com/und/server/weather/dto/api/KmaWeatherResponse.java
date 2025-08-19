package com.und.server.weather.dto.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * 기상청 단기예보 API 응답 DTO
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KmaWeatherResponse {

	@JsonProperty("response")
	private Response response;

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Response {

		@JsonProperty("header")
		private Header header;

		@JsonProperty("body")
		private Body body;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Header {

		@JsonProperty("resultCode")
		private String resultCode;

		@JsonProperty("resultMsg")
		private String resultMsg;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Body {

		@JsonProperty("dataType")
		private String dataType;

		@JsonProperty("items")
		private Items items;

		@JsonProperty("totalCount")
		private Integer totalCount;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Items {

		@JsonProperty("item")
		private List<WeatherItem> item;
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class WeatherItem {

		@JsonProperty("baseDate")
		private String baseDate;      // 발표일자 (YYYYMMDD)

		@JsonProperty("baseTime")
		private String baseTime;      // 발표시각 (HHMM)

		@JsonProperty("category")
		private String category;      // 자료구분코드 (PTY, SKY 등)

		@JsonProperty("fcstDate")
		private String fcstDate;      // 예보일자 (YYYYMMDD)

		@JsonProperty("fcstTime")
		private String fcstTime;      // 예보시각 (HHMM)

		@JsonProperty("fcstValue")
		private String fcstValue;     // 예보값

		@JsonProperty("nx")
		private Integer nx;           // 예보지점 X 좌표

		@JsonProperty("ny")
		private Integer ny;           // 예보지점 Y 좌표
	}
}
