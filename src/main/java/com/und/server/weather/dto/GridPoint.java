package com.und.server.weather.dto;

import lombok.Builder;

@Builder
public record GridPoint(

	int x,
	int y

) { }
