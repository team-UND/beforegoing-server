package com.und.server.notification.dto;

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
public class LocationNotifResponse extends NotificationDetailResponse {

	private Double latitude;
	private Double longitude;
	private Integer trackingRadiusKm;
	private Integer startHour;
	private Integer startMinute;
	private Integer endHour;
	private Integer endMinute;

}
