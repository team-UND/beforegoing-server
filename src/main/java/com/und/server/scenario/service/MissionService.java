package com.und.server.scenario.service;

import com.und.server.member.entity.Member;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MissionService {

	private final MissionRepository missionRepository;

	@Transactional(readOnly = true)
	public List<MissionResponse> findMissionsByScenarioId(Long memberId, Long scenarioId) {
		List<Mission> missionList = missionRepository.findAllByScenarioIdOrderByOrder(scenarioId);

		for (Mission mission : missionList) {
			Member member = mission.getScenario().getMember();
			if (!memberId.equals(member.getId())) {
				break; //에외처리
			}
		}

		return MissionResponse.listOf(missionList);
	}
}
