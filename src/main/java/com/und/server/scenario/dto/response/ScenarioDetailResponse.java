package com.und.server.scenario.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.NotificationResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Scenario detail response")
public record ScenarioDetailResponse(

	@Schema(description = "Scenario id", example = "1")
	Long scenarioId,

	@Schema(description = "Scenario name", example = "Home out")
	String scenarioName,

	@Schema(description = "Scenario memo", example = "Item to carry")
	String memo,

	@ArraySchema(
		arraySchema = @Schema(description = "Basic type mission list, Sort in order"),
		schema = @Schema(implementation = MissionResponse.class), maxItems = 20
	)
	List<MissionResponse> basicMissions,

	@Schema(
		description = "Notification default settings",
		implementation = NotificationResponse.class
	)
	NotificationResponse notification,

	@Schema(
		description = "Notification details condition that are included only when the notification is active",
		discriminatorProperty = "notificationType",
		discriminatorMapping = {
			@DiscriminatorMapping(value = "TIME", schema = TimeNotificationResponse.class)
		},
		requiredMode = Schema.RequiredMode.NOT_REQUIRED,
		nullable = true
	)
	NotificationConditionResponse notificationCondition

) {

	public static ScenarioDetailResponse from(
		Scenario scenario,
		List<Mission> basicMissionList,
		NotificationResponse notificationResponse,
		NotificationConditionResponse notificationConditionResponse
	) {
		return ScenarioDetailResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.memo(scenario.getMemo())
			.basicMissions(MissionResponse.listFrom(basicMissionList))
			.notification(notificationResponse)
			.notificationCondition(notificationConditionResponse)
			.build();
	}

}
