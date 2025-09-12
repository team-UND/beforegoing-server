package com.und.server.scenario.event;

import java.time.LocalDate;

public record MissionCreateEvent(

	Long memberId,
	Long scenarioId,
	LocalDate date

) { }
