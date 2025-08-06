package com.und.server.scenario.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.notification.constants.NotifType;
import com.und.server.scenario.dto.requeset.ScenarioDetailRequest;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.service.ScenarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
@RestController
public class ScenarioController {

	private final ScenarioService scenarioService;


	@GetMapping("/scenarios") ///미션 타입이 있어야하지 않나?
	public ResponseEntity<List<ScenarioResponse>> getScenarios(
		@AuthMember Long memberId,
		@RequestParam NotifType notifType
	) {
		List<ScenarioResponse> scenarioList = scenarioService.findScenariosByMemberId(memberId, notifType);

		return ResponseEntity.ok().body(scenarioList);
	}


	@GetMapping("/scenarios/{scenarioId}")
	public ResponseEntity<ScenarioDetailResponse> getScenarioDetail(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId
	) {
		ScenarioDetailResponse scenarioDetail = scenarioService.findScenarioByScenarioId(memberId, scenarioId);

		return ResponseEntity.ok().body(scenarioDetail);
	}


	@PostMapping("/scenarios")
	public ResponseEntity<Void> addScenario(
		@AuthMember Long memberId,
		@RequestBody @Valid ScenarioDetailRequest scenarioRequest
	) {
		scenarioService.addScenario(memberId, scenarioRequest);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

//
//	@PutMapping("/scenarios/{scenarioId}")
//	public ResponseEntity<Void> updateScenario(
//		@AuthMember Long memberId,
//		@PathVariable Long scenarioId,
//		@RequestBody @Valid ScenarioDetailRequest scenarioRequest
//	) {
//		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//	}

	//시나리오 순서 변경
}
