package com.und.server.scenario.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.scenario.entity.Scenario;
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

	@Test
	void Given_ScenariosAndTargetId_When_Reorder_Then_ReturnReorderedScenarios() {
		// given
		Scenario scenario1 = Scenario.builder()
			.id(1L)
			.scenarioOrder(1000)
			.missions(new java.util.ArrayList<>())
			.build();
		Scenario scenario2 = Scenario.builder()
			.id(2L)
			.scenarioOrder(2000)
			.missions(new java.util.ArrayList<>())
			.build();
		Scenario scenario3 = Scenario.builder()
			.id(3L)
			.scenarioOrder(3000)
			.missions(new java.util.ArrayList<>())
			.build();
		List<Scenario> scenarios = new java.util.ArrayList<>(List.of(scenario1, scenario2, scenario3));

		Long targetScenarioId = 2L;
		int errorOrder = 1500;

		// when
		List<Scenario> result = orderCalculator.reorder(scenarios, targetScenarioId, errorOrder);

		// then
		assertThat(result).hasSize(3);
		// 순서가 재정렬되었는지 확인
		assertThat(result.get(0).getScenarioOrder()).isEqualTo(OrderCalculator.START_ORDER);
		assertThat(result.get(1).getScenarioOrder()).isEqualTo(
			OrderCalculator.START_ORDER + OrderCalculator.DEFAULT_ORDER);
		assertThat(result.get(2).getScenarioOrder()).isEqualTo(
			OrderCalculator.START_ORDER + 2 * OrderCalculator.DEFAULT_ORDER);
	}

	@Test
	void Given_EmptyScenarioList_When_GetMaxOrderAfterReorder_Then_ReturnStartOrder() {
		// given
		List<Scenario> emptyScenarios = List.of();

		// when
		Integer result = orderCalculator.getMaxOrderAfterReorder(emptyScenarios);

		// then
		assertThat(result).isEqualTo(OrderCalculator.START_ORDER);
	}

	@Test
	void Given_ScenarioList_When_GetMaxOrderAfterReorder_Then_ReturnMaxOrderPlusDefault() {
		// given
		Scenario scenario1 = Scenario.builder()
			.id(1L)
			.scenarioOrder(1000)
			.missions(new java.util.ArrayList<>())
			.build();
		Scenario scenario2 = Scenario.builder()
			.id(2L)
			.scenarioOrder(3000)
			.missions(new java.util.ArrayList<>())
			.build();
		List<Scenario> scenarios = new java.util.ArrayList<>(List.of(scenario1, scenario2));

		// when
		Integer result = orderCalculator.getMaxOrderAfterReorder(scenarios);

		// then
		// 리오더링 후 마지막 시나리오의 order + DEFAULT_ORDER
		assertThat(result).isEqualTo(
			OrderCalculator.START_ORDER + OrderCalculator.DEFAULT_ORDER + OrderCalculator.DEFAULT_ORDER);
	}

}
