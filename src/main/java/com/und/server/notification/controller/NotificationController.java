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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Notification", description = "알림 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationCacheService notificationCacheService;

	@Operation(summary = "시나리오 알림 목록 조회", description = "사용자의 시나리오별 알림 목록을 조회합니다.")
	@GetMapping("/scenarios")
	public ResponseEntity<ScenarioNotificationListResponse> getScenarioNotifications(
		@AuthMember Long memberId,
		@Parameter(description = "클라이언트 캐싱용 ETag")
		@RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
	) {
		ScenarioNotificationListResponse response =
			notificationCacheService.getScenariosNotificationCache(memberId);

		if (ifNoneMatch != null && ifNoneMatch.equals(response.etag())) {
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
		}

		return ResponseEntity.ok()
			.header("ETag", response.etag())
			.body(response);
	}

	@Operation(summary = "특정 시나리오 알림 조회", description = "특정 시나리오의 알림 데이터만 조회합니다. 시나리오가 존재하지 않으면 null을 반환합니다.")
	@GetMapping("/scenarios/{scenarioId}")
	public ResponseEntity<ScenarioNotificationResponse> getSingleScenarioNotification(
		@AuthMember Long memberId,
		@PathVariable Long scenarioId
	) {
		ScenarioNotificationResponse response =
			notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId);

		if (response == null) {
			return ResponseEntity.ok().body(null);
		}
		return ResponseEntity.ok().body(response);
	}

}
