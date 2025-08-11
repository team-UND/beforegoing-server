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
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(
	name = "time_notification",
	indexes = {
		@Index(name = "idx_day_time_notif", columnList = "day_of_week, `hour`, `minute`")
	}
)
@Entity
public class TimeNotification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_id", nullable = false)
	private Notification notification;

	@Enumerated(EnumType.ORDINAL)
	@Column
	private DayOfWeek dayOfWeek;

	@Setter
	@Column(name = "`hour`")
	private Integer hour;

	@Setter
	@Column(name = "`minute`")
	private Integer minute;

}
