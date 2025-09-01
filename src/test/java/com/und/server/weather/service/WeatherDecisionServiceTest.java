package com.und.server.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.TimeSlot;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.OpenMeteoWeatherApiResultDto;
import com.und.server.weather.dto.WeatherApiResultDto;
import com.und.server.weather.dto.cache.WeatherCacheData;
import com.und.server.weather.infrastructure.dto.KmaWeatherResponse;
import com.und.server.weather.infrastructure.dto.OpenMeteoResponse;
import com.und.server.weather.infrastructure.dto.OpenMeteoWeatherResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherDecisionService 테스트")
class WeatherDecisionServiceTest {

	@Mock
	private KmaWeatherExtractor kmaWeatherExtractor;

	@Mock
	private OpenMeteoWeatherExtractor openMeteoWeatherExtractor;

	@Mock
	private FineDustExtractor fineDustExtractor;

	@Mock
	private UvIndexExtractor uvIndexExtractor;

	@Mock
	private FutureWeatherDecisionSelector futureWeatherDecisionSelector;

	@InjectMocks
	private WeatherDecisionService weatherDecisionService;


	@Test
	@DisplayName("오늘 날씨 캐시 데이터를 정상적으로 생성한다")
	void Given_WeatherApiResult_When_GetTodayWeatherCacheData_Then_ReturnsHourlyData() {
		// given
		WeatherApiResultDto weatherApiResult = WeatherApiResultDto.builder()
			.kmaWeatherResponse(new KmaWeatherResponse(null))
			.openMeteoResponse(new OpenMeteoResponse(37.5665, 126.9780, "Asia/Seoul", null, null))
			.build();
		TimeSlot currentSlot = TimeSlot.SLOT_09_12;
		LocalDate today = LocalDate.of(2024, 1, 1);

		Map<Integer, WeatherType> weathersByHour = Map.of(
			9, WeatherType.SUNNY,
			10, WeatherType.CLOUDY,
			11, WeatherType.RAIN
		);
		Map<Integer, FineDustType> dustByHour = Map.of(
			9, FineDustType.GOOD,
			10, FineDustType.NORMAL,
			11, FineDustType.BAD
		);
		Map<Integer, UvType> uvByHour = Map.of(
			9, UvType.LOW,
			10, UvType.NORMAL,
			11, UvType.HIGH
		);

		when(kmaWeatherExtractor.extractWeatherForHours(any(KmaWeatherResponse.class), any(), eq(today)))
			.thenReturn(weathersByHour);
		when(fineDustExtractor.extractDustForHours(any(OpenMeteoResponse.class), any(), eq(today)))
			.thenReturn(dustByHour);
		when(uvIndexExtractor.extractUvForHours(any(OpenMeteoResponse.class), any(), eq(today)))
			.thenReturn(uvByHour);

		// when
		Map<String, WeatherCacheData> result = weatherDecisionService.getTodayWeatherCacheData(
			weatherApiResult, currentSlot, today);

		// then
		assertThat(result).isNotNull().hasSize(3);

		assertThat(result.get("09"))
			.isNotNull()
			.extracting(WeatherCacheData::weather, WeatherCacheData::fineDust, WeatherCacheData::uv)
			.containsExactly(WeatherType.SUNNY, FineDustType.GOOD, UvType.LOW);

		assertThat(result.get("11"))
			.isNotNull()
			.extracting(WeatherCacheData::weather, WeatherCacheData::fineDust, WeatherCacheData::uv)
			.containsExactly(WeatherType.RAIN, FineDustType.BAD, UvType.HIGH);
	}


	@Test
	@DisplayName("미래 날씨 캐시 데이터를 정상적으로 생성한다")
	void Given_WeatherApiResult_When_GetFutureWeatherCacheData_Then_ReturnsWorstWeatherData() {
		// given
		WeatherApiResultDto weatherApiResult = WeatherApiResultDto.builder()
			.kmaWeatherResponse(new KmaWeatherResponse(null))
			.openMeteoResponse(new OpenMeteoResponse(37.5665, 126.9780, "Asia/Seoul", null, null))
			.build();
		LocalDate targetDate = LocalDate.of(2024, 1, 2);

		Map<Integer, WeatherType> weatherMap = Map.of(
			9, WeatherType.SUNNY,
			10, WeatherType.CLOUDY,
			11, WeatherType.RAIN,
			12, WeatherType.SNOW
		);
		Map<Integer, FineDustType> dustMap = Map.of(
			9, FineDustType.GOOD,
			10, FineDustType.NORMAL,
			11, FineDustType.BAD,
			12, FineDustType.VERY_BAD
		);
		Map<Integer, UvType> uvMap = Map.of(
			9, UvType.LOW,
			10, UvType.NORMAL,
			11, UvType.HIGH,
			12, UvType.VERY_HIGH
		);

		when(kmaWeatherExtractor.extractWeatherForHours(any(KmaWeatherResponse.class), any(), eq(targetDate)))
			.thenReturn(weatherMap);
		when(fineDustExtractor.extractDustForHours(any(OpenMeteoResponse.class), any(), eq(targetDate)))
			.thenReturn(dustMap);
		when(uvIndexExtractor.extractUvForHours(any(OpenMeteoResponse.class), any(), eq(targetDate)))
			.thenReturn(uvMap);
		when(futureWeatherDecisionSelector.calculateWorstWeather(any()))
			.thenReturn(WeatherType.SNOW);
		when(futureWeatherDecisionSelector.calculateWorstFineDust(any()))
			.thenReturn(FineDustType.VERY_BAD);
		when(futureWeatherDecisionSelector.calculateWorstUv(any()))
			.thenReturn(UvType.VERY_HIGH);

		// when
		WeatherCacheData result = weatherDecisionService.getFutureWeatherCacheData(weatherApiResult, targetDate);

		// then
		assertThat(result).isNotNull();
		assertThat(result.weather()).isEqualTo(WeatherType.SNOW);
		assertThat(result.fineDust()).isEqualTo(FineDustType.VERY_BAD);
		assertThat(result.uv()).isEqualTo(UvType.VERY_HIGH);
	}


	@Test
	@DisplayName("오늘 날씨 폴백 캐시 데이터를 정상적으로 생성한다")
	void Given_OpenMeteoWeatherApiResult_When_GetTodayWeatherCacheDataFallback_Then_ReturnsHourlyData() {
		// given
		OpenMeteoWeatherApiResultDto weatherApiResult = OpenMeteoWeatherApiResultDto.builder()
			.openMeteoWeatherResponse(new OpenMeteoWeatherResponse(37.5665, 126.9780, "Asia/Seoul", null, null))
			.openMeteoResponse(new OpenMeteoResponse(37.5665, 126.9780, "Asia/Seoul", null, null))
			.build();
		TimeSlot currentSlot = TimeSlot.SLOT_15_18;
		LocalDate today = LocalDate.of(2024, 1, 1);

		Map<Integer, WeatherType> weathersByHour = Map.of(
			15, WeatherType.CLOUDY,
			16, WeatherType.OVERCAST,
			17, WeatherType.RAIN
		);
		Map<Integer, FineDustType> dustByHour = Map.of(
			15, FineDustType.NORMAL,
			16, FineDustType.BAD,
			17, FineDustType.VERY_BAD
		);
		Map<Integer, UvType> uvByHour = Map.of(
			15, UvType.NORMAL,
			16, UvType.HIGH,
			17, UvType.VERY_HIGH
		);

		when(openMeteoWeatherExtractor.extractWeatherForHours(any(OpenMeteoWeatherResponse.class), any(), eq(today)))
			.thenReturn(weathersByHour);
		when(fineDustExtractor.extractDustForHours(any(OpenMeteoResponse.class), any(), eq(today)))
			.thenReturn(dustByHour);
		when(uvIndexExtractor.extractUvForHours(any(OpenMeteoResponse.class), any(), eq(today)))
			.thenReturn(uvByHour);

		// when
		Map<String, WeatherCacheData> result = weatherDecisionService.getTodayWeatherCacheDataFallback(
			weatherApiResult, currentSlot, today);

		// then
		assertThat(result)
			.isNotNull()
			.hasSize(3);

		assertThat(result.get("15"))
			.isNotNull()
			.extracting(WeatherCacheData::weather, WeatherCacheData::fineDust, WeatherCacheData::uv)
			.containsExactly(WeatherType.CLOUDY, FineDustType.NORMAL, UvType.NORMAL);

		assertThat(result.get("17"))
			.isNotNull()
			.extracting(WeatherCacheData::weather, WeatherCacheData::fineDust, WeatherCacheData::uv)
			.containsExactly(WeatherType.RAIN, FineDustType.VERY_BAD, UvType.VERY_HIGH);
	}


	@Test
	@DisplayName("미래 날씨 폴백 캐시 데이터를 정상적으로 생성한다")
	void Given_OpenMeteoWeatherApiResult_When_GetFutureWeatherCacheDataFallback_Then_ReturnsWorstWeatherData() {
		// given
		OpenMeteoWeatherApiResultDto weatherApiResult = OpenMeteoWeatherApiResultDto.builder()
			.openMeteoWeatherResponse(new OpenMeteoWeatherResponse(37.5665, 126.9780, "Asia/Seoul", null, null))
			.openMeteoResponse(new OpenMeteoResponse(37.5665, 126.9780, "Asia/Seoul", null, null))
			.build();
		LocalDate targetDate = LocalDate.of(2024, 1, 3);

		Map<Integer, WeatherType> weatherMap = Map.of(
			9, WeatherType.SUNNY,
			10, WeatherType.CLOUDY,
			11, WeatherType.RAIN,
			12, WeatherType.SNOW
		);
		Map<Integer, FineDustType> dustMap = Map.of(
			9, FineDustType.GOOD,
			10, FineDustType.NORMAL,
			11, FineDustType.BAD,
			12, FineDustType.VERY_BAD
		);
		Map<Integer, UvType> uvMap = Map.of(
			9, UvType.LOW,
			10, UvType.NORMAL,
			11, UvType.HIGH,
			12, UvType.VERY_HIGH
		);

		when(openMeteoWeatherExtractor.extractWeatherForHours(any(OpenMeteoWeatherResponse.class), any(),
			eq(targetDate)))
			.thenReturn(weatherMap);
		when(fineDustExtractor.extractDustForHours(any(OpenMeteoResponse.class), any(), eq(targetDate)))
			.thenReturn(dustMap);
		when(uvIndexExtractor.extractUvForHours(any(OpenMeteoResponse.class), any(), eq(targetDate)))
			.thenReturn(uvMap);
		when(futureWeatherDecisionSelector.calculateWorstWeather(any()))
			.thenReturn(WeatherType.SNOW);
		when(futureWeatherDecisionSelector.calculateWorstFineDust(any()))
			.thenReturn(FineDustType.VERY_BAD);
		when(futureWeatherDecisionSelector.calculateWorstUv(any()))
			.thenReturn(UvType.VERY_HIGH);

		// when
		WeatherCacheData result =
			weatherDecisionService.getFutureWeatherCacheDataFallback(weatherApiResult, targetDate);

		// then
		assertThat(result).isNotNull();
		assertThat(result.weather()).isEqualTo(WeatherType.SNOW);
		assertThat(result.fineDust()).isEqualTo(FineDustType.VERY_BAD);
		assertThat(result.uv()).isEqualTo(UvType.VERY_HIGH);
	}


	@Test
	@DisplayName("시간별 데이터가 없을 때 기본값을 사용한다")
	void Given_EmptyHourlyData_When_GetTodayWeatherCacheData_Then_UsesDefaultValues() {
		// given
		WeatherApiResultDto weatherApiResult = WeatherApiResultDto.builder()
			.kmaWeatherResponse(new KmaWeatherResponse(null))
			.openMeteoResponse(new OpenMeteoResponse(37.5665, 126.9780, "Asia/Seoul", null, null))
			.build();
		TimeSlot currentSlot = TimeSlot.SLOT_21_24;
		LocalDate today = LocalDate.of(2024, 1, 1);

		Map<Integer, WeatherType> weathersByHour = Map.of();
		Map<Integer, FineDustType> dustByHour = Map.of();
		Map<Integer, UvType> uvByHour = Map.of();

		when(kmaWeatherExtractor.extractWeatherForHours(any(KmaWeatherResponse.class), any(), eq(today)))
			.thenReturn(weathersByHour);
		when(fineDustExtractor.extractDustForHours(any(OpenMeteoResponse.class), any(), eq(today)))
			.thenReturn(dustByHour);
		when(uvIndexExtractor.extractUvForHours(any(OpenMeteoResponse.class), any(), eq(today)))
			.thenReturn(uvByHour);

		// when
		Map<String, WeatherCacheData> result = weatherDecisionService.getTodayWeatherCacheData(
			weatherApiResult, currentSlot, today);

		// then
		assertThat(result)
			.isNotNull()
			.hasSize(3);

		assertThat(result.get("21"))
			.isNotNull()
			.extracting(WeatherCacheData::weather, WeatherCacheData::fineDust, WeatherCacheData::uv)
			.containsExactly(WeatherType.DEFAULT, FineDustType.DEFAULT, UvType.DEFAULT);

		assertThat(result.get("23"))
			.isNotNull()
			.extracting(WeatherCacheData::weather, WeatherCacheData::fineDust, WeatherCacheData::uv)
			.containsExactly(WeatherType.DEFAULT, FineDustType.DEFAULT, UvType.DEFAULT);
	}


	@Test
	@DisplayName("다른 시간대에서도 정상적으로 데이터를 생성한다")
	void Given_DifferentTimeSlot_When_GetTodayWeatherCacheData_Then_ReturnsCorrectData() {
		// given
		WeatherApiResultDto weatherApiResult = WeatherApiResultDto.builder()
			.kmaWeatherResponse(new KmaWeatherResponse(null))
			.openMeteoResponse(new OpenMeteoResponse(37.5665, 126.9780, "Asia/Seoul", null, null))
			.build();
		TimeSlot currentSlot = TimeSlot.SLOT_03_06;
		LocalDate today = LocalDate.of(2024, 1, 1);

		Map<Integer, WeatherType> weathersByHour = Map.of(
			3, WeatherType.SUNNY,
			4, WeatherType.SUNNY,
			5, WeatherType.SUNNY
		);
		Map<Integer, FineDustType> dustByHour = Map.of(
			3, FineDustType.GOOD,
			4, FineDustType.GOOD,
			5, FineDustType.GOOD
		);
		Map<Integer, UvType> uvByHour = Map.of(
			3, UvType.UNKNOWN,
			4, UvType.UNKNOWN,
			5, UvType.UNKNOWN
		);

		when(kmaWeatherExtractor.extractWeatherForHours(any(KmaWeatherResponse.class), any(), eq(today)))
			.thenReturn(weathersByHour);
		when(fineDustExtractor.extractDustForHours(any(OpenMeteoResponse.class), any(), eq(today)))
			.thenReturn(dustByHour);
		when(uvIndexExtractor.extractUvForHours(any(OpenMeteoResponse.class), any(), eq(today)))
			.thenReturn(uvByHour);

		// when
		Map<String, WeatherCacheData> result = weatherDecisionService.getTodayWeatherCacheData(
			weatherApiResult, currentSlot, today);

		// then
		assertThat(result)
			.isNotNull()
			.hasSize(3);

		assertThat(result.get("03"))
			.isNotNull()
			.extracting(WeatherCacheData::weather, WeatherCacheData::fineDust, WeatherCacheData::uv)
			.containsExactly(WeatherType.SUNNY, FineDustType.GOOD, UvType.UNKNOWN);

		assertThat(result.get("05"))
			.isNotNull()
			.extracting(WeatherCacheData::weather, WeatherCacheData::fineDust, WeatherCacheData::uv)
			.containsExactly(WeatherType.SUNNY, FineDustType.GOOD, UvType.UNKNOWN);
	}

}
