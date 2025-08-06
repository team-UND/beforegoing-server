package com.und.server.notification.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class NotificationDayOfWeekRequest {

	private Long notificationDayOfWeekId;

	@NotNull(message = "DayOfWeek must not be null")
	@Min(value = 0, message = "DayOfWeek must be between 0 and 6")
	@Max(value = 6, message = "DayOfWeek must be between 0 and 6")
	private Integer dayOfWeekOrdinal;

}
