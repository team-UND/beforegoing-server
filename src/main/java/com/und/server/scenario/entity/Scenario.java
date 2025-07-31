package com.und.server.scenario.entity;


import com.und.server.member.entity.Member;
import com.und.server.notification.entity.Notification;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder

@Table
@Entity
public class Scenario extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, length = 10)
	private String scenarioName;

	@Column(length = 15)
	private String memo;

	@Column(nullable = false)
	private Integer order;

	@OneToOne
	@JoinColumn(name = "notification_id")
	private Notification notification;

	@OneToMany(mappedBy = "scenario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Mission> missionList;

}
