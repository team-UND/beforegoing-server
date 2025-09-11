package com.und.server.scenario.event;

public record ActiveUpdateEvent(

	Long memberId,
	boolean isActive

) { }
