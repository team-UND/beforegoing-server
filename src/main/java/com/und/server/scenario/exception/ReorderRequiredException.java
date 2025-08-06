package com.und.server.scenario.exception;

import com.und.server.common.exception.ServerException;

public class ReorderRequiredException extends ServerException {

	public ReorderRequiredException() {
		super(ScenarioErrorResult.REORDER_REQUIRED);
	}

}
