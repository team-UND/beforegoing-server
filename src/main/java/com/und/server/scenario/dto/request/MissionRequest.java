package com.und.server.scenario.dto.request;

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.entity.Scenario;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class MissionRequest {

	private Long missionId;

	@NotBlank(message = "Content must not be blank")
	@Size(max = 10, message = "Content must be at most 10 characters")
	private String content;

	@NotNull(message = "MissionType must not be null")
	@Builder.Default
	private MissionType missionType = MissionType.BASIC;


	public Mission toEntity(Scenario scenario, Integer order) {
		return Mission.builder()
			.scenario(scenario)
			.content(content)
			.isChecked(false)
			.order(order)
			.missionType(missionType)
			.build();
	}

}
