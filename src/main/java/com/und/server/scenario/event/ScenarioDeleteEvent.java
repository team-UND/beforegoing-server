package com.und.server.scenario.event;

import com.und.server.notification.constants.NotificationType;

public record ScenarioDeleteEvent(

	Long memberId,
	Long scenarioId,
	Boolean isNotificationActive,
	NotificationType notificationType

) { }
