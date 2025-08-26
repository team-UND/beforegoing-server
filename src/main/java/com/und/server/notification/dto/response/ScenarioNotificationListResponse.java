package com.und.server.notification.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ETag and Scenario notification list response with notification active")
public record ScenarioNotificationListResponse(

	@Schema(description = "ETag (클라이언트 캐싱용)")
	String etag,

	@Schema(description = "시나리오별 알림 목록")
	List<ScenarioNotificationResponse> scenarios

) {

	public static ScenarioNotificationListResponse from(
		final String etag, final List<ScenarioNotificationResponse> scenarios
	) {
		return new ScenarioNotificationListResponse(etag, scenarios);
	}

}
