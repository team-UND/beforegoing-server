package com.und.server.notification.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ETag and Scenario notification list response with notification active")
public record ScenarioNotificationListResponse(

	@Schema(description = "ETag (for client caching)")
	String etag,

	@Schema(description = "Notification list by scenario")
	List<ScenarioNotificationResponse> scenarios

) {

	public static ScenarioNotificationListResponse from(
		final String etag, final List<ScenarioNotificationResponse> scenarios
	) {
		return new ScenarioNotificationListResponse(etag, scenarios);
	}

}
