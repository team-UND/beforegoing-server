package com.und.server.scenario.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NofitDayOfWeekResponse;
import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.dto.NotificationInfoDto;
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
	private Boolean isEveryDay;
	private List<NofitDayOfWeekResponse> dayOfWeekOrdinalList;
	private NotificationDetailResponse notificationDetail;


	public static ScenarioDetailResponse of(Scenario scenario, NotificationInfoDto notifInfo) {
		Notification notification = scenario.getNotification();
		List<MissionResponse> missionResponseList = MissionResponse.listOf(scenario.getMissionList());

		ScenarioDetailResponse result = ScenarioDetailResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.memo(scenario.getMemo())
			.missionList(missionResponseList)
			.notificationId(notification.getId())
			.isActive(notification.isActive())
			.notificationType(notification.getNotifType())
			.build();

		if (notifInfo != null) {
			result.setIsEveryDay(notifInfo.isEveryDay());
			result.setDayOfWeekOrdinalList(notifInfo.dayOfWeekOrdinalList());
			result.setNotificationDetail(notifInfo.notificationDetail());
		}

		return result;
	}

}
