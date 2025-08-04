package com.und.server.scenario.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.scenario.dto.requeset.MissionAddRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.service.MissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
@RestController
public class MissionController {

	private final MissionService missionService;


	@GetMapping("/scenarios/{scenarioId}/missions")
	public ResponseEntity<MissionGroupResponse> getMissionsByScenarioId(
		@AuthMember Long memberId, @PathVariable Long scenarioId) {
		MissionGroupResponse missionList = missionService.findMissionsByScenarioId(memberId, scenarioId);

		return ResponseEntity.ok().body(missionList);
	}


	@PostMapping("/scenarios/{scenarioId}/missions")
	public ResponseEntity<Void> addTodayMissionToScenario(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId,
		@RequestBody @Valid MissionAddRequest missionAddRequest
	) {
		missionService.addMissionToScenario(memberId, scenarioId, missionAddRequest);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
