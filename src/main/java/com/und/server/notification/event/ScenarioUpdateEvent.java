package com.und.server.notification.event;

import com.und.server.scenario.entity.Scenario;

public record ScenarioUpdateEvent(

	Long memberId,
	Scenario scenario

) { }
