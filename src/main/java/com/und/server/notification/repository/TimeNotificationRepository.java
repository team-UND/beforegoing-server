package com.und.server.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.und.server.notification.entity.TimeNotification;

public interface TimeNotificationRepository extends JpaRepository<TimeNotification, Long> {

	List<TimeNotification> findByNotificationId(Long notificationId);

}
