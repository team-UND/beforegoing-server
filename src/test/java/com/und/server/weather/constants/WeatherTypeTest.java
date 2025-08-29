package com.und.server.weather.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WeatherType 테스트")
class WeatherTypeTest {

	@Test
	@DisplayName("PTY 값으로 WeatherType을 가져올 수 있다")
	void Given_PtyValue_When_FromPtyValue_Then_ReturnsWeatherType() {
		// given
		int ptyValue = 1; // 비

		// when
		WeatherType result = WeatherType.fromPtyValue(ptyValue);

		// then
		assertThat(result).isEqualTo(WeatherType.RAIN);
	}

	@Test
	@DisplayName("SKY 값으로 WeatherType을 가져올 수 있다")
	void Given_SkyValue_When_FromSkyValue_Then_ReturnsWeatherType() {
		// given
		int skyValue = 1; // 맑음

		// when
		WeatherType result = WeatherType.fromSkyValue(skyValue);

		// then
		assertThat(result).isEqualTo(WeatherType.SUNNY);
	}

	@Test
	@DisplayName("존재하지 않는 PTY 값에 대해 DEFAULT를 반환한다")
	void Given_InvalidPtyValue_When_FromPtyValue_Then_ReturnsDefault() {
		// given
		int invalidPtyValue = 999;

		// when
		WeatherType result = WeatherType.fromPtyValue(invalidPtyValue);

		// then
		assertThat(result).isEqualTo(WeatherType.DEFAULT);
	}

	@Test
	@DisplayName("존재하지 않는 SKY 값에 대해 DEFAULT를 반환한다")
	void Given_InvalidSkyValue_When_FromSkyValue_Then_ReturnsDefault() {
		// given
		int invalidSkyValue = 999;

		// when
		WeatherType result = WeatherType.fromSkyValue(invalidSkyValue);

		// then
		assertThat(result).isEqualTo(WeatherType.DEFAULT);
	}

	@Test
	@DisplayName("OpenMeteo 코드로 WeatherType을 가져올 수 있다")
	void Given_OpenMeteoCode_When_FromOpenMeteoCode_Then_ReturnsWeatherType() {
		// given
		int sunnyCode = 0;
		int cloudyCode = 1;
		int overcastCode = 45;
		int rainCode = 61;
		int snowCode = 71;
		int showerCode = 80;
		int sleetCode = 85;

		// when & then
		assertThat(WeatherType.fromOpenMeteoCode(sunnyCode)).isEqualTo(WeatherType.SUNNY);
		assertThat(WeatherType.fromOpenMeteoCode(cloudyCode)).isEqualTo(WeatherType.CLOUDY);
		assertThat(WeatherType.fromOpenMeteoCode(overcastCode)).isEqualTo(WeatherType.OVERCAST);
		assertThat(WeatherType.fromOpenMeteoCode(rainCode)).isEqualTo(WeatherType.RAIN);
		assertThat(WeatherType.fromOpenMeteoCode(snowCode)).isEqualTo(WeatherType.SNOW);
		assertThat(WeatherType.fromOpenMeteoCode(showerCode)).isEqualTo(WeatherType.SHOWER);
		assertThat(WeatherType.fromOpenMeteoCode(sleetCode)).isEqualTo(WeatherType.SLEET);
	}

	@Test
	@DisplayName("존재하지 않는 OpenMeteo 코드에 대해 DEFAULT를 반환한다")
	void Given_InvalidOpenMeteoCode_When_FromOpenMeteoCode_Then_ReturnsDefault() {
		// given
		int invalidCode = 999;

		// when
		WeatherType result = WeatherType.fromOpenMeteoCode(invalidCode);

		// then
		assertThat(result).isEqualTo(WeatherType.DEFAULT);
	}

	@Test
	@DisplayName("시간대별 기준 시간을 가져올 수 있다")
	void Given_TimeSlot_When_GetBaseTime_Then_ReturnsBaseTime() {
		// given & when & then
		assertThat(WeatherType.getBaseTime(TimeSlot.SLOT_00_03)).isEqualTo("2300");
		assertThat(WeatherType.getBaseTime(TimeSlot.SLOT_03_06)).isEqualTo("0200");
		assertThat(WeatherType.getBaseTime(TimeSlot.SLOT_06_09)).isEqualTo("0500");
		assertThat(WeatherType.getBaseTime(TimeSlot.SLOT_09_12)).isEqualTo("0800");
		assertThat(WeatherType.getBaseTime(TimeSlot.SLOT_12_15)).isEqualTo("1100");
		assertThat(WeatherType.getBaseTime(TimeSlot.SLOT_15_18)).isEqualTo("1400");
		assertThat(WeatherType.getBaseTime(TimeSlot.SLOT_18_21)).isEqualTo("1700");
		assertThat(WeatherType.getBaseTime(TimeSlot.SLOT_21_24)).isEqualTo("2000");
	}

	@Test
	@DisplayName("시간대별 기준 날짜를 가져올 수 있다")
	void Given_TimeSlotAndDate_When_GetBaseDate_Then_ReturnsBaseDate() {
		// given
		LocalDate date = LocalDate.of(2024, 1, 15);

		// when & then
		// 00-03 시간대는 전날 기준
		assertThat(WeatherType.getBaseDate(TimeSlot.SLOT_00_03, date))
			.isEqualTo(LocalDate.of(2024, 1, 14));

		// 다른 시간대는 당일 기준
		assertThat(WeatherType.getBaseDate(TimeSlot.SLOT_12_15, date))
			.isEqualTo(LocalDate.of(2024, 1, 15));
	}

	@Test
	@DisplayName("가장 심각한 날씨 타입을 가져올 수 있다")
	void Given_WeatherTypesList_When_GetWorst_Then_ReturnsWorstWeatherType() {
		// given
		List<WeatherType> weatherTypes = List.of(
			WeatherType.SUNNY,    // severity: 1
			WeatherType.CLOUDY,   // severity: 2
			WeatherType.RAIN,     // severity: 5
			WeatherType.SHOWER    // severity: 6
		);

		// when
		WeatherType worst = WeatherType.getWorst(weatherTypes);

		// then
		assertThat(worst).isEqualTo(WeatherType.SHOWER);
	}

	@Test
	@DisplayName("null이 포함된 리스트에서도 가장 심각한 날씨 타입을 가져올 수 있다")
	void Given_WeatherTypesListWithNull_When_GetWorst_Then_ReturnsWorstWeatherType() {
		// given
		List<WeatherType> weatherTypes = Arrays.asList(
			WeatherType.SUNNY,    // severity: 1
			null,
			WeatherType.RAIN,     // severity: 5
			null
		);

		// when
		WeatherType worst = WeatherType.getWorst(weatherTypes);

		// then
		assertThat(worst).isEqualTo(WeatherType.RAIN);
	}

	@Test
	@DisplayName("빈 리스트에서 DEFAULT를 반환한다")
	void Given_EmptyWeatherTypesList_When_GetWorst_Then_ReturnsDefault() {
		// given
		List<WeatherType> weatherTypes = List.of();

		// when
		WeatherType worst = WeatherType.getWorst(weatherTypes);

		// then
		assertThat(worst).isEqualTo(WeatherType.DEFAULT);
	}

	@Test
	@DisplayName("모든 null 리스트에서 DEFAULT를 반환한다")
	void Given_AllNullWeatherTypesList_When_GetWorst_Then_ReturnsDefault() {
		// given
		List<WeatherType> weatherTypes = Arrays.asList(null, null, null);

		// when
		WeatherType worst = WeatherType.getWorst(weatherTypes);

		// then
		assertThat(worst).isEqualTo(WeatherType.DEFAULT);
	}

	@Test
	@DisplayName("WeatherType의 심각도가 올바르다")
	void Given_WeatherType_When_GetSeverity_Then_ReturnsCorrectSeverity() {
		// given & when & then
		assertThat(WeatherType.UNKNOWN.getSeverity()).isEqualTo(0);
		assertThat(WeatherType.SUNNY.getSeverity()).isEqualTo(1);
		assertThat(WeatherType.CLOUDY.getSeverity()).isEqualTo(2);
		assertThat(WeatherType.OVERCAST.getSeverity()).isEqualTo(2);
		assertThat(WeatherType.SLEET.getSeverity()).isEqualTo(3);
		assertThat(WeatherType.SNOW.getSeverity()).isEqualTo(4);
		assertThat(WeatherType.RAIN.getSeverity()).isEqualTo(5);
		assertThat(WeatherType.SHOWER.getSeverity()).isEqualTo(6);
	}

	@Test
	@DisplayName("WeatherType의 설명이 올바르다")
	void Given_WeatherType_When_GetDescription_Then_ReturnsCorrectDescription() {
		// given & when & then
		assertThat(WeatherType.UNKNOWN.getDescription()).isEqualTo("없음");
		assertThat(WeatherType.SUNNY.getDescription()).isEqualTo("맑음");
		assertThat(WeatherType.CLOUDY.getDescription()).isEqualTo("구름많음");
		assertThat(WeatherType.OVERCAST.getDescription()).isEqualTo("흐림");
		assertThat(WeatherType.RAIN.getDescription()).isEqualTo("비");
		assertThat(WeatherType.SLEET.getDescription()).isEqualTo("진눈깨비");
		assertThat(WeatherType.SNOW.getDescription()).isEqualTo("눈");
		assertThat(WeatherType.SHOWER.getDescription()).isEqualTo("소나기");
	}

	@Test
	@DisplayName("PTY 값이 올바르다")
	void Given_WeatherType_When_GetPtyValue_Then_ReturnsCorrectPtyValue() {
		// given & when & then
		assertThat(WeatherType.RAIN.getPtyValue()).isEqualTo(1);
		assertThat(WeatherType.SLEET.getPtyValue()).isEqualTo(2);
		assertThat(WeatherType.SNOW.getPtyValue()).isEqualTo(3);
		assertThat(WeatherType.SHOWER.getPtyValue()).isEqualTo(4);

		// PTY 값이 없는 타입들
		assertThat(WeatherType.UNKNOWN.getPtyValue()).isNull();
		assertThat(WeatherType.SUNNY.getPtyValue()).isNull();
		assertThat(WeatherType.CLOUDY.getPtyValue()).isNull();
		assertThat(WeatherType.OVERCAST.getPtyValue()).isNull();
	}

	@Test
	@DisplayName("SKY 값이 올바르다")
	void Given_WeatherType_When_GetSkyValue_Then_ReturnsCorrectSkyValue() {
		// given & when & then
		assertThat(WeatherType.SUNNY.getSkyValue()).isEqualTo(1);
		assertThat(WeatherType.CLOUDY.getSkyValue()).isEqualTo(3);
		assertThat(WeatherType.OVERCAST.getSkyValue()).isEqualTo(4);

		// SKY 값이 없는 타입들
		assertThat(WeatherType.UNKNOWN.getSkyValue()).isNull();
		assertThat(WeatherType.RAIN.getSkyValue()).isNull();
		assertThat(WeatherType.SLEET.getSkyValue()).isNull();
		assertThat(WeatherType.SNOW.getSkyValue()).isNull();
		assertThat(WeatherType.SHOWER.getSkyValue()).isNull();
	}

	@Test
	@DisplayName("OpenMeteo 변수명이 올바르다")
	void Given_WeatherType_When_GetOpenMeteoVariables_Then_ReturnsCorrectVariables() {
		// given & when & then
		assertThat(WeatherType.OPEN_METEO_VARIABLES).isEqualTo("weathercode");
	}

	@Test
	@DisplayName("KMA 날짜 포맷터가 올바르다")
	void Given_WeatherType_When_GetKmaDateFormatter_Then_ReturnsCorrectFormatter() {
		// given & when & then
		assertThat(WeatherType.KMA_DATE_FORMATTER).isNotNull();
		assertThat(WeatherType.KMA_DATE_FORMATTER.toString()).contains("Value(DayOfMonth,2)");
	}

}
