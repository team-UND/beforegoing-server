package com.und.server.scenario.event;

public record MissionDeleteEvent(

	Long memberId,
	Long scenarioId

) { }
