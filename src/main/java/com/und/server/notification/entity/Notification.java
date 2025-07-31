package com.und.server.notification.entity;

import com.und.server.scenario.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder

@Table
@Entity
public class Notification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Boolean isActive;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotifType notifType;

}
