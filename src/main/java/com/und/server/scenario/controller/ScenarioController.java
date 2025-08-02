package com.und.server.scenario.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.service.ScenarioService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
@RestController
public class ScenarioController {

	private final ScenarioService scenarioService;

	@GetMapping("/scenarios")
	public ResponseEntity<List<ScenarioResponse>> getScenarios(@AuthMember Long memberId) {
		List<ScenarioResponse> scenarioList = scenarioService.findScenariosByMemberId(memberId);

		return ResponseEntity.ok().body(scenarioList);
	}

	/// 디테일
	@GetMapping("/scenarios/{scenarioId}")
	public ResponseEntity<ScenarioDetailResponse> getScenarioDetail(@AuthMember Long memberId,
																	@PathVariable Long scenarioId
	) {
		ScenarioDetailResponse scenarioDetail = scenarioService.findScenarioByScenarioId(memberId, scenarioId);

		return ResponseEntity.ok().body(scenarioDetail);
	}

}
