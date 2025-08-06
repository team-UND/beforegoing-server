package com.und.server.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.und.server.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
