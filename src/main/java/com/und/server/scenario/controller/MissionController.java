package com.und.server.scenario.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.scenario.dto.request.TodayMissionRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.service.MissionService;
import com.und.server.scenario.service.ScenarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
@RestController
public class MissionController {

	private final ScenarioService scenarioService;
	private final MissionService missionService;


	@GetMapping("/scenarios/{scenarioId}/missions")
	public ResponseEntity<MissionGroupResponse> getMissionsByScenarioId(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId,
		@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
	) {
		MissionGroupResponse missionList =
			scenarioService.findMissionsGroupByMissionType(memberId, scenarioId, date);

		return ResponseEntity.ok().body(missionList);
	}


	@PostMapping("/scenarios/{scenarioId}/missions/today")
	public ResponseEntity<Void> addTodayMissionToScenario(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId,
		@RequestBody @Valid TodayMissionRequest missionAddRequest,
		@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
	) {
		scenarioService.addTodayMissionToScenario(memberId, scenarioId, missionAddRequest, date);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}


	@PatchMapping("/missions/{missionId}/check")
	public ResponseEntity<Void> updateMissionCheck(
		@AuthMember Long memberId,
		@PathVariable Long missionId,
		@RequestBody @NotNull Boolean isChecked
	) {
		missionService.updateMissionCheck(memberId, missionId, isChecked);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}


	@DeleteMapping("/missions/{missionId}")
	public ResponseEntity<Void> deleteTodayMissionById(
		@AuthMember Long memberId,
		@PathVariable Long missionId
	) {
		missionService.deleteTodayMission(memberId, missionId);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
