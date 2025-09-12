package com.und.server.scenario.event;

import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.entity.Scenario;

public record ScenarioCreateEvent(

	Long memberId,
	Scenario scenario,
	NotificationType notificationType

) { }
