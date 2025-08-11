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
public class Mission extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "scenario_id", nullable = false)
	private Scenario scenario;

	@Column(nullable = false, length = 10)
	private String content;

	@Setter
	@Column(nullable = false)
	private Boolean isChecked;

	@Setter
	@Column(name = "`order`")
	private Integer order;

	@Column
	private LocalDate useDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MissionType missionType;

}
