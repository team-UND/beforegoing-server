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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
@Schema(description = "Scenario detail request for create and update")
public class ScenarioDetailRequest {

	@Schema(description = "Scenario id", example = "Before house")
	@NotBlank(message = "Scenario name must not be blank")
	@Size(max = 10, message = "Scenario name must be at most 10 characters")
	private String scenarioName;

	@Schema(description = "Scenario memo", example = "Item to carry")
	@Size(max = 15, message = "Memo must be at most 15 characters")
	private String memo;

	@ArraySchema(
		arraySchema = @Schema(description = "Basic type mission list"),
		schema = @Schema(implementation = BasicMissionRequest.class), maxItems = 20
	)
	@Size(max = 20, message = "Maximum mission count exceeded")
	@Valid
	private List<BasicMissionRequest> basicMissionList;

	@Schema(
		description = "Notification default settings",
		implementation = NotificationRequest.class
	)
	private NotificationRequest notification;

	@Schema(
		description = "Notification details condition that are included only when the notification is active",
		discriminatorProperty = "notificationType",
		discriminatorMapping = {
			@DiscriminatorMapping(value = "time", schema = TimeNotificationRequest.class)
		}
	)
	private NotificationConditionRequest notificationCondition;

}
