package com.und.server.scenario.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.und.server.common.dto.response.ErrorResponse;
import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.dto.response.OrderUpdateResponse;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

public interface ScenarioApiDocs {

	@Operation(summary = "Get Scenarios API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Get scenarios successful",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ScenarioResponse.class)
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
					value = "{\"code\":\"UNAUTHORIZED_ACCESS\", \"message\":\"Unauthorized access\"}"
				)
			)
		)
	})
	ResponseEntity<List<ScenarioResponse>> getScenarios(
		@Parameter(hidden = true) final Long memberId,
		@Parameter(description = "Notification type filter (TIME, LOCATION)") final NotificationType notificationType
	);


	@Operation(summary = "Get Scenario Detail API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Get scenario detail successful",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ScenarioDetailResponse.class),
				examples = {
					@ExampleObject(
						name = "(TIME) With notification",
						value = """
							{
							  "scenarioId": 1,
							  "scenarioName": "Home out",
							  "memo": "Item to carry",
							  "basicMissions": [
							    {
							      "missionId": 1,
							      "content": "Lock door",
							      "isChecked": false,
							      "missionType": "BASIC"
							    },
							    {
							      "missionId": 2,
							      "content": "Turn off lights",
							      "isChecked": true,
							      "missionType": "BASIC"
							    }
							  ],
							  "notification": {
							    "notificationId": 2,
							    "notificationType": "TIME",
							    "notificationMethodType": "PUSH",
							    "isActive": true,
							    "daysOfWeekOrdinal": [0, 1, 2, 3, 4, 5, 6]
							  },
							  "notificationCondition": {
							    "notificationType": "TIME",
							    "startHour": 12,
							    "startMinute": 58
							  }
							}
							"""
					),
					@ExampleObject(
						name = "(TIME) Without notification",
						value = """
							{
							  "scenarioId": 2,
							  "scenarioName": "Home out",
							  "memo": "Item to carry",
							  "basicMissions": [
							    {
							        "missionId": 3,
							        "content": "Lock door",
							        "isChecked": false,
							        "missionType": "BASIC"
							    },
							    {
							        "missionId": 4,
							        "content": "Open door",
							        "isChecked": false,
							        "missionType": "BASIC"
							    }
							  ],
							  "notification": {
							    "notificationId": 2,
							    "isActive": false,
							    "notificationType": "TIME"
							  }
							}
							"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad Request",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "Unsupported mission type",
						value = "{\"code\":\"UNSUPPORTED_MISSION_TYPE\", \"message\":\"Unsupported mission type\"}"
					)
				}
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
					value = "{\"code\":\"UNAUTHORIZED_ACCESS\", \"message\":\"Unauthorized access\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "Scenario not found",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					name = "Scenario not found",
					value = "{\"code\":\"NOT_FOUND_SCENARIO\", \"message\":\"Scenario not found\"}"
				)
			)
		)
	})
	ResponseEntity<ScenarioDetailResponse> getScenarioDetail(
		@Parameter(hidden = true) final Long memberId,
		@Parameter(description = "Scenario ID") final Long scenarioId
	);


	@Operation(summary = "Add Scenario API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "201",
			description = "Create Scenario successful",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = MissionGroupResponse.class),
				examples = @ExampleObject(
					name = "Basic missions only",
					value = """
						{
						  "scenarioId": 2,
						  "basicMissions": [
						    {
						      "missionId": 3,
						      "content": "Lock door",
						      "isChecked": false,
						      "missionType": "BASIC"
						    },
						    {
						      "missionId": 4,
						      "content": "Open door",
						      "isChecked": false,
						      "missionType": "BASIC"
						    }
						  ],
						  "todayMissions": []
						}
						"""
				)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad request - invalid parameters",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "Scenario name must not be blank",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Scenario name must not be blank\"}"
					),
					@ExampleObject(
						name = "Scenario name must be at most 10 characters",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Scenario name must be at most 10 characters\"}"
					),
					@ExampleObject(
						name = "Memo must be at most 15 characters",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Memo must be at most 15 characters\"}"
					),
					@ExampleObject(
						name = "Basic mission content must not be blank",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Basic mission content must not be blank\"}"
					),
					@ExampleObject(
						name = "Basic mission content must be at most 10 characters",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Basic mission content must be at most 50 characters\"}"
					),
					@ExampleObject(
						name = "Unsupported mission type",
						value = "{\"code\":\"UNSUPPORTED_MISSION_TYPE\", \"message\":\"Unsupported mission type\"}"
					),
					@ExampleObject(
						name = "Max scenario count exceeded",
						value = "{\"code\":\"MAX_SCENARIO_COUNT_EXCEEDED\", \"message\":\"Maximum scenario count exceeded\"}"
					)
				}
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
					value = "{\"code\":\"UNAUTHORIZED_ACCESS\", \"message\":\"Unauthorized access\"}"
				)
			)
		)
	})
	ResponseEntity<MissionGroupResponse> addScenario(
		@Parameter(hidden = true) Long memberId,
		@RequestBody(
			description = "Scenario detail request",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ScenarioDetailRequest.class),
				examples = {
					@ExampleObject(
						name = "(TIME) With notification",
						value = """
							{
							  "scenarioName": "Home out",
							  "memo": "Item to carry",
							  "basicMissions": [
							    { "content": "Lock door" },
							    { "content": "Open door" }
							  ],
							  "notification": {
							    "isActive": true,
							    "notificationType": "time",
							    "notificationMethodType": "alarm",
							    "daysOfWeekOrdinal": [0,1,2]
							  },
							  "notificationCondition": {
							    "notificationType": "time",
							    "startHour": 1,
							    "startMinute": 5
							  }
							}
							"""
					),
					@ExampleObject(
						name = "(TIME) Without notification",
						value = """
							{
							  "scenarioName": "Home out",
							  "memo": "Item to carry",
							  "basicMissions": [
							    { "content": "Lock door" },
							    { "content": "Open door" }
							  ],
							  "notification": {
							    "isActive": false,
							    "notificationType": "time"
							  }
							}
							"""
					)
				}
			)
		)
		ScenarioDetailRequest scenarioRequest
	);


	@Operation(summary = "Update Scenario API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Update Scenario successful",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = MissionGroupResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad request - invalid parameters",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "Scenario name must not be blank",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Scenario name must not be blank\"}"
					),
					@ExampleObject(
						name = "Scenario name must be at most 10 characters",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Scenario name must be at most 10 characters\"}"
					),
					@ExampleObject(
						name = "Memo must be at most 15 characters",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Memo must be at most 15 characters\"}"
					),
					@ExampleObject(
						name = "Basic mission content must not be blank",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Basic mission content must not be blank\"}"
					),
					@ExampleObject(
						name = "Basic mission content must be at most 10 characters",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Basic mission content must be at most 50 characters\"}"
					),
					@ExampleObject(
						name = "Unsupported mission type",
						value = "{\"code\":\"UNSUPPORTED_MISSION_TYPE\", \"message\":\"Unsupported mission type\"}"
					),
					@ExampleObject(
						name = "Max scenario count exceeded",
						value = "{\"code\":\"MAX_SCENARIO_COUNT_EXCEEDED\", \"message\":\"Maximum scenario count exceeded\"}"
					)
				}
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
					value = "{\"code\":\"UNAUTHORIZED_ACCESS\", \"message\":\"Unauthorized access\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "Scenario not found",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					name = "Scenario not found",
					value = "{\"code\":\"NOT_FOUND_SCENARIO\", \"message\":\"Scenario not found\"}"
				)
			)
		)
	})
	ResponseEntity<MissionGroupResponse> updateScenario(
		@Parameter(hidden = true) Long memberId,
		@Parameter(description = "Scenario ID") Long scenarioId,
		@RequestBody(
			description = "Scenario detail request",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ScenarioDetailRequest.class),
				examples = {
					@ExampleObject(
						name = "(TIME) Without notification",
						value = """
                                {
                                  "scenarioName": "Home out",
                                  "memo": "Item to carry",
                                  "basicMissions": [
                                    { "content": "new" },
                                    { "missionId": 1, "content": "old" }
                                  ],
                                  "notification": {
                                    "isActive": false,
                                    "notificationType": "time"
                                  }
                                }
                                """
					)
				}
			)
		)
		ScenarioDetailRequest scenarioRequest
	);



	@Operation(summary = "Update Scenario Order API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Update Scenario order successful",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = OrderUpdateResponse.class),
				examples = {
					@ExampleObject(
						name = "No reorder required",
						value = """
													{
													  "isReorder": false,
													  "orderUpdates": [
													    {
													      "id": 1,
													      "newOrder": 100500
													    }
													  ]
													}
													"""
					),
					@ExampleObject(
						name = "Reorder required",
						value = """
													{
													  "isReorder": true,
													  "orderUpdates": [
													    {
													      "id": 1,
													      "newOrder": 100000
													    },
													    {
													      "id": 2,
													      "newOrder": 101000
													    }
													  ]
													}
													"""
					)
				}
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad request - invalid parameters",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "PrevOrder must be greater than or equal to 1",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"PrevOrder must be greater than or equal to 1\"}"
					),
					@ExampleObject(
						name = "NextOrder must be greater than or equal to 1",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"NextOrder must be greater than or equal to 1\"}"
					)
				}
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
					value = "{\"code\":\"UNAUTHORIZED_ACCESS\", \"message\":\"Unauthorized access\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "Scenario not found",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					name = "Scenario not found",
					value = "{\"code\":\"NOT_FOUND_SCENARIO\", \"message\":\"Scenario not found\"}"
				)
			)
		)
	})
	ResponseEntity<OrderUpdateResponse> updateScenarioOrder(
		@Parameter(hidden = true) Long memberId,
		@Parameter(description = "Scenario ID") Long scenarioId,
		@RequestBody(
			description = "Scenario order update request",
			required = true,
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ScenarioOrderUpdateRequest.class),
				examples = {
					@ExampleObject(
						name = "Move to front",
						value = """
                                {
                                  "prevOrder": null,
                                  "nextOrder": 100000
                                }
                                """
					),
					@ExampleObject(
						name = "Move to back",
						value = """
                                {
                                  "prevOrder": 101000,
                                  "nextOrder": null
                                }
                                """
					)
				}
			)
		)
		ScenarioOrderUpdateRequest scenarioOrderUpdateRequest
	);


	@Operation(summary = "Delete Scenario API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "204",
			description = "Delete Scenario successful"
		),
		@ApiResponse(
			responseCode = "401",
			description = "Unauthorized access",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					name = "Unauthorized access",
					value = "{\"code\":\"UNAUTHORIZED_ACCESS\", \"message\":\"Unauthorized access\"}"
				)
			)
		),
		@ApiResponse(
			responseCode = "404",
			description = "Scenario not found",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					name = "Scenario not found",
					value = "{\"code\":\"NOT_FOUND_SCENARIO\", \"message\":\"Scenario not found\"}"
				)
			)
		)
	})
	ResponseEntity<Void> deleteScenario(
		@Parameter(hidden = true) Long memberId,
		@Parameter(description = "Scenario ID") Long scenarioId
	);

}
