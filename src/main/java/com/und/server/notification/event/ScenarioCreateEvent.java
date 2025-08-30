package com.und.server.notification.event;

import com.und.server.scenario.entity.Scenario;

public record ScenarioCreateEvent(

	Long memberId,
	Scenario scenario

) { }
