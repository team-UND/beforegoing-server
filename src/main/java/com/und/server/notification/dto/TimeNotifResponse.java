package com.und.server.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeNotifResponse {

	private Long notificationId;
	private Boolean isEveryDay;
	private Integer dayOfWeekOrdinal;
	private Integer hour;
	private Integer minute;

}
