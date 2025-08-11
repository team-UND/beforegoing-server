package com.und.server.notification.entity;

import java.time.DayOfWeek;

import com.und.server.common.entity.BaseTimeEntity;
import com.und.server.notification.constants.LocationTrackingRadiusType;

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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
		@Index(name = "idx_day_location_notif", columnList = "day_of_week, `start_hour`, `start_minute`")
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
	@Column(nullable = false)
	private DayOfWeek dayOfWeek;

	@Column(nullable = false, precision = 9, scale = 6)
	@DecimalMin(value = "-90.0")
	@DecimalMax(value = "90.0")
	@Digits(integer = 3, fraction = 6)
	private Double latitude;

	@Column(nullable = false, precision = 9, scale = 6)
	@DecimalMin(value = "-180.0")
	@DecimalMax(value = "180.0")
	@Digits(integer = 3, fraction = 6)
	private Double longitude;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LocationTrackingRadiusType trackingRadiusType;

	@Column(name = "`start_hour`", nullable = false)
	@Min(0)
	@Max(23)
	private Integer startHour;

	@Column(name = "`start_minute`", nullable = false)
	@Min(0)
	@Max(59)
	private Integer startMinute;

	@Column(name = "`end_hour`", nullable = false)
	@Min(0)
	@Max(23)
	private Integer endHour;

	@Column(name = "`end_minute`", nullable = false)
	@Min(0)
	@Max(59)
	private Integer endMinute;

}
