package com.und.server.weather.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UvType 테스트")
class UvTypeTest {

	@Test
	@DisplayName("UV 지수로 UvType을 가져올 수 있다")
	void Given_UvIndex_When_FromUvIndex_Then_ReturnsUvType() {
		// given
		double uvIndexValue = 3.0; // 낮음 범위

		// when
		UvType result = UvType.fromUvIndex(uvIndexValue);

		// then
		assertThat(result).isEqualTo(UvType.LOW);
	}

	@Test
	@DisplayName("UV 지수 경계값을 올바르게 처리한다")
	void Given_UvIndexBoundaryValues_When_FromUvIndex_Then_ReturnsCorrectUvType() {
		// given & when & then
		assertThat(UvType.fromUvIndex(0.0)).isEqualTo(UvType.VERY_LOW);
		assertThat(UvType.fromUvIndex(2.0)).isEqualTo(UvType.VERY_LOW);
		assertThat(UvType.fromUvIndex(3.0)).isEqualTo(UvType.LOW);
		assertThat(UvType.fromUvIndex(4.0)).isEqualTo(UvType.LOW);
		assertThat(UvType.fromUvIndex(5.0)).isEqualTo(UvType.NORMAL);
		assertThat(UvType.fromUvIndex(6.0)).isEqualTo(UvType.NORMAL);
		assertThat(UvType.fromUvIndex(7.0)).isEqualTo(UvType.HIGH);
		assertThat(UvType.fromUvIndex(9.0)).isEqualTo(UvType.HIGH);
		assertThat(UvType.fromUvIndex(10.0)).isEqualTo(UvType.VERY_HIGH);
		assertThat(UvType.fromUvIndex(15.0)).isEqualTo(UvType.VERY_HIGH);
	}

	@Test
	@DisplayName("음수 UV 지수에 대해 DEFAULT를 반환한다")
	void Given_NegativeUvIndex_When_FromUvIndex_Then_ReturnsDefault() {
		// given
		double negativeUvIndex = -5.0;

		// when
		UvType result = UvType.fromUvIndex(negativeUvIndex);

		// then
		assertThat(result).isEqualTo(UvType.DEFAULT);
	}

	@Test
	@DisplayName("가장 심각한 자외선 타입을 가져올 수 있다")
	void Given_UvTypesList_When_GetWorst_Then_ReturnsWorstUvType() {
		// given
		List<UvType> uvTypes = List.of(
			UvType.VERY_LOW,  // severity: 1
			UvType.LOW,       // severity: 2
			UvType.NORMAL,    // severity: 3
			UvType.HIGH,      // severity: 4
			UvType.VERY_HIGH  // severity: 5
		);

		// when
		UvType worst = UvType.getWorst(uvTypes);

		// then
		assertThat(worst).isEqualTo(UvType.VERY_HIGH);
	}

	@Test
	@DisplayName("빈 리스트에서 DEFAULT를 반환한다")
	void Given_EmptyUvTypesList_When_GetWorst_Then_ReturnsDefault() {
		// given
		List<UvType> uvTypes = List.of();

		// when
		UvType worst = UvType.getWorst(uvTypes);

		// then
		assertThat(worst).isEqualTo(UvType.DEFAULT);
	}

	@Test
	@DisplayName("UvType의 심각도가 올바르다")
	void Given_UvType_When_GetSeverity_Then_ReturnsCorrectSeverity() {
		// given & when & then
		assertThat(UvType.UNKNOWN.getSeverity()).isEqualTo(0);
		assertThat(UvType.VERY_LOW.getSeverity()).isEqualTo(1);
		assertThat(UvType.LOW.getSeverity()).isEqualTo(2);
		assertThat(UvType.NORMAL.getSeverity()).isEqualTo(3);
		assertThat(UvType.HIGH.getSeverity()).isEqualTo(4);
		assertThat(UvType.VERY_HIGH.getSeverity()).isEqualTo(5);
	}

	@Test
	@DisplayName("UvType의 설명이 올바르다")
	void Given_UvType_When_GetDescription_Then_ReturnsCorrectDescription() {
		// given & when & then
		assertThat(UvType.UNKNOWN.getDescription()).isEqualTo("없음");
		assertThat(UvType.VERY_LOW.getDescription()).isEqualTo("매우낮음");
		assertThat(UvType.LOW.getDescription()).isEqualTo("낮음");
		assertThat(UvType.NORMAL.getDescription()).isEqualTo("보통");
		assertThat(UvType.HIGH.getDescription()).isEqualTo("높음");
		assertThat(UvType.VERY_HIGH.getDescription()).isEqualTo("매우높음");
	}

	@Test
	@DisplayName("UV 지수 범위가 올바르다")
	void Given_UvType_When_GetUvIndexRange_Then_ReturnsCorrectRange() {
		// given & when & then
		assertThat(UvType.VERY_LOW.getMinUvIndex()).isEqualTo(0);
		assertThat(UvType.VERY_LOW.getMaxUvIndex()).isEqualTo(2);
		assertThat(UvType.LOW.getMinUvIndex()).isEqualTo(3);
		assertThat(UvType.LOW.getMaxUvIndex()).isEqualTo(4);
		assertThat(UvType.NORMAL.getMinUvIndex()).isEqualTo(5);
		assertThat(UvType.NORMAL.getMaxUvIndex()).isEqualTo(6);
		assertThat(UvType.HIGH.getMinUvIndex()).isEqualTo(7);
		assertThat(UvType.HIGH.getMaxUvIndex()).isEqualTo(9);
		assertThat(UvType.VERY_HIGH.getMinUvIndex()).isEqualTo(10);
		assertThat(UvType.VERY_HIGH.getMaxUvIndex()).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	@DisplayName("OpenMeteo 변수명이 올바르다")
	void Given_UvType_When_GetOpenMeteoVariables_Then_ReturnsCorrectVariables() {
		// given & when & then
		assertThat(UvType.OPEN_METEO_VARIABLES).isEqualTo("uv_index");
	}

	@Test
	@DisplayName("UNKNOWN 타입의 범위가 올바르다")
	void Given_UnknownUvType_When_GetRange_Then_ReturnsCorrectRange() {
		// given & when & then
		assertThat(UvType.UNKNOWN.getMinUvIndex()).isEqualTo(-1);
		assertThat(UvType.UNKNOWN.getMaxUvIndex()).isEqualTo(-1);
	}

	@Test
	@DisplayName("반올림된 값으로 올바른 타입을 반환한다")
	void Given_RoundedValues_When_FromUvIndex_Then_ReturnsCorrectUvType() {
		// given & when & then
		assertThat(UvType.fromUvIndex(2.4)).isEqualTo(UvType.VERY_LOW);
		assertThat(UvType.fromUvIndex(2.5)).isEqualTo(UvType.LOW);
		assertThat(UvType.fromUvIndex(4.4)).isEqualTo(UvType.LOW);
		assertThat(UvType.fromUvIndex(4.5)).isEqualTo(UvType.NORMAL);
		assertThat(UvType.fromUvIndex(6.4)).isEqualTo(UvType.NORMAL);
		assertThat(UvType.fromUvIndex(6.5)).isEqualTo(UvType.HIGH);
		assertThat(UvType.fromUvIndex(9.4)).isEqualTo(UvType.HIGH);
		assertThat(UvType.fromUvIndex(9.5)).isEqualTo(UvType.VERY_HIGH);
	}

	@Test
	@DisplayName("극한 UV 지수값을 처리할 수 있다")
	void Given_ExtremeUvIndexValues_When_FromUvIndex_Then_ReturnsCorrectUvType() {
		// given & when & then
		assertThat(UvType.fromUvIndex(0.0)).isEqualTo(UvType.VERY_LOW);
		assertThat(UvType.fromUvIndex(100.0)).isEqualTo(UvType.VERY_HIGH);
		assertThat(UvType.fromUvIndex(Double.MAX_VALUE)).isEqualTo(UvType.UNKNOWN);
	}

	@Test
	@DisplayName("소수점 UV 지수값을 올바르게 처리한다")
	void Given_DecimalUvIndexValues_When_FromUvIndex_Then_ReturnsCorrectUvType() {
		// given & when & then
		assertThat(UvType.fromUvIndex(1.5)).isEqualTo(UvType.VERY_LOW);
		assertThat(UvType.fromUvIndex(3.7)).isEqualTo(UvType.LOW);
		assertThat(UvType.fromUvIndex(5.2)).isEqualTo(UvType.NORMAL);
		assertThat(UvType.fromUvIndex(7.8)).isEqualTo(UvType.HIGH);
		assertThat(UvType.fromUvIndex(12.3)).isEqualTo(UvType.VERY_HIGH);
	}

}
