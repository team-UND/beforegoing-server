package com.und.server.notification.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.und.server.auth.filter.AuthMember;
import com.und.server.notification.dto.response.ScenarioNotificationListResponse;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.service.NotificationCacheService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class NotificationController implements NotificationApiDocs {

	private final NotificationCacheService notificationCacheService;


	@Override
	@GetMapping("/scenarios/notifications")
	public ResponseEntity<ScenarioNotificationListResponse> getScenarioNotifications(
		@AuthMember final Long memberId,
		@Parameter(description = "ETag for client caching")
		@RequestHeader(value = "If-None-Match", required = false) final String ifNoneMatch
	) {
		final ScenarioNotificationListResponse scenarioNotificationListResponse =
			notificationCacheService.getScenariosNotificationCache(memberId);

		if (ifNoneMatch != null && ifNoneMatch.equals(scenarioNotificationListResponse.etag())) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}

		return ResponseEntity.ok()
			.header("ETag", scenarioNotificationListResponse.etag())
			.body(scenarioNotificationListResponse);
	}


	@Override
	@GetMapping("/scenarios/{scenarioId}/notifications")
	public ResponseEntity<ScenarioNotificationResponse> getSingleScenarioNotification(
		@AuthMember final Long memberId,
		@PathVariable final Long scenarioId
	) {
		final ScenarioNotificationResponse scenarioNotificationResponse =
			notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId);

		return ResponseEntity.ok().body(scenarioNotificationResponse);
	}

}
