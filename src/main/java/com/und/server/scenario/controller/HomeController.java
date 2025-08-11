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
import com.und.server.scenario.dto.response.MissionGroupResponse;
import com.und.server.scenario.dto.response.ScenarioResponse;
import com.und.server.scenario.service.MissionService;
import com.und.server.scenario.service.ScenarioService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1")
@RestController
public class HomeController {

	private final ScenarioService scenarioService;
	private final MissionService missionService;

	@GetMapping("/home")
	public ResponseEntity<HomeResponse> getHomeData(
		@AuthMember Long memberId,
		@RequestParam(defaultValue = "TIME") NotificationType notifType
	) {
		List<ScenarioResponse> scenarios = scenarioService.findScenariosByMemberId(memberId, notifType);

		LocalDate today = LocalDate.now();
		MissionGroupResponse missions = null;
		if (!scenarios.isEmpty()) {
			Long firstScenarioId = scenarios.get(0).getScenarioId();
			missions = missionService.findMissionsByScenarioId(memberId, firstScenarioId, today);
		}

		HomeResponse result = HomeResponse.builder()
			.scenarios(scenarios)
			.missions(missions)
			.build();

		return ResponseEntity.ok().body(result);
	}

}
