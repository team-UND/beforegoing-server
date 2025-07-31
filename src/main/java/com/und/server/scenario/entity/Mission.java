package com.und.server.scenario.entity;

import com.und.server.scenario.constants.MissionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder

@Table
@Entity
public class Mission {

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

	@Column(nullable = false)
	private Integer order;

	@Column
	private LocalDate useDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MissionType missionType;

}
