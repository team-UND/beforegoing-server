package com.und.server.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder

@Table
@Entity
public class TimeNotif {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_id", nullable = false)
	private Notification notification;

	@Enumerated(EnumType.ORDINAL)
	@Column
	private DayOfWeek dayOfWeek; //null일경우 매일

	@Column
	private Integer hour;

	@Column
	private Integer minute;

}
