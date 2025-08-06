package com.und.server.scenario.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.scenario.exception.ReorderRequiredException;

@ExtendWith(MockitoExtension.class)
class OrderCalculatorTest {

	@InjectMocks
	private OrderCalculator orderCalculator;


	@Test
	void Given_NullPrevAndNextOrder_When_GetOrder_Then_ReturnStartOrder() {
		int result = orderCalculator.getOrder(null, null);
		assertThat(result).isEqualTo(OrderCalculator.START_ORDER);
	}

	@Test
	void Given_NullPrevOrder_When_GetOrder_Then_ReturnStartOrderBeforeNext() {
		int result = orderCalculator.getOrder(null, 3000);
		assertThat(result).isEqualTo(2000);
	}

	@Test
	void Given_NullNextOrder_When_GetOrder_Then_ReturnLastOrderAfterPrev() {
		int result = orderCalculator.getOrder(3000, null);
		assertThat(result).isEqualTo(4000);
	}

	@Test
	void Given_ValidPrevAndNextOrder_When_GetOrder_Then_ReturnMiddleOrder() {
		int result = orderCalculator.getOrder(2000, 4000);
		assertThat(result).isEqualTo(3000);
	}

	@Test
	void Given_SmallGapPrevAndNextOrder_When_GetOrder_Then_ThrowReorderRequiredException() {
		assertThatThrownBy(() -> orderCalculator.getOrder(1000, 1050))
			.isInstanceOf(ReorderRequiredException.class);
	}

	@Test
	void Given_ResultOutOfRangeOrder_When_GetOrder_Then_ThrowReorderRequiredException() {
		assertThatThrownBy(() -> orderCalculator.getOrder(10_000_000, null))
			.isInstanceOf(ReorderRequiredException.class);
	}

}
