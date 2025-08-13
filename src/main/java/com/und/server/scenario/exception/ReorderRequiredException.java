package com.und.server.scenario.exception;

import com.und.server.common.exception.ServerException;

import lombok.Getter;

@Getter
public class ReorderRequiredException extends ServerException {

	private final int errorOrder;

	public ReorderRequiredException(int errorOrder) {
		super(ScenarioErrorResult.REORDER_REQUIRED);
		this.errorOrder = errorOrder;
	}

}
