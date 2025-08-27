package com.und.server.notification.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.entity.Notification;
import com.und.server.notification.service.NotificationCacheService;
import com.und.server.scenario.entity.Scenario;


@ExtendWith(MockitoExtension.class)
class ScenarioUpdateEventListenerTest {

	@InjectMocks
	private ScenarioUpdateEventListener scenarioUpdateEventListener;

	@Mock
	private NotificationCacheService notificationCacheService;

	private final Long memberId = 1L;
	private final Long scenarioId = 1L;


	@Test
	void Given_ValidScenarioWithActiveNotification_When_HandleUpdate_Then_UpdateCache() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("업데이트된 루틴")
			.memo("업데이트된 메모")
			.notification(notification)
			.build();

		ScenarioUpdateEvent event = new ScenarioUpdateEvent(memberId, scenario, false);

		// when
		scenarioUpdateEventListener.handleUpdate(event);

		// then
		verify(notificationCacheService, never()).deleteCache(anyLong(), anyLong());
		verify(notificationCacheService).updateCache(eq(memberId), eq(scenario));
	}


	@Test
	void Given_ValidScenarioWithInactiveNotificationAndOldNotificationWasActive_When_HandleUpdate_Then_DeleteCache() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("비활성화된 루틴")
			.memo("비활성화된 메모")
			.notification(notification)
			.build();

		ScenarioUpdateEvent event = new ScenarioUpdateEvent(memberId, scenario, true);

		// when
		scenarioUpdateEventListener.handleUpdate(event);

		// then
		verify(notificationCacheService).deleteCache(eq(memberId), eq(scenarioId));
		verify(notificationCacheService, never()).updateCache(anyLong(), any());
	}


	@Test
	void Given_ValidScenarioWithInactiveNotificationAndOldNotificationWasInactive_When_HandleUpdate_Then_DoNothing() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("비활성화된 루틴")
			.memo("비활성화된 메모")
			.notification(notification)
			.build();

		ScenarioUpdateEvent event = new ScenarioUpdateEvent(memberId, scenario, false);

		// when
		scenarioUpdateEventListener.handleUpdate(event);

		// then
		verify(notificationCacheService, never()).deleteCache(anyLong(), anyLong());
		verify(notificationCacheService, never()).updateCache(anyLong(), any());
	}


	@Test
	void Given_ValidScenarioWithNullNotificationAndOldNotificationWasActive_When_HandleUpdate_Then_DeleteCache() {
		// given
		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("알림 없는 루틴")
			.memo("알림 없는 메모")
			.notification(null)
			.build();

		ScenarioUpdateEvent event = new ScenarioUpdateEvent(memberId, scenario, true);

		// when
		scenarioUpdateEventListener.handleUpdate(event);

		// then
		verify(notificationCacheService).deleteCache(eq(memberId), eq(scenarioId));
		verify(notificationCacheService, never()).updateCache(anyLong(), any());
	}


	@Test
	void Given_ValidScenarioWithNullNotificationAndOldNotificationWasInactive_When_HandleUpdate_Then_DoNothing() {
		// given
		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("알림 없는 루틴")
			.memo("알림 없는 메모")
			.notification(null)
			.build();

		ScenarioUpdateEvent event = new ScenarioUpdateEvent(memberId, scenario, false);

		// when
		scenarioUpdateEventListener.handleUpdate(event);

		// then
		verify(notificationCacheService, never()).deleteCache(anyLong(), anyLong());
		verify(notificationCacheService, never()).updateCache(anyLong(), any());
	}


	@Test
	void Given_ExceptionOccurs_When_HandleUpdate_Then_DeleteMemberAllCache() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("예외 발생 루틴")
			.memo("예외 발생 메모")
			.notification(notification)
			.build();

		ScenarioUpdateEvent event = new ScenarioUpdateEvent(memberId, scenario, false);

		doThrow(new RuntimeException("Cache operation failed"))
			.when(notificationCacheService).updateCache(anyLong(), any());

		// when
		scenarioUpdateEventListener.handleUpdate(event);

		// then
		verify(notificationCacheService, never()).deleteCache(anyLong(), anyLong());
		verify(notificationCacheService).updateCache(eq(memberId), eq(scenario));
		verify(notificationCacheService).deleteMemberAllCache(eq(memberId));
	}


	@Test
	void Given_ValidScenarioWithActiveNotification_When_HandleUpdate_Then_ProcessWithNotification() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("저녁 루틴")
			.memo("저녁에 할 일들")
			.notification(notification)
			.build();

		ScenarioUpdateEvent event = new ScenarioUpdateEvent(memberId, scenario, false);

		// when
		scenarioUpdateEventListener.handleUpdate(event);

		// then
		verify(notificationCacheService, never()).deleteCache(anyLong(), anyLong());
		verify(notificationCacheService).updateCache(eq(memberId), eq(scenario));
	}

}
