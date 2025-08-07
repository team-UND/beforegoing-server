package com.und.server.notification.dto;

import java.util.List;

import com.und.server.notification.dto.response.NotificationConditionResponse;


public record NotificationInfoDto(

	Boolean isEveryDay,
	List<Integer> dayOfWeekOrdinalList,
	NotificationConditionResponse notificationCondition

) {
}
