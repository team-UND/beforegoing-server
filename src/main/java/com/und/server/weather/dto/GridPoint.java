package com.und.server.weather.dto;

import lombok.Builder;

/**
 * 기상청 격자 좌표
 */
@Builder
public record GridPoint(

	int x,  // 격자 X 좌표
	int y   // 격자 Y 좌표

) {}
