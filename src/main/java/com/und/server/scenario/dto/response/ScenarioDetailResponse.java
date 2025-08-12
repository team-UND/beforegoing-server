package com.und.server.scenario.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.NotificationResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Scenario detail response")
public class ScenarioDetailResponse {

	@Schema(description = "Scenario id", example = "1")
	private Long scenarioId;

	@Schema(description = "Scenario name", example = "Before house")
	private String scenarioName;

	@Schema(description = "Scenario memo", example = "Item to carry")
	private String memo;

	@ArraySchema(
		arraySchema = @Schema(description = "Basic type mission list, Sort in order"),
		schema = @Schema(implementation = MissionResponse.class), maxItems = 20
	)
	private List<MissionResponse> basicMissionList;

	@Schema(
		description = "Notification default settings",
		implementation = NotificationResponse.class
	)
	private NotificationResponse notification;

	@Schema(
		description = "Notification details condition that are included only when the notification is active",
		discriminatorProperty = "notificationType",
		discriminatorMapping = {
			@DiscriminatorMapping(value = "TIME", schema = TimeNotificationResponse.class)
		},
		requiredMode = Schema.RequiredMode.NOT_REQUIRED,
		nullable = true
	)
	private NotificationConditionResponse notificationCondition;

}
