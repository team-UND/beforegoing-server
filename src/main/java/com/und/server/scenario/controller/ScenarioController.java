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
import com.und.server.scenario.dto.request.ScenarioOrderUpdateRequest;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.dto.response.OrderUpdateResponse;
import com.und.server.scenario.dto.response.ScenarioDetailResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.service.ScenarioService;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
public class ScenarioController {

	private final ScenarioService scenarioService;


	@GetMapping("/scenarios")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Get scenarios successful"),
		@ApiResponse(responseCode = "400", description = "Invalid parameter")
	})
	public ResponseEntity<List<ScenarioResponse>> getScenarios(
		@AuthMember final Long memberId,
		@RequestParam(defaultValue = "TIME") final NotificationType notificationType
	) {
		final List<ScenarioResponse> scenarios =
			scenarioService.findScenariosByMemberId(memberId, notificationType);

		return ResponseEntity.ok().body(scenarios);
	}


	@GetMapping("/scenarios/{scenarioId}")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Get scenario detail successful"),
		@ApiResponse(responseCode = "400", description = "Invalid parameter"),
		@ApiResponse(responseCode = "404", description = "Scenario not found")
	})
	public ResponseEntity<ScenarioDetailResponse> getScenarioDetail(
		@AuthMember final Long memberId,
		@PathVariable final Long scenarioId
	) {
		final ScenarioDetailResponse scenarioDetail =
			scenarioService.findScenarioDetailByScenarioId(memberId, scenarioId);

		return ResponseEntity.ok().body(scenarioDetail);
	}


	@PostMapping("/scenarios")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Create Scenario successful"),
		@ApiResponse(responseCode = "400", description = "Invalid parameter")
	})
	public ResponseEntity<MissionGroupResponse> addScenario(
		@AuthMember final Long memberId,
		@RequestBody @Valid final ScenarioDetailRequest scenarioRequest
	) {
		final MissionGroupResponse missionGroupResponse =
			scenarioService.addScenario(memberId, scenarioRequest);

		return ResponseEntity.status(HttpStatus.CREATED).body(missionGroupResponse);
	}


	@PutMapping("/scenarios/{scenarioId}")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "Update Scenario successful"),
		@ApiResponse(responseCode = "400", description = "Invalid parameter"),
		@ApiResponse(responseCode = "404", description = "Scenario not found")
	})
	public ResponseEntity<MissionGroupResponse> updateScenario(
		@AuthMember final Long memberId,
		@PathVariable final Long scenarioId,
		@RequestBody @Valid final ScenarioDetailRequest scenarioRequest
	) {
		MissionGroupResponse missionGroupResponse =
			scenarioService.updateScenario(memberId, scenarioId, scenarioRequest);

		return ResponseEntity.ok().body(missionGroupResponse);
	}


	@PatchMapping("/scenarios/{scenarioId}/order")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Update Scenario order successful"),
		@ApiResponse(responseCode = "400", description = "Invalid parameter"),
		@ApiResponse(responseCode = "404", description = "Scenario not found")
	})
	public ResponseEntity<OrderUpdateResponse> updateScenarioOrder(
		@AuthMember final Long memberId,
		@PathVariable final Long scenarioId,
		@RequestBody @Valid final ScenarioOrderUpdateRequest scenarioOrderUpdateRequest
	) {
		final OrderUpdateResponse orderUpdateResponse =
			scenarioService.updateScenarioOrder(memberId, scenarioId, scenarioOrderUpdateRequest);

		return ResponseEntity.ok().body(orderUpdateResponse);
	}


	@DeleteMapping("/scenarios/{scenarioId}")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "Delete Scenario successful"),
		@ApiResponse(responseCode = "404", description = "Scenario not found")
	})
	public ResponseEntity<Void> deleteScenario(
		@AuthMember final Long memberId,
		@PathVariable final Long scenarioId
	) {
		scenarioService.deleteScenarioWithAllMissions(memberId, scenarioId);

		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
