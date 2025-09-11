package com.und.server.scenario.event;

public record ScenarioDeleteEvent(

	Long memberId,
	Long scenarioId,
	Boolean isNotificationActive

) { }
