package com.und.server.notification.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LocationTrackingRadiusType {

	M_100(100),
	M_500(500),
	KM_1(1_000),
	KM_2(2_000),
	KM_3(3_000),
	KM_4(4_000);

	private final int meters;

}
