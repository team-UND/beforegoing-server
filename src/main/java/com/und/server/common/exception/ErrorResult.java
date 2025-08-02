package com.und.server.common.exception;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

public interface ErrorResult extends Serializable {

	String name();

	HttpStatus getHttpStatus();

	String getMessage();

}
