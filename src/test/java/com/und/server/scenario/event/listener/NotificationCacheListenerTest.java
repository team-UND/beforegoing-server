package com.und.server.scenario.event.listener;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.entity.Notification;
import com.und.server.notification.exception.NotificationCacheErrorResult;
import com.und.server.notification.exception.NotificationCacheException;
import com.und.server.notification.service.NotificationCacheService;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.event.ActiveUpdateEvent;
import com.und.server.scenario.event.ScenarioCreateEvent;
import com.und.server.scenario.event.ScenarioDeleteEvent;
import com.und.server.scenario.event.ScenarioUpdateEvent;

@ExtendWith(MockitoExtension.class)
class NotificationCacheListenerTest {

	@Mock
	private NotificationCacheService notificationCacheService;

	@InjectMocks
	private NotificationCacheListener notificationCacheListener;

	private static final Long MEMBER_ID = 1L;
	private static final Long SCENARIO_ID = 100L;

	@Test
	void Given_ScenarioCreateEventWithActiveNotification_When_OnCreate_Then_UpdateCache() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(true);
		when(scenario.getNotification()).thenReturn(notification);
		ScenarioCreateEvent event = new ScenarioCreateEvent(MEMBER_ID, scenario, null);

		notificationCacheListener.onCreate(event);

		verify(notificationCacheService).updateCache(MEMBER_ID, scenario);
	}

	@Test
	void Given_ScenarioCreateEventWithInactiveNotification_When_OnCreate_Then_DoNothing() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(false);
		when(scenario.getNotification()).thenReturn(notification);
		ScenarioCreateEvent event = new ScenarioCreateEvent(MEMBER_ID, scenario, null);

		notificationCacheListener.onCreate(event);

		verify(notificationCacheService, never()).updateCache(MEMBER_ID, scenario);
	}

	@Test
	void Given_ScenarioCreateEventWithNullNotification_When_OnCreate_Then_DoNothing() {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getNotification()).thenReturn(null);
		ScenarioCreateEvent event = new ScenarioCreateEvent(MEMBER_ID, scenario, null);

		notificationCacheListener.onCreate(event);

		verify(notificationCacheService, never()).updateCache(MEMBER_ID, scenario);
	}

	@Test
	void Given_ScenarioCreateEventWithCacheException_When_OnCreate_Then_DeleteMemberAllCache() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(true);
		when(scenario.getNotification()).thenReturn(notification);
		doThrow(new NotificationCacheException(NotificationCacheErrorResult.CACHE_UPDATE_FAILED))
			.when(notificationCacheService).updateCache(MEMBER_ID, scenario);
		ScenarioCreateEvent event = new ScenarioCreateEvent(MEMBER_ID, scenario, null);

		notificationCacheListener.onCreate(event);

		verify(notificationCacheService).updateCache(MEMBER_ID, scenario);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_ScenarioCreateEventWithUnexpectedException_When_OnCreate_Then_DeleteMemberAllCache() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(true);
		when(scenario.getNotification()).thenReturn(notification);
		doThrow(new RuntimeException("Unexpected error"))
			.when(notificationCacheService).updateCache(MEMBER_ID, scenario);
		ScenarioCreateEvent event = new ScenarioCreateEvent(MEMBER_ID, scenario, null);

		notificationCacheListener.onCreate(event);

		verify(notificationCacheService).updateCache(MEMBER_ID, scenario);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_ScenarioUpdateEventWithActiveNotification_When_OnUpdate_Then_UpdateCache() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(true);
		when(scenario.getNotification()).thenReturn(notification);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, false, null);

		notificationCacheListener.onUpdate(event);

		verify(notificationCacheService).updateCache(MEMBER_ID, scenario);
	}

	@Test
	void Given_ScenarioUpdateEventWithInactiveNotificationAndOldActive_When_OnUpdate_Then_DeleteCache() {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getId()).thenReturn(SCENARIO_ID);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(false);
		when(scenario.getNotification()).thenReturn(notification);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, null);

		notificationCacheListener.onUpdate(event);

		verify(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);
		verify(notificationCacheService, never()).updateCache(MEMBER_ID, scenario);
	}

	@Test
	void Given_ScenarioUpdateEventWithInactiveNotificationAndOldInactive_When_OnUpdate_Then_DoNothing() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(false);
		when(scenario.getNotification()).thenReturn(notification);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, false, null);

		notificationCacheListener.onUpdate(event);

		verify(notificationCacheService, never()).updateCache(MEMBER_ID, scenario);
		verify(notificationCacheService, never()).deleteCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_ScenarioUpdateEventWithNullNotificationAndOldActive_When_OnUpdate_Then_DeleteCache() {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getId()).thenReturn(SCENARIO_ID);
		when(scenario.getNotification()).thenReturn(null);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, null);

		notificationCacheListener.onUpdate(event);

		verify(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);
		verify(notificationCacheService, never()).updateCache(MEMBER_ID, scenario);
	}

	@Test
	void Given_ScenarioUpdateEventWithCacheException_When_OnUpdate_Then_DeleteMemberAllCache() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(true);
		when(scenario.getNotification()).thenReturn(notification);
		doThrow(new NotificationCacheException(NotificationCacheErrorResult.CACHE_UPDATE_FAILED))
			.when(notificationCacheService).updateCache(MEMBER_ID, scenario);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, false, null);

		notificationCacheListener.onUpdate(event);

		verify(notificationCacheService).updateCache(MEMBER_ID, scenario);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_ScenarioUpdateEventWithUnexpectedException_When_OnUpdate_Then_DeleteMemberAllCache() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(true);
		when(scenario.getNotification()).thenReturn(notification);
		doThrow(new RuntimeException("Unexpected error"))
			.when(notificationCacheService).updateCache(MEMBER_ID, scenario);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, false, null);

		notificationCacheListener.onUpdate(event);

		verify(notificationCacheService).updateCache(MEMBER_ID, scenario);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_ActiveUpdateEventWithActiveTrue_When_OnActiveUpdate_Then_RefreshCache() {
		ActiveUpdateEvent event = new ActiveUpdateEvent(MEMBER_ID, true);

		notificationCacheListener.onActiveUpdate(event);

		verify(notificationCacheService).refreshCacheFromDatabase(MEMBER_ID);
		verify(notificationCacheService, never()).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_ActiveUpdateEventWithActiveFalse_When_OnActiveUpdate_Then_DeleteMemberAllCache() {
		ActiveUpdateEvent event = new ActiveUpdateEvent(MEMBER_ID, false);

		notificationCacheListener.onActiveUpdate(event);

		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
		verify(notificationCacheService, never()).refreshCacheFromDatabase(MEMBER_ID);
	}

	@Test
	void Given_ActiveUpdateEventWithException_When_OnActiveUpdate_Then_DeleteMemberAllCache() {
		ActiveUpdateEvent event = new ActiveUpdateEvent(MEMBER_ID, true);
		doThrow(new RuntimeException("Unexpected error"))
			.when(notificationCacheService).refreshCacheFromDatabase(MEMBER_ID);

		notificationCacheListener.onActiveUpdate(event);

		verify(notificationCacheService).refreshCacheFromDatabase(MEMBER_ID);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_ScenarioDeleteEventWithActiveNotification_When_OnDelete_Then_DeleteCache() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, null);

		notificationCacheListener.onDelete(event);

		verify(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_ScenarioDeleteEventWithInactiveNotification_When_OnDelete_Then_DoNothing() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, false, null);

		notificationCacheListener.onDelete(event);

		verify(notificationCacheService, never()).deleteCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_ScenarioDeleteEventWithCacheException_When_OnDelete_Then_DeleteMemberAllCache() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, null);
		doThrow(new NotificationCacheException(NotificationCacheErrorResult.CACHE_DELETE_FAILED))
			.when(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);

		notificationCacheListener.onDelete(event);

		verify(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_ScenarioDeleteEventWithUnexpectedException_When_OnDelete_Then_DeleteMemberAllCache() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, null);
		doThrow(new RuntimeException("Unexpected error"))
			.when(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);

		notificationCacheListener.onDelete(event);

		verify(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_DifferentMemberIds_When_OnCreate_Then_UpdateCacheWithCorrectMemberId() {
		Long memberId1 = 1L;
		Long memberId2 = 2L;
		Scenario scenario1 = mock(Scenario.class);
		Scenario scenario2 = mock(Scenario.class);
		Notification notification1 = mock(Notification.class);
		Notification notification2 = mock(Notification.class);
		when(notification1.isActive()).thenReturn(true);
		when(notification2.isActive()).thenReturn(true);
		when(scenario1.getNotification()).thenReturn(notification1);
		when(scenario2.getNotification()).thenReturn(notification2);
		ScenarioCreateEvent event1 = new ScenarioCreateEvent(memberId1, scenario1, null);
		ScenarioCreateEvent event2 = new ScenarioCreateEvent(memberId2, scenario2, null);

		notificationCacheListener.onCreate(event1);
		notificationCacheListener.onCreate(event2);

		verify(notificationCacheService).updateCache(memberId1, scenario1);
		verify(notificationCacheService).updateCache(memberId2, scenario2);
	}

	@Test
	void Given_ExtremeValues_When_OnActiveUpdate_Then_HandleCorrectly() {
		Long maxMemberId = Long.MAX_VALUE;
		ActiveUpdateEvent event = new ActiveUpdateEvent(maxMemberId, true);

		notificationCacheListener.onActiveUpdate(event);

		verify(notificationCacheService).refreshCacheFromDatabase(maxMemberId);
	}

	@Test
	void Given_MinValues_When_OnDelete_Then_HandleCorrectly() {
		Long minMemberId = 1L;
		Long minScenarioId = 1L;
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			minMemberId, minScenarioId, true, null);

		notificationCacheListener.onDelete(event);

		verify(notificationCacheService).deleteCache(minMemberId, minScenarioId);
	}

	@Test
	void Given_NullPointerException_When_OnCreate_Then_DeleteMemberAllCache() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(true);
		when(scenario.getNotification()).thenReturn(notification);
		doThrow(new NullPointerException("Null pointer"))
			.when(notificationCacheService).updateCache(MEMBER_ID, scenario);
		ScenarioCreateEvent event = new ScenarioCreateEvent(MEMBER_ID, scenario, null);

		notificationCacheListener.onCreate(event);

		verify(notificationCacheService).updateCache(MEMBER_ID, scenario);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_IllegalArgumentException_When_OnUpdate_Then_DeleteMemberAllCache() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(true);
		when(scenario.getNotification()).thenReturn(notification);
		doThrow(new IllegalArgumentException("Invalid argument"))
			.when(notificationCacheService).updateCache(MEMBER_ID, scenario);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, false, null);

		notificationCacheListener.onUpdate(event);

		verify(notificationCacheService).updateCache(MEMBER_ID, scenario);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_ConcurrentEvents_When_OnMultipleEvents_Then_ProcessAllEvents() {
		Scenario scenario = mock(Scenario.class);
		Notification notification = mock(Notification.class);
		when(notification.isActive()).thenReturn(true);
		when(scenario.getNotification()).thenReturn(notification);
		ScenarioCreateEvent createEvent = new ScenarioCreateEvent(
			MEMBER_ID, scenario, null);
		ScenarioUpdateEvent updateEvent = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, false, null);
		ActiveUpdateEvent activeEvent = new ActiveUpdateEvent(
			MEMBER_ID, true);
		ScenarioDeleteEvent deleteEvent = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, null);

		notificationCacheListener.onCreate(createEvent);
		notificationCacheListener.onUpdate(updateEvent);
		notificationCacheListener.onActiveUpdate(activeEvent);
		notificationCacheListener.onDelete(deleteEvent);

		verify(notificationCacheService, times(2)).updateCache(MEMBER_ID, scenario);
		verify(notificationCacheService).refreshCacheFromDatabase(MEMBER_ID);
		verify(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_SecurityException_When_OnActiveUpdate_Then_DeleteMemberAllCache() {
		ActiveUpdateEvent event = new ActiveUpdateEvent(MEMBER_ID, true);
		doThrow(new SecurityException("Access denied"))
			.when(notificationCacheService).refreshCacheFromDatabase(MEMBER_ID);

		notificationCacheListener.onActiveUpdate(event);

		verify(notificationCacheService).refreshCacheFromDatabase(MEMBER_ID);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

	@Test
	void Given_TimeoutException_When_OnDelete_Then_DeleteMemberAllCache() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, null);
		doThrow(new RuntimeException("Operation timeout"))
			.when(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);

		notificationCacheListener.onDelete(event);

		verify(notificationCacheService).deleteCache(MEMBER_ID, SCENARIO_ID);
		verify(notificationCacheService).deleteMemberAllCache(MEMBER_ID);
	}

}
