package com.und.server.scenario.dto.request;

import java.util.List;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import com.und.server.notification.dto.request.NotificationConditionRequest;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class ScenarioDetailRequest {

	@NotBlank(message = "Scenario name must not be blank")
	@Size(max = 10, message = "Scenario name must be at most 10 characters")
	private String scenarioName;

	@Size(max = 15, message = "Memo must be at most 15 characters")
	private String memo;

	@Size(max = 20, message = "Maximum mission count exceeded")
	private List<MissionRequest> basicMissionList;

	private NotificationRequest notification;

	@Schema(
		description = "알림 상세 정보",
		oneOf = {TimeNotificationRequest.class},
		discriminatorProperty = "notificationType",
		discriminatorMapping = {
			@DiscriminatorMapping(value = "TIME", schema = TimeNotificationRequest.class)
		}
	)
	private NotificationConditionRequest notificationCondition;

}
