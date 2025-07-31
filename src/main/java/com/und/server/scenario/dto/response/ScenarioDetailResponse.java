package com.und.server.scenario.dto.response;

import com.und.server.notification.dto.NotificationResponse;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class ScenarioDetailResponse {

	private Long scenarioId;
	private String scenarioName;
	private String memo;
	private List<MissionResponse> missionList;
	private List<NotificationResponse> notificationList;

}
