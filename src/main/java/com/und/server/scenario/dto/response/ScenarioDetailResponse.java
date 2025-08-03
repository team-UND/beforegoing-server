package com.und.server.scenario.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.und.server.notification.constants.NotifMethodType;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.NofitDayOfWeekResponse;
import com.und.server.notification.dto.NotificationDetailResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.scenario.dto.NotificationInfoDto;
import com.und.server.scenario.entity.Mission;
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
	private List<MissionResponse> basicMissionList;

	private Long notificationId;
	private Boolean isActive;
	private NotifType notificationType;
	private NotifMethodType notificationMethodType;
	private Boolean isEveryDay;
	private List<NofitDayOfWeekResponse> dayOfWeekOrdinalList;
	private NotificationDetailResponse notificationDetail;


	public static ScenarioDetailResponse of(Scenario scenario,
											List<Mission> basicMissionList,
											NotificationInfoDto notifInfo
	) {
		Notification notification = scenario.getNotification();

		ScenarioDetailResponse result = ScenarioDetailResponse.builder()
			.scenarioId(scenario.getId())
			.scenarioName(scenario.getScenarioName())
			.memo(scenario.getMemo())
			.basicMissionList(MissionResponse.listOf(basicMissionList))
			.notificationId(notification.getId())
			.isActive(notification.isActive())
			.notificationType(notification.getNotifType())
			.notificationMethodType(notification.getNotifMethodType())
			.build();

		if (notifInfo != null) {
			result.setIsEveryDay(notifInfo.isEveryDay());
			result.setDayOfWeekOrdinalList(notifInfo.dayOfWeekOrdinalList());
			result.setNotificationDetail(notifInfo.notificationDetail());
		}

		return result;
	}

}
