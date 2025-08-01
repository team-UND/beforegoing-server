package com.und.server.notification.repository;

import com.und.server.notification.entity.TimeNotif;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeNotifRepository extends JpaRepository<TimeNotif, Long> {

	List<TimeNotif> findByNotificationId(Long notificationId);

}
