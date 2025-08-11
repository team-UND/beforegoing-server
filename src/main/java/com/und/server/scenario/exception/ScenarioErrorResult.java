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
	NOT_FOUND_MISSION(
		HttpStatus.NOT_FOUND, "Mission not found"),
	UNAUTHORIZED_ACCESS(
		HttpStatus.UNAUTHORIZED, "Unauthorized access"),
	UNSUPPORTED_MISSION_TYPE(
		HttpStatus.BAD_REQUEST, "Unsupported mission type"),
	REORDER_REQUIRED(
		HttpStatus.BAD_REQUEST, "Reorder required"),
	INVALID_TODAY_MISSION_DATE(
		HttpStatus.BAD_REQUEST, "Today mission can only be added for today or future dates"),
	INVALID_MISSION_FOUND_DATE(
		HttpStatus.BAD_REQUEST, "Mission can only be founded for mission dates"),
	MAX_SCENARIO_COUNT_EXCEEDED(
		HttpStatus.BAD_REQUEST, "Maximum scenario count exceeded"),
	MAX_MISSION_COUNT_EXCEEDED(
		HttpStatus.BAD_REQUEST, "Maximum mission count exceeded");

	private final HttpStatus httpStatus;
	private final String message;

}
