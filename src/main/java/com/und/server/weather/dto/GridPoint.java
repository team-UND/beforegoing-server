package com.und.server.weather.dto;

import lombok.Builder;

@Builder
public record GridPoint(

	int gridX,
	int gridY

) {

	public static GridPoint from(int gridX, int gridY) {
		return new GridPoint(gridX, gridY);
	}

}
