package com.und.server.scenario.entity;

import java.time.LocalDate;

import com.und.server.common.entity.BaseTimeEntity;
import com.und.server.scenario.constants.MissionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "mission")
public class Mission extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "scenario_id", nullable = false)
	private Scenario scenario;

	@Column(nullable = false, length = 10)
	private String content;

	@Column(nullable = false)
	private Boolean isChecked;

	@Column
	@Min(0)
	@Max(10_000_000)
	private Integer missionOrder;

	@Column
	private Long parentMissionId;

	@Column
	private LocalDate useDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MissionType missionType;

	public void updateCheckStatus(final Boolean checked) {
		this.isChecked = checked;
	}

	public void updateMissionOrder(final Integer missionOrder) {
		this.missionOrder = missionOrder;
	}

	public Mission createFutureChildMission(final boolean isChecked, LocalDate future) {
		return Mission.builder()
			.scenario(this.scenario)
			.content(this.content)
			.isChecked(isChecked)
			.parentMissionId(this.id)
			.useDate(future)
			.missionType(this.missionType)
			.build();
	}

}
