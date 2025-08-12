package com.und.server.scenario.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.dto.request.ScenarioDetailRequest;
import com.und.server.scenario.dto.request.ScenarioNoNotificationRequest;
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.service.ScenarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
public class ScenarioController {

	private final ScenarioService scenarioService;


	@GetMapping("/scenarios")
	public ResponseEntity<List<ScenarioResponse>> getScenarios(
		@AuthMember Long memberId,
		@RequestParam NotificationType notifType
	) {
		List<ScenarioResponse> scenarioList = scenarioService.findScenariosByMemberId(memberId, notifType);

		return ResponseEntity.ok().body(scenarioList);
	}


	@GetMapping("/scenarios/{scenarioId}")
	public ResponseEntity<ScenarioDetailResponse> getScenarioDetail(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId
	) {
		ScenarioDetailResponse scenarioDetail =
			scenarioService.findScenarioDetailByScenarioId(memberId, scenarioId);

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


	@PostMapping("/scenarios/without-notification")
	public ResponseEntity<Void> addScenarioWithoutNotification(
		@AuthMember Long memberId,
		@RequestBody @Valid ScenarioNoNotificationRequest scenarioNoNotificationResponse
	) {
		scenarioService.addScenarioWithoutNotification(memberId, scenarioNoNotificationResponse);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}


	@PutMapping("/scenarios/{scenarioId}")
	public ResponseEntity<Void> updateScenario(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId,
		@RequestBody @Valid ScenarioDetailRequest scenarioRequest
	) {
		scenarioService.updateScenario(memberId, scenarioId, scenarioRequest);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}


	@PutMapping("/scenarios/{scenarioId}/without-notification")
	public ResponseEntity<Void> updateScenarioWithoutNotification(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId,
		@RequestBody @Valid ScenarioNoNotificationRequest scenarioNoNotificationRequest
	) {
		scenarioService.updateScenarioWithoutNotification(memberId, scenarioId, scenarioNoNotificationRequest);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}


	@PatchMapping("/scenarios/{scenarioId}/order")
	public ResponseEntity<Void> updateScenarioOrder(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId,
		@RequestBody @Valid ScenarioOrderUpdateRequest scenarioOrderUpdateRequest
	) {
		scenarioService.updateScenarioOrder(memberId, scenarioId, scenarioOrderUpdateRequest);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}


	@DeleteMapping("/scenarios/{scenarioId}")
	public ResponseEntity<Void> deleteScenario(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId
	) {
		scenarioService.deleteScenarioWithAllMissions(memberId, scenarioId);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
