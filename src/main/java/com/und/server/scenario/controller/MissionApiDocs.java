package com.und.server.scenario.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.und.server.common.dto.response.ErrorResponse;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.dto.response.MissionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface MissionApiDocs {

	@Operation(summary = "Get Missions by Scenario ID API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Get missions successful, Return empty array if no mission exists",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = MissionGroupResponse.class)
			)
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad request",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "Invalid mission found date",
						value = "{\"code\":\"INVALID_MISSION_FOUND_DATE\", \"message\":\"Mission can only be founded for mission dates\"}"
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
	ResponseEntity<MissionGroupResponse> getMissionsByScenarioId(
		@Parameter(hidden = true) final Long memberId,
		@Parameter(description = "Scenario ID") final Long scenarioId,
		@Parameter(description = "Target date for missions (yyyy-MM-dd)") final LocalDate date
	);


	@Operation(summary = "Add Today Mission to Scenario API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "201",
			description = "Create Today Mission successful",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = MissionResponse.class)
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
						name = "Content must not be blank",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Content must not be blank\"}"
					),
					@ExampleObject(
						name = "Content must be at most 10 characters",
						value = "{\"code\":\"INVALID_PARAMETER\", \"message\":\"Content must be at most 10 characters\"}"
					),
					@ExampleObject(
						name = "Invalid today mission date",
						value = "{\"code\":\"INVALID_TODAY_MISSION_DATE\", \"message\":\"Today mission can only be added for today or future dates\"}"
					),
					@ExampleObject(
						name = "Max mission count exceeded",
						value = "{\"code\":\"MAX_MISSION_COUNT_EXCEEDED\", \"message\":\"Maximum mission count exceeded\"}"
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
	ResponseEntity<MissionResponse> addTodayMissionToScenario(
		@Parameter(hidden = true) final Long memberId,
		@Parameter(description = "Scenario ID") final Long scenarioId,
		@Parameter(description = "Today mission request") @Valid final TodayMissionRequest missionAddRequest,
		@Parameter(description = "Target date for mission (yyyy-MM-dd)") final LocalDate date
	);


	@Operation(summary = "Update Mission Check Status API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "204",
			description = "Update check status successful"
		),
		@ApiResponse(
			responseCode = "400",
			description = "Bad Request",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = {
					@ExampleObject(
						name = "Invalid today mission date",
						value = "{\"code\":\"INVALID_TODAY_MISSION_DATE\", \"message\":\"Today mission can only be added for today or future dates\"}"
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
			description = "Mission not found",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					name = "Mission not found",
					value = "{\"code\":\"NOT_FOUND_MISSION\", \"message\":\"Mission not found\"}"
				)
			)
		)
	})
	ResponseEntity<Void> updateMissionCheck(
		@Parameter(hidden = true) final Long memberId,
		@Parameter(description = "Mission ID") final Long missionId,
		@Parameter(description = "Check status to update") @NotNull final Boolean isChecked,
		@Parameter(description = "Target date for mission (yyyy-MM-dd)") final LocalDate date
	);


	@Operation(summary = "Delete Today Mission API")
	@ApiResponses({
		@ApiResponse(
			responseCode = "204",
			description = "Delete Today Mission successful"
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
			description = "Mission not found",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = ErrorResponse.class),
				examples = @ExampleObject(
					name = "Mission not found",
					value = "{\"code\":\"NOT_FOUND_MISSION\", \"message\":\"Mission not found\"}"
				)
			)
		)
	})
	ResponseEntity<Void> deleteTodayMissionById(
		@Parameter(hidden = true) final Long memberId,
		@Parameter(description = "Mission ID") final Long missionId
	);

}
