package com.und.server.notification.dto;

import java.util.List;

import com.und.server.notification.dto.response.NotificationConditionResponse;
import com.und.server.notification.dto.response.NotificationDayOfWeekResponse;


public record NotificationInfoDto(

	Boolean isEveryDay,
	List<NotificationDayOfWeekResponse> dayOfWeekOrdinalList,
	NotificationConditionResponse notificationCondition

) {
}
