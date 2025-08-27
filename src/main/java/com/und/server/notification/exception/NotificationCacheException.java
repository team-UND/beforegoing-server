package com.und.server.notification.exception;

import com.und.server.common.exception.ErrorResult;
import com.und.server.common.exception.ServerException;

public class NotificationCacheException extends ServerException {

	public NotificationCacheException(ErrorResult errorResult) {
		super(errorResult);
	}

}
