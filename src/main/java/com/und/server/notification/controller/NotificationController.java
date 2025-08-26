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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Scenario Notification", description = "Notification related APIs")
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
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
	@GetMapping("/scenarios")
	public ResponseEntity<ScenarioNotificationListResponse> getScenarioNotifications(
		@AuthMember Long memberId,
		@Parameter(description = "ETag for client caching")
		@RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
	) {
		ScenarioNotificationListResponse response =
			notificationCacheService.getScenariosNotificationCache(memberId);

		if (ifNoneMatch != null && ifNoneMatch.equals(response.etag())) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}

		return ResponseEntity.ok()
			.header("ETag", response.etag())
			.body(response);
	}


	@Operation(
		summary = "Get single scenario notification",
		description = "Retrieve notification data for a specific scenario. Returns null if the scenario does not exist."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200", description = "Successfully retrieved scenario notification data",
			content = @Content(schema = @Schema(implementation = ScenarioNotificationResponse.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
		@ApiResponse(
			responseCode = "500", description = "Internal server error - failed to retrieve notification cache")
	})
	@GetMapping("/scenarios/{scenarioId}")
	public ResponseEntity<ScenarioNotificationResponse> getSingleScenarioNotification(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId
	) {
		ScenarioNotificationResponse response =
			notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId);

		if (response == null) {
			return ResponseEntity.ok().body(null);
		}
		return ResponseEntity.ok().body(response);
	}

}
