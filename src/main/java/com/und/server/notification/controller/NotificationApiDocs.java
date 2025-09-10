package com.und.server.notification.controller;

import org.springframework.http.ResponseEntity;

import com.und.server.common.dto.response.ErrorResponse;
import com.und.server.notification.dto.response.ScenarioNotificationListResponse;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;

public interface NotificationApiDocs {

	@Operation(summary = "Update Notification Active Status API")
	@ApiResponses({
			@ApiResponse(
					responseCode = "204",
					description = "Successfully updated notification active status",
					content = @Content
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized access",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class),
							examples = @ExampleObject(
									name = "Unauthorized access",
									value = """
										{
										  "code": "UNAUTHORIZED_ACCESS",
										  "message": "Unauthorized access"
										}
										"""
							)
					)
			)
	})
	ResponseEntity<Void> updateNotificationActive(
		@Parameter(hidden = true) final Long memberId,
		@Parameter(description = "Notification active status") @NotNull final Boolean isActive
	);


	@Operation(summary = "Get Scenario Notification List API")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Successfully retrieved scenario notification list",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ScenarioNotificationListResponse.class),
							examples = @ExampleObject(
									name = "Scenario notification list",
									value = """
										{
										  "etag": "1756272632565",
										  "scenarios": [
										    {
										      "scenarioId": 1,
										      "scenarioName": "Home out",
										      "memo": "Item to carry",
										      "notificationId": 2,
										      "notificationType": "TIME",
										      "notificationMethodType": "PUSH",
										      "daysOfWeekOrdinal": [0, 1, 2, 3, 4, 5, 6],
										      "notificationCondition": {
										        "notificationType": "TIME",
										        "startHour": 12,
										        "startMinute": 58
										      }
										    }
										  ]
										}
										"""
							)
					)
			),
			@ApiResponse(
					responseCode = "304",
					description = "Not modified - data has not changed since last request",
					content = @Content
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized access",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class),
							examples = @ExampleObject(
									name = "Unauthorized access",
									value = """
										{
										  "code": "UNAUTHORIZED_ACCESS",
										  "message": "Unauthorized access"
										}
										"""
							)
					)
			),
			@ApiResponse(
					responseCode = "500",
					description = "Server error - failed to retrieve notification cache",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class),
							examples = @ExampleObject(
									name = "Cache fetch failed",
									value = """
										{
										  "code": "CACHE_FETCH_ALL_FAILED",
										  "message": "Failed to fetch all scenarios notification cache"
										}
										"""
							)
					)
			)
	})
	ResponseEntity<ScenarioNotificationListResponse> getScenarioNotifications(
			@Parameter(hidden = true) final Long memberId,
			@Parameter(description = "ETag for client caching") final String ifNoneMatch
	);


	@Operation(summary = "Get Single Scenario Notification API")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Successfully retrieved scenario notification data",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ScenarioNotificationResponse.class),
							examples = @ExampleObject(
									name = "Time notification scenario",
									value = """
										{
										  "scenarioId": 1,
										  "scenarioName": "Home out",
										  "memo": "Item to carry",
										  "notificationId": 2,
										  "notificationType": "TIME",
										  "notificationMethodType": "PUSH",
										  "daysOfWeekOrdinal": [0, 1, 2, 3, 4, 5, 6],
										  "notificationCondition": {
										    "notificationType": "TIME",
										    "startHour": 12,
										    "startMinute": 58
										  }
										}
										"""
							)
					)
			),
			@ApiResponse(
					responseCode = "401",
					description = "Unauthorized access",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class),
							examples = @ExampleObject(
									name = "Unauthorized access",
									value = """
										{
										  "code": "UNAUTHORIZED_ACCESS",
										  "message": "Unauthorized access"
										}
										"""
							)
					)
			),
			@ApiResponse(
					responseCode = "404",
					description = "Scenario notification cache not found",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class),
							examples = @ExampleObject(
									name = "Scenario notification cache not found",
									value = """
										{
										  "code": "CACHE_NOT_FOUND_SCENARIO_NOTIFICATION",
										  "message": "Not found scenario notification cache"
										}
										"""
							)
					)
			),
			@ApiResponse(
					responseCode = "500",
					description = "Server error - failed to retrieve notification cache",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class),
							examples = @ExampleObject(
									name = "Cache fetch failed",
									value = """
										{
										  "code": "CACHE_FETCH_SINGLE_FAILED",
										  "message": "Failed to fetch single scenario notification cache"
										}
										"""
							)
					)
			)
	})
	ResponseEntity<ScenarioNotificationResponse> getSingleScenarioNotification(
			@Parameter(hidden = true) final Long memberId,
			@Parameter(description = "Scenario ID") final Long scenarioId
	);

}
