package com.und.server.weather.dto;

import lombok.Builder;

@Builder
public record GridPoint(

	int gridX,
	int gridY

) {

	public static GridPoint from(final int gridX, final int gridY) {
		return new GridPoint(gridX, gridY);
	}

}
