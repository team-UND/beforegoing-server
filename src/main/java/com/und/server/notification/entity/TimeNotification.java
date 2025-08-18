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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(
	name = "time_notification",
	indexes = {
		@Index(name = "idx_day_time_notification", columnList = "day_of_week, startHour, startMinute")
	}
)
public class TimeNotification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_id", nullable = false)
	private Notification notification;

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = false)
	private DayOfWeek dayOfWeek;

	@Column(nullable = false)
	@Min(0)
	@Max(23)
	private Integer startHour;

	@Column(nullable = false)
	@Min(0)
	@Max(59)
	private Integer startMinute;

	public void updateTimeCondition(final Integer hour, final Integer minute) {
		this.startHour = hour;
		this.startMinute = minute;
	}

}
