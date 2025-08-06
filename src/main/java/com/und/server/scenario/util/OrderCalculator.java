package com.und.server.scenario.util;

import org.springframework.stereotype.Component;

import com.und.server.scenario.exception.ReorderRequiredException;

@Component
public class OrderCalculator {

	public static final int START_ORDER = 1000;
	public static final int DEFAULT_ORDER = 1000;
	private static final int MIN_ORDER = 0;
	private static final int MAX_ORDER = 10_000_000;
	private static final int MIN_GAP = 100;


	public int getOrder(Integer prevOrder, Integer nextOrder) {
		int resultOrder = 0;

		if (prevOrder == null && nextOrder == null) {
			return START_ORDER;
		}
		if (prevOrder == null) {
			resultOrder = calculateStartOrder(nextOrder);
		} else if (nextOrder == null) {
			resultOrder = calculateLastOrder(prevOrder);
		} else {
			validateOrderGap(prevOrder, nextOrder);
			resultOrder = calculateMiddleOrder(prevOrder, nextOrder);
		}
		validateOrderRange(resultOrder);

		return resultOrder;
	}


	private Integer calculateMiddleOrder(Integer prevOrder, Integer nextOrder) {
		return (prevOrder + nextOrder) / 2;
	}

	private Integer calculateStartOrder(int minOrder) {
		return minOrder - DEFAULT_ORDER;
	}

	private Integer calculateLastOrder(int maxOrder) {
		return maxOrder + DEFAULT_ORDER;
	}

	private void validateOrderGap(Integer prevOrder, Integer nextOrder) {
		int gap = nextOrder - prevOrder;
		if (gap <= MIN_GAP) {
			throw new ReorderRequiredException();
		}
	}

	private void validateOrderRange(int order) {
		if (order < MIN_ORDER || order > MAX_ORDER) {
			throw new ReorderRequiredException();
		}
	}

}
