package com.und.server.scenario.event;

import java.time.LocalDate;

public record MissionCheckUpdateEvent(

	Long memberId,
	Long scenarioId,
	LocalDate date

) { }
