package com.und.server.scenario.dto.response;

import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.entity.Mission;
import lombok.*;

import java.util.List;

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


	public static MissionResponse of(Mission mission) {
		return MissionResponse.builder()
			.missionId(mission.getId())
			.content(mission.getContent())
			.isChecked(mission.getIsChecked())
			.order(mission.getOrder())
			.missionType(mission.getMissionType())
			.build();
	}

	public static List<MissionResponse> listOf(List<Mission> missionList) {
		return missionList.stream()
			.map(MissionResponse::of)
			.toList();
	}

}
