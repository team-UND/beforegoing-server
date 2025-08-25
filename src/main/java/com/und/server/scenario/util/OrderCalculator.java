package com.und.server.scenario.util;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.exception.ReorderRequiredException;

@Component
public class OrderCalculator {

	public static final int START_ORDER = 100000;
	public static final int DEFAULT_ORDER = 1000;
	private static final int MIN_ORDER = 0;
	private static final int MAX_ORDER = 10_000_000;
	private static final int MIN_GAP = 100;


	public int getOrder(final Integer prevOrder, final Integer nextOrder) {
		int resultOrder = 0;

		if (prevOrder == null && nextOrder == null) {
			return START_ORDER;
		}
		if (prevOrder == null) {
			resultOrder = calculateStartOrder(nextOrder);
		} else if (nextOrder == null) {
			resultOrder = calculateLastOrder(prevOrder);
		} else {
			resultOrder = calculateMiddleOrder(prevOrder, nextOrder);
			validateOrderGap(prevOrder, nextOrder, resultOrder);
		}
		validateOrderRange(resultOrder);

		return resultOrder;
	}


	public List<Scenario> reorder(
		final List<Scenario> scenarios,
		final Long targetScenarioId,
		final int errorOrder
	) {
		scenarios.sort(
			Comparator
				.comparingInt((Scenario s) ->
					s.getId().equals(targetScenarioId) ? errorOrder : s.getScenarioOrder())
				.thenComparingLong(Scenario::getId)
		);

		assignSequentialOrders(scenarios);
		scenarios.sort(Comparator.comparing(Scenario::getScenarioOrder));

		return scenarios;
	}


	public Integer getMaxOrderAfterReorder(final List<Scenario> scenarios) {
		if (scenarios.isEmpty()) {
			return START_ORDER;
		}
		scenarios.sort(Comparator.comparing(Scenario::getScenarioOrder));

		assignSequentialOrders(scenarios);
		Scenario lastScenario = scenarios.get(scenarios.size() - 1);

		return calculateLastOrder(lastScenario.getScenarioOrder());
	}


	private void assignSequentialOrders(final List<Scenario> scenarios) {
		int order = OrderCalculator.START_ORDER;
		for (Scenario scenario : scenarios) {
			scenario.updateScenarioOrder(order);
			order += OrderCalculator.DEFAULT_ORDER;
		}
	}

	private Integer calculateMiddleOrder(final Integer prevOrder, final Integer nextOrder) {
		return (prevOrder + nextOrder) / 2;
	}

	private Integer calculateStartOrder(final int minOrder) {
		return minOrder - DEFAULT_ORDER;
	}

	private Integer calculateLastOrder(final int maxOrder) {
		return maxOrder + DEFAULT_ORDER;
	}

	private void validateOrderGap(final Integer prevOrder, final Integer nextOrder, final int resultOrder) {
		int gap = nextOrder - prevOrder;
		if (gap <= MIN_GAP) {
			throw new ReorderRequiredException(resultOrder);
		}
	}

	private void validateOrderRange(final int order) {
		if (order < MIN_ORDER || order > MAX_ORDER) {
			throw new ReorderRequiredException(order);
		}
	}

}
