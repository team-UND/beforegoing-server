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
class ScenarioCreateEventListenerTest {

	@InjectMocks
	private ScenarioCreateEventListener scenarioCreateEventListener;

	@Mock
	private NotificationCacheService notificationCacheService;

	private final Long memberId = 1L;
	private final Long scenarioId = 1L;


	@Test
	void Given_ValidScenarioWithActiveNotification_When_HandleCreate_Then_UpdateCache() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("테스트 루틴")
			.memo("테스트 메모")
			.notification(notification)
			.build();

		ScenarioCreateEvent event = new ScenarioCreateEvent(memberId, scenario);

		// when
		scenarioCreateEventListener.handleCreate(event);

		// then
		verify(notificationCacheService).updateCache(eq(memberId), eq(scenario));
	}


	@Test
	void Given_ValidScenarioWithInactiveNotification_When_HandleCreate_Then_DoNothing() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(false)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("테스트 루틴")
			.memo("테스트 메모")
			.notification(notification)
			.build();

		ScenarioCreateEvent event = new ScenarioCreateEvent(memberId, scenario);

		// when
		scenarioCreateEventListener.handleCreate(event);

		// then
		verify(notificationCacheService, never()).updateCache(anyLong(), any());
	}


	@Test
	void Given_ValidScenarioWithNullNotification_When_HandleCreate_Then_DoNothing() {
		// given
		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("테스트 루틴")
			.memo("테스트 메모")
			.notification(null)
			.build();

		ScenarioCreateEvent event = new ScenarioCreateEvent(memberId, scenario);

		// when
		scenarioCreateEventListener.handleCreate(event);

		// then
		verify(notificationCacheService, never()).updateCache(anyLong(), any());
	}


	@Test
	void Given_ExceptionOccurs_When_HandleCreate_Then_DeleteMemberAllCache() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("테스트 루틴")
			.memo("테스트 메모")
			.notification(notification)
			.build();

		ScenarioCreateEvent event = new ScenarioCreateEvent(memberId, scenario);

		doThrow(new RuntimeException("Cache update failed"))
			.when(notificationCacheService).updateCache(anyLong(), any());

		// when
		scenarioCreateEventListener.handleCreate(event);

		// then
		verify(notificationCacheService).updateCache(eq(memberId), eq(scenario));
		verify(notificationCacheService).deleteMemberAllCache(eq(memberId));
	}


	@Test
	void Given_ValidScenarioWithActiveNotification_When_HandleCreate_Then_ProcessWithNotification() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.build();

		Scenario scenario = Scenario.builder()
			.id(scenarioId)
			.scenarioName("아침 루틴")
			.memo("아침에 할 일들")
			.notification(notification)
			.build();

		ScenarioCreateEvent event = new ScenarioCreateEvent(memberId, scenario);

		// when
		scenarioCreateEventListener.handleCreate(event);

		// then
		verify(notificationCacheService).updateCache(eq(memberId), eq(scenario));
	}

}
