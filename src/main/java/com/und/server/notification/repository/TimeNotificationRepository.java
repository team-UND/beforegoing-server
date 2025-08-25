package com.und.server.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.und.server.notification.entity.TimeNotification;

import jakarta.validation.constraints.NotNull;

public interface TimeNotificationRepository extends JpaRepository<TimeNotification, Long> {

	@NotNull
	TimeNotification findByNotificationId(@NotNull Long notificationId);

	@Modifying
	void deleteByNotificationId(@NotNull Long notificationId);

}
