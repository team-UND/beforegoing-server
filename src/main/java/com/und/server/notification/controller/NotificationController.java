package com.und.server.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.notification.dto.response.ScenarioNotificationListResponse;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.service.NotificationCacheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class NotificationController {

	private final NotificationCacheService notificationCacheService;


	@Operation(
		summary = "Get scenario notification list",
		description = "Retrieve the list of scenario notifications for the user."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200", description = "Successfully retrieved scenario notification list",
			content = @Content(schema = @Schema(implementation = ScenarioNotificationListResponse.class))),
		@ApiResponse(responseCode = "304", description = "Not modified - data has not changed since last request"),
		@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
		@ApiResponse(
			responseCode = "500", description = "Internal server error - failed to retrieve notification cache")
	})
	@GetMapping("/scenarios/notifications")
	public ResponseEntity<ScenarioNotificationListResponse> getScenarioNotifications(
		@AuthMember final Long memberId,
		@Parameter(description = "ETag for client caching")
		@RequestHeader(value = "If-None-Match", required = false) final String ifNoneMatch
	) {
		final ScenarioNotificationListResponse scenarioNotificationListResponse =
			notificationCacheService.getScenariosNotificationCache(memberId);

		if (ifNoneMatch != null && ifNoneMatch.equals(scenarioNotificationListResponse.etag())) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}

		return ResponseEntity.ok()
			.header("ETag", scenarioNotificationListResponse.etag())
			.body(scenarioNotificationListResponse);
	}


	@Operation(
		summary = "Get single scenario notification",
		description = "Retrieve notification data for a specific scenario."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200", description = "Successfully retrieved scenario notification data",
			content = @Content(schema = @Schema(implementation = ScenarioNotificationResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
		@ApiResponse(responseCode = "404", description = "Scenario not found or scenario has no notification"),
		@ApiResponse(
			responseCode = "500", description = "Internal server error - failed to retrieve notification cache")
	})
	@GetMapping("/scenarios/{scenarioId}/notifications")
	public ResponseEntity<ScenarioNotificationResponse> getSingleScenarioNotification(
		@AuthMember final Long memberId,
		@PathVariable final Long scenarioId
	) {
		final ScenarioNotificationResponse scenarioNotificationResponse =
			notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId);

		return ResponseEntity.ok().body(scenarioNotificationResponse);
	}

}
