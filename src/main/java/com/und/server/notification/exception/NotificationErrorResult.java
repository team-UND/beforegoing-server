package com.und.server.notification.exception;

import org.springframework.http.HttpStatus;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorResult implements ErrorResult {

	UNSUPPORTED_NOTIF(
		HttpStatus.BAD_REQUEST, "Unsupported notification type"),
	NOT_FOUND_NOTIF(
		HttpStatus.NOT_FOUND, "Notification not found");

	private final HttpStatus httpStatus;
	private final String message;

}
