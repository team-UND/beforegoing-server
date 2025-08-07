package com.und.server.notification.entity;

import java.time.DayOfWeek;

import com.und.server.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder

@Table(
	name = "location_notification",
	indexes = {
		@Index(name = "idx_day_location_notif", columnList = "day_of_week, start_hour, start_minute")
	}
)
@Entity
public class LocationNotification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_id", nullable = false)
	private Notification notification;

	@Enumerated(EnumType.ORDINAL)
	@Column
	private DayOfWeek dayOfWeek;

	@Column
	private Double latitude;

	@Column
	private Double longitude;

	@Column
	private Integer trackingRadiusKm;

	@Column
	private Integer startHour;

	@Column
	private Integer startMinute;

	@Column
	private Integer endHour;

	@Column
	private Integer endMinute;

}
