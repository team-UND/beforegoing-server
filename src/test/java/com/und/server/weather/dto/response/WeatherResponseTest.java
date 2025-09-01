package com.und.server.weather.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.cache.WeatherCacheData;

@DisplayName("WeatherResponse 테스트")
class WeatherResponseTest {

	@Test
	@DisplayName("WeatherType, FineDustType, UvType으로 WeatherResponse를 생성한다")
	void Given_WeatherTypes_When_From_Then_ReturnsWeatherResponse() {
		// given
		WeatherType weather = WeatherType.SUNNY;
		FineDustType fineDust = FineDustType.GOOD;
		UvType uv = UvType.LOW;

		// when
		WeatherResponse result = WeatherResponse.from(weather, fineDust, uv);

		// then
		assertThat(result.weather()).isEqualTo(weather);
		assertThat(result.fineDust()).isEqualTo(fineDust);
		assertThat(result.uv()).isEqualTo(uv);
	}


	@Test
	@DisplayName("비 오는 날씨로 WeatherResponse를 생성한다")
	void Given_RainyWeather_When_From_Then_ReturnsRainyWeatherResponse() {
		// given
		WeatherType weather = WeatherType.RAIN;
		FineDustType fineDust = FineDustType.NORMAL;
		UvType uv = UvType.NORMAL;

		// when
		WeatherResponse result = WeatherResponse.from(weather, fineDust, uv);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.RAIN);
		assertThat(result.fineDust()).isEqualTo(FineDustType.NORMAL);
		assertThat(result.uv()).isEqualTo(UvType.NORMAL);
	}


	@Test
	@DisplayName("눈 오는 날씨로 WeatherResponse를 생성한다")
	void Given_SnowyWeather_When_From_Then_ReturnsSnowyWeatherResponse() {
		// given
		WeatherType weather = WeatherType.SNOW;
		FineDustType fineDust = FineDustType.GOOD;
		UvType uv = UvType.VERY_LOW;

		// when
		WeatherResponse result = WeatherResponse.from(weather, fineDust, uv);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SNOW);
		assertThat(result.fineDust()).isEqualTo(FineDustType.GOOD);
		assertThat(result.uv()).isEqualTo(UvType.VERY_LOW);
	}


	@Test
	@DisplayName("흐린 날씨로 WeatherResponse를 생성한다")
	void Given_CloudyWeather_When_From_Then_ReturnsCloudyWeatherResponse() {
		// given
		WeatherType weather = WeatherType.CLOUDY;
		FineDustType fineDust = FineDustType.BAD;
		UvType uv = UvType.HIGH;

		// when
		WeatherResponse result = WeatherResponse.from(weather, fineDust, uv);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.CLOUDY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.BAD);
		assertThat(result.uv()).isEqualTo(UvType.HIGH);
	}


	@Test
	@DisplayName("미세먼지가 매우 나쁜 날씨로 WeatherResponse를 생성한다")
	void Given_VeryBadFineDust_When_From_Then_ReturnsVeryBadFineDustResponse() {
		// given
		WeatherType weather = WeatherType.SUNNY;
		FineDustType fineDust = FineDustType.VERY_BAD;
		UvType uv = UvType.VERY_HIGH;

		// when
		WeatherResponse result = WeatherResponse.from(weather, fineDust, uv);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SUNNY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.VERY_BAD);
		assertThat(result.uv()).isEqualTo(UvType.VERY_HIGH);
	}


	@Test
	@DisplayName("UV 지수가 높은 날씨로 WeatherResponse를 생성한다")
	void Given_HighUvIndex_When_From_Then_ReturnsHighUvResponse() {
		// given
		WeatherType weather = WeatherType.SUNNY;
		FineDustType fineDust = FineDustType.GOOD;
		UvType uv = UvType.HIGH;

		// when
		WeatherResponse result = WeatherResponse.from(weather, fineDust, uv);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SUNNY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.GOOD);
		assertThat(result.uv()).isEqualTo(UvType.HIGH);
	}

	@Test
	@DisplayName("WeatherCacheData로 WeatherResponse를 생성한다")
	void Given_WeatherCacheData_When_From_Then_ReturnsWeatherResponse() {
		// given
		WeatherCacheData cacheData = WeatherCacheData.from(
			WeatherType.SUNNY, FineDustType.GOOD, UvType.LOW
		);

		// when
		WeatherResponse result = WeatherResponse.from(cacheData);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SUNNY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.GOOD);
		assertThat(result.uv()).isEqualTo(UvType.LOW);
	}


	@Test
	@DisplayName("비 오는 날씨의 WeatherCacheData로 WeatherResponse를 생성한다")
	void Given_RainyWeatherCacheData_When_From_Then_ReturnsRainyWeatherResponse() {
		// given
		WeatherCacheData cacheData = WeatherCacheData.from(
			WeatherType.RAIN, FineDustType.NORMAL, UvType.NORMAL
		);

		// when
		WeatherResponse result = WeatherResponse.from(cacheData);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.RAIN);
		assertThat(result.fineDust()).isEqualTo(FineDustType.NORMAL);
		assertThat(result.uv()).isEqualTo(UvType.NORMAL);
	}


	@Test
	@DisplayName("눈 오는 날씨의 WeatherCacheData로 WeatherResponse를 생성한다")
	void Given_SnowyWeatherCacheData_When_From_Then_ReturnsSnowyWeatherResponse() {
		// given
		WeatherCacheData cacheData = WeatherCacheData.from(
			WeatherType.SNOW, FineDustType.GOOD, UvType.VERY_LOW
		);

		// when
		WeatherResponse result = WeatherResponse.from(cacheData);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SNOW);
		assertThat(result.fineDust()).isEqualTo(FineDustType.GOOD);
		assertThat(result.uv()).isEqualTo(UvType.VERY_LOW);
	}


	@Test
	@DisplayName("흐린 날씨의 WeatherCacheData로 WeatherResponse를 생성한다")
	void Given_CloudyWeatherCacheData_When_From_Then_ReturnsCloudyWeatherResponse() {
		// given
		WeatherCacheData cacheData = WeatherCacheData.from(
			WeatherType.CLOUDY, FineDustType.BAD, UvType.HIGH
		);

		// when
		WeatherResponse result = WeatherResponse.from(cacheData);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.CLOUDY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.BAD);
		assertThat(result.uv()).isEqualTo(UvType.HIGH);
	}


	@Test
	@DisplayName("미세먼지가 매우 나쁜 WeatherCacheData로 WeatherResponse를 생성한다")
	void Given_VeryBadFineDustCacheData_When_From_Then_ReturnsVeryBadFineDustResponse() {
		// given
		WeatherCacheData cacheData = WeatherCacheData.from(
			WeatherType.SUNNY, FineDustType.VERY_BAD, UvType.VERY_HIGH
		);

		// when
		WeatherResponse result = WeatherResponse.from(cacheData);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SUNNY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.VERY_BAD);
		assertThat(result.uv()).isEqualTo(UvType.VERY_HIGH);
	}


	@Test
	@DisplayName("UV 지수가 높은 WeatherCacheData로 WeatherResponse를 생성한다")
	void Given_HighUvCacheData_When_From_Then_ReturnsHighUvResponse() {
		// given
		WeatherCacheData cacheData = WeatherCacheData.from(
			WeatherType.SUNNY, FineDustType.GOOD, UvType.HIGH
		);

		// when
		WeatherResponse result = WeatherResponse.from(cacheData);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SUNNY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.GOOD);
		assertThat(result.uv()).isEqualTo(UvType.HIGH);
	}


	@Test
	@DisplayName("모든 날씨 조건이 최악인 WeatherCacheData로 WeatherResponse를 생성한다")
	void Given_WorstWeatherCacheData_When_From_Then_ReturnsWorstWeatherResponse() {
		// given
		WeatherCacheData cacheData = WeatherCacheData.from(
			WeatherType.SNOW, FineDustType.VERY_BAD, UvType.VERY_HIGH
		);

		// when
		WeatherResponse result = WeatherResponse.from(cacheData);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SNOW);
		assertThat(result.fineDust()).isEqualTo(FineDustType.VERY_BAD);
		assertThat(result.uv()).isEqualTo(UvType.VERY_HIGH);
	}


	@Test
	@DisplayName("모든 날씨 조건이 좋은 WeatherCacheData로 WeatherResponse를 생성한다")
	void Given_BestWeatherCacheData_When_From_Then_ReturnsBestWeatherResponse() {
		// given
		WeatherCacheData cacheData = WeatherCacheData.from(
			WeatherType.SUNNY, FineDustType.GOOD, UvType.LOW
		);

		// when
		WeatherResponse result = WeatherResponse.from(cacheData);

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SUNNY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.GOOD);
		assertThat(result.uv()).isEqualTo(UvType.LOW);
	}

	@Test
	@DisplayName("WeatherResponse의 빌더 패턴으로 생성한다")
	void Given_Builder_When_Build_Then_ReturnsWeatherResponse() {
		// when
		WeatherResponse result = WeatherResponse.builder()
			.weather(WeatherType.SUNNY)
			.fineDust(FineDustType.GOOD)
			.uv(UvType.LOW)
			.build();

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.SUNNY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.GOOD);
		assertThat(result.uv()).isEqualTo(UvType.LOW);
	}

	@Test
	@DisplayName("WeatherResponse의 빌더 패턴으로 비 오는 날씨를 생성한다")
	void Given_Builder_When_BuildRainyWeather_Then_ReturnsRainyWeatherResponse() {
		// when
		WeatherResponse result = WeatherResponse.builder()
			.weather(WeatherType.RAIN)
			.fineDust(FineDustType.NORMAL)
			.uv(UvType.NORMAL)
			.build();

		// then
		assertThat(result.weather()).isEqualTo(WeatherType.RAIN);
		assertThat(result.fineDust()).isEqualTo(FineDustType.NORMAL);
		assertThat(result.uv()).isEqualTo(UvType.NORMAL);
	}

}
