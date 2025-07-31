package com.und.server.scenario.service;

import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScenarioService {

	private final ScenarioRepository scenarioRepository;

	@Transactional(readOnly = true)
	public List<ScenarioResponse> findScenariosByMemberId(Long memberId) {
		List<Scenario> scenarioList = scenarioRepository.findByMemberIdOrderByOrder(memberId);

		return ScenarioResponse.listOf(scenarioList);
	}

}
