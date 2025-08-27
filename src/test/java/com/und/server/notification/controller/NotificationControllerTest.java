package com.und.server.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.und.server.notification.dto.response.ScenarioNotificationListResponse;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.exception.NotificationCacheErrorResult;
import com.und.server.notification.exception.NotificationCacheException;
import com.und.server.notification.service.NotificationCacheService;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

	@InjectMocks
	private NotificationController notificationController;

	@Mock
	private NotificationCacheService notificationCacheService;

	private final Long memberId = 1L;
	private final Long scenarioId = 10L;


	@Test
	void Given_ValidRequest_When_GetScenarioNotifications_Then_ReturnNotificationList() {
		// given
		ScenarioNotificationListResponse expectedResponse = ScenarioNotificationListResponse.from(
			"1234567890",
			List.of(
				ScenarioNotificationResponse.builder()
					.scenarioId(1L)
					.scenarioName("아침 루틴")
					.memo("아침에 할 일들")
					.notificationCondition(null)
					.build(),
				ScenarioNotificationResponse.builder()
					.scenarioId(2L)
					.scenarioName("저녁 루틴")
					.memo("저녁에 할 일들")
					.notificationCondition(null)
					.build()
			)
		);

		given(notificationCacheService.getScenariosNotificationCache(memberId))
			.willReturn(expectedResponse);

		// when
		ResponseEntity<ScenarioNotificationListResponse> response =
			notificationController.getScenarioNotifications(memberId, null);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expectedResponse);
		assertThat(response.getHeaders().getFirst("ETag")).isEqualTo("1234567890");
		verify(notificationCacheService).getScenariosNotificationCache(memberId);
	}


	@Test
	void Given_ValidEtag_When_GetScenarioNotifications_Then_Return304NotModified() {
		// given
		String ifNoneMatch = "1234567890";
		ScenarioNotificationListResponse expectedResponse = ScenarioNotificationListResponse.from(
			"1234567890",
			List.of()
		);

		given(notificationCacheService.getScenariosNotificationCache(memberId))
			.willReturn(expectedResponse);

		// when
		ResponseEntity<ScenarioNotificationListResponse> response =
			notificationController.getScenarioNotifications(memberId, ifNoneMatch);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
		assertThat(response.getBody()).isNull();
		verify(notificationCacheService).getScenariosNotificationCache(memberId);
	}


	@Test
	void Given_DifferentEtag_When_GetScenarioNotifications_Then_Return200WithData() {
		// given
		String ifNoneMatch = "old-etag";
		ScenarioNotificationListResponse expectedResponse = ScenarioNotificationListResponse.from(
			"new-etag",
			List.of(
				ScenarioNotificationResponse.builder()
					.scenarioId(1L)
					.scenarioName("업데이트된 루틴")
					.memo("새로운 메모")
					.notificationCondition(null)
					.build()
			)
		);

		given(notificationCacheService.getScenariosNotificationCache(memberId))
			.willReturn(expectedResponse);

		// when
		ResponseEntity<ScenarioNotificationListResponse> response =
			notificationController.getScenarioNotifications(memberId, ifNoneMatch);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expectedResponse);
		assertThat(response.getHeaders().getFirst("ETag")).isEqualTo("new-etag");
		verify(notificationCacheService).getScenariosNotificationCache(memberId);
	}


	@Test
	void Given_EmptyNotificationList_When_GetScenarioNotifications_Then_ReturnEmptyList() {
		// given
		ScenarioNotificationListResponse expectedResponse = ScenarioNotificationListResponse.from(
			"1234567890",
			List.of()
		);

		given(notificationCacheService.getScenariosNotificationCache(memberId))
			.willReturn(expectedResponse);

		// when
		ResponseEntity<ScenarioNotificationListResponse> response =
			notificationController.getScenarioNotifications(memberId, null);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().scenarios()).isEmpty();
		assertThat(response.getHeaders().getFirst("ETag")).isEqualTo("1234567890");
		verify(notificationCacheService).getScenariosNotificationCache(memberId);
	}


	@Test
	void Given_ValidRequest_When_GetSingleScenarioNotification_Then_ReturnNotification() {
		// given
		ScenarioNotificationResponse expectedResponse = ScenarioNotificationResponse.builder()
			.scenarioId(scenarioId)
			.scenarioName("아침 루틴")
			.memo("아침에 할 일들")
			.notificationCondition(null)
			.build();

		given(notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId))
			.willReturn(expectedResponse);

		// when
		ResponseEntity<ScenarioNotificationResponse> response =
			notificationController.getSingleScenarioNotification(memberId, scenarioId);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expectedResponse);
		verify(notificationCacheService).getSingleScenarioNotificationCache(memberId, scenarioId);
	}


	@Test
	void Given_NonExistentScenario_When_GetSingleScenarioNotification_Then_ThrowNotFoundException() {
		// given
		given(notificationCacheService.getSingleScenarioNotificationCache(memberId, scenarioId))
			.willThrow(
				new NotificationCacheException(NotificationCacheErrorResult.CACHE_NOT_FOUND_SCENARIO_NOTIFICATION));

		// when & then
		assertThatThrownBy(() ->
			notificationController.getSingleScenarioNotification(memberId, scenarioId)
		).isInstanceOf(NotificationCacheException.class)
			.hasFieldOrPropertyWithValue("errorResult",
				NotificationCacheErrorResult.CACHE_NOT_FOUND_SCENARIO_NOTIFICATION);

		verify(notificationCacheService).getSingleScenarioNotificationCache(memberId, scenarioId);
	}


	@Test
	void Given_DifferentScenarioId_When_GetSingleScenarioNotification_Then_ReturnCorrectNotification() {
		// given
		Long differentScenarioId = 20L;
		ScenarioNotificationResponse expectedResponse = ScenarioNotificationResponse.builder()
			.scenarioId(differentScenarioId)
			.scenarioName("다른 루틴")
			.memo("다른 메모")
			.notificationCondition(null)
			.build();

		given(notificationCacheService.getSingleScenarioNotificationCache(memberId, differentScenarioId))
			.willReturn(expectedResponse);

		// when
		ResponseEntity<ScenarioNotificationResponse> response =
			notificationController.getSingleScenarioNotification(memberId, differentScenarioId);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().scenarioId()).isEqualTo(differentScenarioId);
		assertThat(response.getBody().scenarioName()).isEqualTo("다른 루틴");
		verify(notificationCacheService).getSingleScenarioNotificationCache(memberId, differentScenarioId);
	}

}
