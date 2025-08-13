package com.und.server.scenario.constants;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MissionType {

	BASIC, TODAY;

	@JsonCreator
	public static MissionType fromValue(final String value) {
		if (value == null) {
			return null;
		}
		return MissionType.valueOf(value.toUpperCase());
	}

}
