package com.und.server.scenario.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.NotificationResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScenarioDetailResponse {

	private Long scenarioId;
	private String scenarioName;
	private String memo;
	private List<MissionResponse> basicMissionList;

	private NotificationResponse notification;

	@Schema(
		description = "알림 상세 정보",
		oneOf = {TimeNotificationResponse.class},
		discriminatorProperty = "notificationType",
		discriminatorMapping = {
			@DiscriminatorMapping(value = "TIME", schema = TimeNotificationResponse.class)
		}
	)
	private NotificationConditionResponse notificationCondition;

}
