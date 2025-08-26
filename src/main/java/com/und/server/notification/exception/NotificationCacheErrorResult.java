package com.und.server.notification.exception;

import org.springframework.http.HttpStatus;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationCacheErrorResult implements ErrorResult {

	CACHE_FETCH_ALL_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch all scenarios notification cache"),
	CACHE_FETCH_SINGLE_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch single scenario notification cache"),
	CACHE_UPDATE_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update notification cache"),
	CACHE_DELETE_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete notification cache"),
	SERIALIZE_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize NotificationCacheData"),
	DESERIALIZE_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to deserialize NotificationCacheData"),
	CONDITION_PARSE_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse NotificationConditionResponse from cache"),
	CONDITION_SERIALIZE_FAILED(
		HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize NotificationConditionResponse");

	private final HttpStatus httpStatus;
	private final String message;

}
