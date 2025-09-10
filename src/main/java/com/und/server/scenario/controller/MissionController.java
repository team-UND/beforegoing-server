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
import com.und.server.scenario.dto.response.MissionResponse;
import com.und.server.scenario.service.MissionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
public class MissionController implements MissionApiDocs {

	private final MissionService missionService;


	@Override
	@GetMapping("/scenarios/{scenarioId}/missions")
	public ResponseEntity<MissionGroupResponse> getMissionsByScenarioId(
		@AuthMember final Long memberId,
		@PathVariable final Long scenarioId,
		@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate date
	) {
		final MissionGroupResponse missions =
			missionService.findMissionsByScenarioId(memberId, scenarioId, date);

		return ResponseEntity.ok().body(missions);
	}


	@Override
	@PostMapping("/scenarios/{scenarioId}/missions/today")
	public ResponseEntity<MissionResponse> addTodayMissionToScenario(
		@AuthMember final Long memberId,
		@PathVariable final Long scenarioId,
		@RequestBody @Valid final TodayMissionRequest missionAddRequest,
		@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate date
	) {
		final MissionResponse missionResponse =
			missionService.addTodayMission(memberId, scenarioId, missionAddRequest, date);

		return ResponseEntity.status(HttpStatus.CREATED).body(missionResponse);
	}


	@Override
	@PatchMapping("/missions/{missionId}/check")
	public ResponseEntity<Void> updateMissionCheck(
		@AuthMember final Long memberId,
		@PathVariable final Long missionId,
		@RequestBody @NotNull final Boolean isChecked,
		@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate date
	) {
		missionService.updateMissionCheck(memberId, missionId, isChecked, date);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}


	@Override
	@DeleteMapping("/missions/{missionId}")
	public ResponseEntity<Void> deleteTodayMissionById(
		@AuthMember final Long memberId,
		@PathVariable final Long missionId
	) {
		missionService.deleteTodayMission(memberId, missionId);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
