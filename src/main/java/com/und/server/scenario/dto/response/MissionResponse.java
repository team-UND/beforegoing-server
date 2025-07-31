package com.und.server.scenario.dto.response;

import com.und.server.scenario.constants.MissionType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class MissionResponse {

	private Long missionId;
	private String content;
	private Boolean isChecked;
	private Integer order;
	private MissionType missionType;

}
