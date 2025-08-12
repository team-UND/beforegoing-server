package com.und.server.scenario.dto.request;

import java.util.List;

import com.und.server.notification.constants.NotificationType;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Scenario no notification request for create and update")
public record ScenarioNoNotificationRequest(

	@Schema(description = "Scenario id", example = "Before house")
	@NotBlank(message = "Scenario name must not be blank")
	@Size(max = 10, message = "Scenario name must be at most 10 characters")
	String scenarioName,

	@Schema(description = "Scenario memo", example = "Item to carry")
	@Size(max = 15, message = "Memo must be at most 15 characters")
	String memo,

	@ArraySchema(
		arraySchema = @Schema(description = "Basic type mission list"),
		schema = @Schema(implementation = BasicMissionRequest.class), maxItems = 20
	)
	@Size(max = 20, message = "Maximum mission count exceeded")
	@Valid
	List<BasicMissionRequest> basicMissionList,

	@Schema(description = "Notification type", example = "TIME")
	@NotNull(message = "notificationType must not be null")
	NotificationType notificationType

) { }
