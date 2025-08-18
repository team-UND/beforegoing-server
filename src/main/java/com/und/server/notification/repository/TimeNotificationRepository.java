package com.und.server.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.und.server.notification.entity.TimeNotification;

import jakarta.validation.constraints.NotNull;

public interface TimeNotificationRepository extends JpaRepository<TimeNotification, Long> {

	@NotNull
	List<TimeNotification> findByNotificationId(@NotNull Long notificationId);

}
