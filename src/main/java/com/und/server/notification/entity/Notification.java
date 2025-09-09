package com.und.server.notification.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.und.server.common.entity.BaseTimeEntity;
import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Boolean isActive;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType notificationType;

	@Enumerated(EnumType.STRING)
	private NotificationMethodType notificationMethodType;

	@Column
	private String daysOfWeek;

	public boolean isActive() {
		return isActive;
	}

	public boolean isEveryDay() {
		List<Integer> days = getDaysOfWeekOrdinalList();
		return days.size() == 7;
	}

	public void activate(
		final NotificationType notificationType,
		final NotificationMethodType notificationMethodType,
		final List<Integer> daysOfWeek
	) {
		this.isActive = true;
		this.notificationType = notificationType;
		this.notificationMethodType = notificationMethodType;
		updateDaysOfWeekOrdinal(daysOfWeek);
	}

	public void deactivate() {
		this.isActive = false;
		this.notificationMethodType = null;
		this.daysOfWeek = null;
	}

	public List<Integer> getDaysOfWeekOrdinalList() {
		if (daysOfWeek == null || daysOfWeek.isEmpty()) {
			return List.of();
		}
		return Arrays.stream(daysOfWeek.split(","))
			.map(String::trim)
			.map(Integer::parseInt)
			.collect(Collectors.toList());
	}

	public void updateDaysOfWeekOrdinal(final List<Integer> daysOfWeekOrdinal) {
		if (!isActive || daysOfWeekOrdinal == null || daysOfWeekOrdinal.isEmpty()) {
			this.daysOfWeek = null;
			return;
		}
		this.daysOfWeek = daysOfWeekOrdinal.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
	}

}
