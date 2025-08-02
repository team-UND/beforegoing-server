package com.und.server.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.und.server.notification.entity.TimeNotif;

public interface TimeNotifRepository extends JpaRepository<TimeNotif, Long> {

	List<TimeNotif> findByNotificationId(Long notificationId);

}
