package com.und.server.weather.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FineDustType 테스트")
class FineDustTypeTest {

	@Test
	@DisplayName("PM10 농도로 FineDustType을 가져올 수 있다")
	void Given_Pm10Concentration_When_FromPm10Concentration_Then_ReturnsFineDustType() {
		// given
		double pm10Value = 25.0; // 좋음 범위

		// when
		FineDustType result = FineDustType.fromPm10Concentration(pm10Value);

		// then
		assertThat(result).isEqualTo(FineDustType.GOOD);
	}


	@Test
	@DisplayName("PM2.5 농도로 FineDustType을 가져올 수 있다")
	void Given_Pm25Concentration_When_FromPm25Concentration_Then_ReturnsFineDustType() {
		// given
		double pm25Value = 20.0; // 보통 범위

		// when
		FineDustType result = FineDustType.fromPm25Concentration(pm25Value);

		// then
		assertThat(result).isEqualTo(FineDustType.NORMAL);
	}


	@Test
	@DisplayName("PM10 농도 경계값을 올바르게 처리한다")
	void Given_Pm10BoundaryValues_When_FromPm10Concentration_Then_ReturnsCorrectFineDustType() {
		// given & when & then
		assertThat(FineDustType.fromPm10Concentration(0.0)).isEqualTo(FineDustType.GOOD);
		assertThat(FineDustType.fromPm10Concentration(30.0)).isEqualTo(FineDustType.GOOD);
		assertThat(FineDustType.fromPm10Concentration(31.0)).isEqualTo(FineDustType.NORMAL);
		assertThat(FineDustType.fromPm10Concentration(80.0)).isEqualTo(FineDustType.NORMAL);
		assertThat(FineDustType.fromPm10Concentration(81.0)).isEqualTo(FineDustType.BAD);
		assertThat(FineDustType.fromPm10Concentration(150.0)).isEqualTo(FineDustType.BAD);
		assertThat(FineDustType.fromPm10Concentration(151.0)).isEqualTo(FineDustType.VERY_BAD);
	}


	@Test
	@DisplayName("PM2.5 농도 경계값을 올바르게 처리한다")
	void Given_Pm25BoundaryValues_When_FromPm25Concentration_Then_ReturnsCorrectFineDustType() {
		// given & when & then
		assertThat(FineDustType.fromPm25Concentration(0.0)).isEqualTo(FineDustType.GOOD);
		assertThat(FineDustType.fromPm25Concentration(15.0)).isEqualTo(FineDustType.GOOD);
		assertThat(FineDustType.fromPm25Concentration(16.0)).isEqualTo(FineDustType.NORMAL);
		assertThat(FineDustType.fromPm25Concentration(35.0)).isEqualTo(FineDustType.NORMAL);
		assertThat(FineDustType.fromPm25Concentration(36.0)).isEqualTo(FineDustType.BAD);
		assertThat(FineDustType.fromPm25Concentration(75.0)).isEqualTo(FineDustType.BAD);
		assertThat(FineDustType.fromPm25Concentration(76.0)).isEqualTo(FineDustType.VERY_BAD);
	}


	@Test
	@DisplayName("음수 PM10 농도에 대해 DEFAULT를 반환한다")
	void Given_NegativePm10Concentration_When_FromPm10Concentration_Then_ReturnsDefault() {
		// given
		double negativePm10 = -10.0;

		// when
		FineDustType result = FineDustType.fromPm10Concentration(negativePm10);

		// then
		assertThat(result).isEqualTo(FineDustType.DEFAULT);
	}


	@Test
	@DisplayName("음수 PM2.5 농도에 대해 DEFAULT를 반환한다")
	void Given_NegativePm25Concentration_When_FromPm25Concentration_Then_ReturnsDefault() {
		// given
		double negativePm25 = -5.0;

		// when
		FineDustType result = FineDustType.fromPm25Concentration(negativePm25);

		// then
		assertThat(result).isEqualTo(FineDustType.DEFAULT);
	}


	@Test
	@DisplayName("가장 심각한 미세먼지 타입을 가져올 수 있다")
	void Given_FineDustTypesList_When_GetWorst_Then_ReturnsWorstFineDustType() {
		// given
		List<FineDustType> fineDustTypes = List.of(
			FineDustType.GOOD,      // severity: 1
			FineDustType.NORMAL,    // severity: 2
			FineDustType.BAD,       // severity: 3
			FineDustType.VERY_BAD   // severity: 4
		);

		// when
		FineDustType worst = FineDustType.getWorst(fineDustTypes);

		// then
		assertThat(worst).isEqualTo(FineDustType.VERY_BAD);
	}


	@Test
	@DisplayName("빈 리스트에서 DEFAULT를 반환한다")
	void Given_EmptyFineDustTypesList_When_GetWorst_Then_ReturnsDefault() {
		// given
		List<FineDustType> fineDustTypes = List.of();

		// when
		FineDustType worst = FineDustType.getWorst(fineDustTypes);

		// then
		assertThat(worst).isEqualTo(FineDustType.DEFAULT);
	}


	@Test
	@DisplayName("평균값을 계산할 수 있다")
	void Given_FineDustType_When_GetAverageValue_Then_ReturnsAverageValue() {
		// given & when & then
		assertThat(FineDustType.UNKNOWN.getAverageValue()).isEqualTo(0.0);
		assertThat(FineDustType.GOOD.getAverageValue()).isEqualTo(7.5); // (0 + 15) / 2
		assertThat(FineDustType.NORMAL.getAverageValue()).isEqualTo(25.5); // (16 + 35) / 2
		assertThat(FineDustType.BAD.getAverageValue()).isEqualTo(55.5); // (36 + 75) / 2
		assertThat(FineDustType.VERY_BAD.getAverageValue()).isEqualTo(126.0); // 76 + 50
	}


	@Test
	@DisplayName("평균값으로 FineDustType을 가져올 수 있다")
	void Given_AverageValue_When_FromAverageValue_Then_ReturnsFineDustType() {
		// given
		double averageValue = 10.0; // 좋음 범위

		// when
		FineDustType result = FineDustType.fromAverageValue(averageValue);

		// then
		assertThat(result).isEqualTo(FineDustType.GOOD);
	}


	@Test
	@DisplayName("FineDustType의 심각도가 올바르다")
	void Given_FineDustType_When_GetSeverity_Then_ReturnsCorrectSeverity() {
		// given & when & then
		assertThat(FineDustType.UNKNOWN.getSeverity()).isEqualTo(0);
		assertThat(FineDustType.GOOD.getSeverity()).isEqualTo(1);
		assertThat(FineDustType.NORMAL.getSeverity()).isEqualTo(2);
		assertThat(FineDustType.BAD.getSeverity()).isEqualTo(3);
		assertThat(FineDustType.VERY_BAD.getSeverity()).isEqualTo(4);
	}

	@Test
	@DisplayName("FineDustType의 설명이 올바르다")
	void Given_FineDustType_When_GetDescription_Then_ReturnsCorrectDescription() {
		// given & when & then
		assertThat(FineDustType.UNKNOWN.getDescription()).isEqualTo("없음");
		assertThat(FineDustType.GOOD.getDescription()).isEqualTo("좋음");
		assertThat(FineDustType.NORMAL.getDescription()).isEqualTo("보통");
		assertThat(FineDustType.BAD.getDescription()).isEqualTo("나쁨");
		assertThat(FineDustType.VERY_BAD.getDescription()).isEqualTo("매우나쁨");
	}

	@Test
	@DisplayName("PM10 범위가 올바르다")
	void Given_FineDustType_When_GetPm10Range_Then_ReturnsCorrectRange() {
		// given & when & then
		assertThat(FineDustType.GOOD.getMinPm10()).isEqualTo(0);
		assertThat(FineDustType.GOOD.getMaxPm10()).isEqualTo(30);
		assertThat(FineDustType.NORMAL.getMinPm10()).isEqualTo(31);
		assertThat(FineDustType.NORMAL.getMaxPm10()).isEqualTo(80);
		assertThat(FineDustType.BAD.getMinPm10()).isEqualTo(81);
		assertThat(FineDustType.BAD.getMaxPm10()).isEqualTo(150);
		assertThat(FineDustType.VERY_BAD.getMinPm10()).isEqualTo(151);
		assertThat(FineDustType.VERY_BAD.getMaxPm10()).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	@DisplayName("PM2.5 범위가 올바르다")
	void Given_FineDustType_When_GetPm25Range_Then_ReturnsCorrectRange() {
		// given & when & then
		assertThat(FineDustType.GOOD.getMinPm25()).isEqualTo(0);
		assertThat(FineDustType.GOOD.getMaxPm25()).isEqualTo(15);
		assertThat(FineDustType.NORMAL.getMinPm25()).isEqualTo(16);
		assertThat(FineDustType.NORMAL.getMaxPm25()).isEqualTo(35);
		assertThat(FineDustType.BAD.getMinPm25()).isEqualTo(36);
		assertThat(FineDustType.BAD.getMaxPm25()).isEqualTo(75);
		assertThat(FineDustType.VERY_BAD.getMinPm25()).isEqualTo(76);
		assertThat(FineDustType.VERY_BAD.getMaxPm25()).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	@DisplayName("OpenMeteo 변수명이 올바르다")
	void Given_FineDustType_When_GetOpenMeteoVariables_Then_ReturnsCorrectVariables() {
		// given & when & then
		assertThat(FineDustType.OPEN_METEO_VARIABLES).isEqualTo("pm2_5,pm10");
	}

	@Test
	@DisplayName("UNKNOWN 타입의 범위가 올바르다")
	void Given_UnknownFineDustType_When_GetRange_Then_ReturnsCorrectRange() {
		// given & when & then
		assertThat(FineDustType.UNKNOWN.getMinPm10()).isEqualTo(-1);
		assertThat(FineDustType.UNKNOWN.getMaxPm10()).isEqualTo(-1);
		assertThat(FineDustType.UNKNOWN.getMinPm25()).isEqualTo(-1);
		assertThat(FineDustType.UNKNOWN.getMaxPm25()).isEqualTo(-1);
	}

	@Test
	@DisplayName("반올림된 값으로 올바른 타입을 반환한다")
	void Given_RoundedValues_When_FromConcentration_Then_ReturnsCorrectFineDustType() {
		// given & when & then
		assertThat(FineDustType.fromPm10Concentration(30.4)).isEqualTo(FineDustType.GOOD);
		assertThat(FineDustType.fromPm10Concentration(30.5)).isEqualTo(FineDustType.NORMAL);
		assertThat(FineDustType.fromPm25Concentration(15.4)).isEqualTo(FineDustType.GOOD);
		assertThat(FineDustType.fromPm25Concentration(15.5)).isEqualTo(FineDustType.NORMAL);
		assertThat(FineDustType.fromPm25Concentration(15.6)).isEqualTo(FineDustType.NORMAL);
		assertThat(FineDustType.fromPm25Concentration(15.7)).isEqualTo(FineDustType.NORMAL);
	}

}
