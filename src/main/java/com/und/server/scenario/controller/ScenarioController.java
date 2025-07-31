package com.und.server.scenario.controller;

import com.und.server.auth.filter.AuthMember;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.service.ScenarioService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
