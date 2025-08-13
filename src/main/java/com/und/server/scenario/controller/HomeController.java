package com.und.server.scenario.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.dto.response.HomeResponse;
import com.und.server.scenario.dto.response.HomeScenarioResponse;
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.service.MissionService;
import com.und.server.scenario.service.ScenarioService;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
public class HomeController {

	private final ScenarioService scenarioService;
	private final MissionService missionService;

	@GetMapping("/home")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Get home data successful"),
		@ApiResponse(responseCode = "400", description = "Invalid parameter")
	})
	public ResponseEntity<HomeResponse> getHomeData(
		@AuthMember Long memberId,
		@RequestParam(defaultValue = "TIME") NotificationType notificationType
	) {
		List<HomeScenarioResponse> scenarios =
			scenarioService.findHomeScenariosByMemberId(memberId, notificationType);

		LocalDate today = LocalDate.now();
		MissionGroupResponse missions = null;
		if (!scenarios.isEmpty()) {
			Long firstScenarioId = scenarios.get(0).scenarioId();
			missions = missionService.findMissionsByScenarioId(memberId, firstScenarioId, today);
		}

		HomeResponse homeResponse = HomeResponse.builder()
			.scenarios(scenarios)
			.missionListByType(missions)
			.build();

		return ResponseEntity.ok().body(homeResponse);
	}

}
