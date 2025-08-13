package com.und.server.notification.entity;

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
@Table
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

	public boolean isActive() {
		return isActive;
	}

	public void updateActiveStatus(final Boolean isActive) {
		this.isActive = isActive;
	}

	public void updateNotification(
		final NotificationType notificationType,
		final NotificationMethodType notificationMethodType
	) {
		this.notificationType = notificationType;
		this.notificationMethodType = notificationMethodType;
	}

	public void deleteNotificationMethodType() {
		this.notificationMethodType = null;
	}

}
