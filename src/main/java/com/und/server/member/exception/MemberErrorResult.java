package com.und.server.member.exception;

import org.springframework.http.HttpStatus;

import com.und.server.common.exception.ErrorResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorResult implements ErrorResult {

	INVALID_MEMBER_ID(HttpStatus.BAD_REQUEST, "Invalid Member ID"),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member Not Found"),
	DUPLICATE_KAKAO_ID(HttpStatus.CONFLICT, "Duplicate Kakao ID"),
	DUPLICATE_APPLE_ID(HttpStatus.CONFLICT, "Duplicate Apple ID");

	private final HttpStatus httpStatus;
	private final String message;

}
