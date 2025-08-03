package com.und.server.scenario.dto;

import java.util.List;

import com.und.server.notification.dto.NofitDayOfWeekResponse;
import com.und.server.notification.dto.NotificationDetailResponse;


public record NotificationInfoDto(

	Boolean isEveryDay,
	List<NofitDayOfWeekResponse> dayOfWeekOrdinalList,
	NotificationDetailResponse notificationDetail

) {
}
