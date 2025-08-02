package com.und.server.scenario.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.entity.Scenario;

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
	private List<MissionResponse> missionList;

	private Long notificationId;
	private Boolean isActive;
	private NotifType notificationType;
	private List<NotificationDetailResponse> notificationDetailList;


	public static ScenarioDetailResponse of(Scenario scenario,
											List<NotificationDetailResponse> notificationDetailList) {
		Notification notification = scenario.getNotification();
		List<MissionResponse> missionResponseList = MissionResponse.listOf(scenario.getMissionList());

		return ScenarioDetailResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.memo(scenario.getMemo())
			.missionList(missionResponseList)
			.notificationId(notification.getId())
			.isActive(notification.isActive())
			.notificationType(notification.getNotifType())
			.notificationDetailList(notificationDetailList.isEmpty() ? null : notificationDetailList)
			.build();
	}
}
