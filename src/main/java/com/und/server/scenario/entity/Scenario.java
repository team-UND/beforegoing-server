package com.und.server.scenario.entity;

import java.util.List;

import com.und.server.common.entity.BaseTimeEntity;
import com.und.server.member.entity.Member;
import com.und.server.notification.entity.Notification;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

	@Setter
	@Column(nullable = false, length = 10)
	private String scenarioName;

	@Setter
	@Column(length = 15)
	private String memo;

	@Setter
	@Column(name = "`order`", nullable = false)
	private Integer order;

	@Setter
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_id")
	private Notification notification;

	@OneToMany(mappedBy = "scenario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Mission> missionList;

}
