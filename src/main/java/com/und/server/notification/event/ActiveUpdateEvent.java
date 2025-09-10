package com.und.server.notification.event;

public record ActiveUpdateEvent(

	Long memberId,
	boolean isActive

) { }
