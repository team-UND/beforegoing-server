package com.und.server.notification.event;

public record ScenarioDeleteEvent(

	Long memberId,
	Long scenarioId,
	Boolean isNotificationActive

) { }
