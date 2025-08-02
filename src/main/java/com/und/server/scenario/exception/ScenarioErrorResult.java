package com.und.server.scenario.exception;

import org.springframework.http.HttpStatus;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScenarioErrorResult implements ErrorResult {
	NOT_FOUND_SCENARIO(
		HttpStatus.NOT_FOUND, "Scenario not found"),
	UNAUTHORIZED_ACCESS(
		HttpStatus.UNAUTHORIZED, "Unauthorized access");


	private final HttpStatus httpStatus;
	private final String message;

}
