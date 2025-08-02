package com.und.server.scenario.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.service.MissionService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
@RestController
public class MissionController {

	private final MissionService missionService;

	@GetMapping("/scenarios/{scenarioId}/missions")
	public ResponseEntity<List<MissionResponse>> getMissionsByScenarioId(
		@AuthMember Long memberId, @PathVariable Long scenarioId) {
		List<MissionResponse> missionList = missionService.findMissionsByScenarioId(memberId, scenarioId);

		return ResponseEntity.ok().body(missionList);
	}

}
