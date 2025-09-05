package com.und.server.scenario.entity;

import java.util.ArrayList;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Table(name = "scenario")
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
	@Min(0)
	@Max(10_000_000)
	private Integer scenarioOrder;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "notification_id", nullable = false, unique = true)
	private Notification notification;

	@OneToMany(mappedBy = "scenario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Mission> missions = new ArrayList<>();

	public void updateScenarioName(final String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public void updateMemo(final String memo) {
		this.memo = memo;
	}

	public void updateScenarioOrder(final Integer scenarioOrder) {
		this.scenarioOrder = scenarioOrder;
	}

	public boolean addMission(final Mission mission) {
		if (missions.size() >= 20) {
			return false;
		}
		missions.add(mission);
		return true;
	}

}
