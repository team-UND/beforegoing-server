package com.und.server.weather.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;

@ExtendWith(MockitoExtension.class)
@DisplayName("FutureWeatherDecisionSelector 테스트")
class FutureWeatherDecisionSelectorTest {

	@InjectMocks
	private FutureWeatherDecisionSelector futureWeatherDecisionSelector;


	@Test
	@DisplayName("최악의 날씨를 정상적으로 선택한다")
	void Given_WeatherTypes_When_CalculateWorstWeather_Then_ReturnsWorstWeather() {
		// given
		List<WeatherType> weatherTypes = Arrays.asList(
			WeatherType.SUNNY,
			WeatherType.CLOUDY,
			WeatherType.RAIN,
			WeatherType.SHOWER
		);

		// when
		WeatherType result = futureWeatherDecisionSelector.calculateWorstWeather(weatherTypes);

		// then
		assertThat(result).isEqualTo(WeatherType.SHOWER);
	}


	@Test
	@DisplayName("날씨 리스트가 null일 때 기본값을 반환한다")
	void Given_NullWeatherTypes_When_CalculateWorstWeather_Then_ReturnsDefault() {
		// given
		List<WeatherType> weatherTypes = null;

		// when
		WeatherType result = futureWeatherDecisionSelector.calculateWorstWeather(weatherTypes);

		// then
		assertThat(result).isEqualTo(WeatherType.DEFAULT);
	}


	@Test
	@DisplayName("날씨 리스트가 비어있을 때 기본값을 반환한다")
	void Given_EmptyWeatherTypes_When_CalculateWorstWeather_Then_ReturnsDefault() {
		// given
		List<WeatherType> weatherTypes = Collections.emptyList();

		// when
		WeatherType result = futureWeatherDecisionSelector.calculateWorstWeather(weatherTypes);

		// then
		assertThat(result).isEqualTo(WeatherType.DEFAULT);
	}


	@Test
	@DisplayName("최악의 미세먼지를 정상적으로 선택한다")
	void Given_FineDustTypes_When_CalculateWorstFineDust_Then_ReturnsWorstFineDust() {
		// given
		List<FineDustType> fineDustTypes = Arrays.asList(
			FineDustType.GOOD,
			FineDustType.NORMAL,
			FineDustType.BAD,
			FineDustType.VERY_BAD
		);

		// when
		FineDustType result = futureWeatherDecisionSelector.calculateWorstFineDust(fineDustTypes);

		// then
		assertThat(result).isEqualTo(FineDustType.VERY_BAD);
	}


	@Test
	@DisplayName("미세먼지 리스트가 null일 때 기본값을 반환한다")
	void Given_NullFineDustTypes_When_CalculateWorstFineDust_Then_ReturnsDefault() {
		// given
		List<FineDustType> fineDustTypes = null;

		// when
		FineDustType result = futureWeatherDecisionSelector.calculateWorstFineDust(fineDustTypes);

		// then
		assertThat(result).isEqualTo(FineDustType.DEFAULT);
	}


	@Test
	@DisplayName("미세먼지 리스트가 비어있을 때 기본값을 반환한다")
	void Given_EmptyFineDustTypes_When_CalculateWorstFineDust_Then_ReturnsDefault() {
		// given
		List<FineDustType> fineDustTypes = Collections.emptyList();

		// when
		FineDustType result = futureWeatherDecisionSelector.calculateWorstFineDust(fineDustTypes);

		// then
		assertThat(result).isEqualTo(FineDustType.DEFAULT);
	}


	@Test
	@DisplayName("최악의 자외선을 정상적으로 선택한다")
	void Given_UvTypes_When_CalculateWorstUv_Then_ReturnsWorstUv() {
		// given
		List<UvType> uvTypes = Arrays.asList(
			UvType.LOW,
			UvType.NORMAL,
			UvType.HIGH,
			UvType.VERY_HIGH
		);

		// when
		UvType result = futureWeatherDecisionSelector.calculateWorstUv(uvTypes);

		// then
		assertThat(result).isEqualTo(UvType.VERY_HIGH);
	}


	@Test
	@DisplayName("자외선 리스트가 null일 때 기본값을 반환한다")
	void Given_NullUvTypes_When_CalculateWorstUv_Then_ReturnsDefault() {
		// given
		List<UvType> uvTypes = null;

		// when
		UvType result = futureWeatherDecisionSelector.calculateWorstUv(uvTypes);

		// then
		assertThat(result).isEqualTo(UvType.DEFAULT);
	}


	@Test
	@DisplayName("자외선 리스트가 비어있을 때 기본값을 반환한다")
	void Given_EmptyUvTypes_When_CalculateWorstUv_Then_ReturnsDefault() {
		// given
		List<UvType> uvTypes = Collections.emptyList();

		// when
		UvType result = futureWeatherDecisionSelector.calculateWorstUv(uvTypes);

		// then
		assertThat(result).isEqualTo(UvType.DEFAULT);
	}

}
