package com.und.server.scenario.dto.request;

import java.util.List;

import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Scenario detail request for create and update")
public record ScenarioDetailRequest(

	@Schema(description = "Scenario name", example = "Home out")
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
	List<BasicMissionRequest> basicMissions,

	@Schema(
		description = "Notification default settings",
		implementation = NotificationRequest.class
	)
	@Valid
	NotificationRequest notification,

	@Schema(
		description = "Notification details condition that are included only when the notification is active",
		discriminatorProperty = "notificationType",
		discriminatorMapping = {
			@DiscriminatorMapping(value = "time", schema = TimeNotificationRequest.class)
		}
	)
	@Valid
	NotificationConditionRequest notificationCondition

) { }
