package com.und.server.notification.event;

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

import com.und.server.notification.service.NotificationCacheService;


@ExtendWith(MockitoExtension.class)
class ScenarioDeleteEventListenerTest {

	@InjectMocks
	private ScenarioDeleteEventListener scenarioDeleteEventListener;

	@Mock
	private NotificationCacheService notificationCacheService;

	private final Long memberId = 1L;
	private final Long scenarioId = 1L;


	@Test
	void Given_ValidScenarioWithActiveNotification_When_HandleDelete_Then_DeleteCache() {
		// given
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(memberId, scenarioId, true);

		// when
		scenarioDeleteEventListener.handleDelete(event);

		// then
		verify(notificationCacheService).deleteCache(eq(memberId), eq(scenarioId));
	}


	@Test
	void Given_ValidScenarioWithInactiveNotification_When_HandleDelete_Then_DoNothing() {
		// given
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(memberId, scenarioId, false);

		// when
		scenarioDeleteEventListener.handleDelete(event);

		// then
		verify(notificationCacheService, never()).deleteCache(anyLong(), anyLong());
	}


	@Test
	void Given_ExceptionOccurs_When_HandleDelete_Then_DeleteMemberAllCache() {
		// given
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(memberId, scenarioId, true);

		doThrow(new RuntimeException("Cache delete failed"))
			.when(notificationCacheService).deleteCache(anyLong(), anyLong());

		// when
		scenarioDeleteEventListener.handleDelete(event);

		// then
		verify(notificationCacheService).deleteCache(eq(memberId), eq(scenarioId));
		verify(notificationCacheService).deleteMemberAllCache(eq(memberId));
	}


	@Test
	void Given_ValidScenarioWithActiveNotification_When_HandleDelete_Then_ProcessWithNotification() {
		// given
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(memberId, scenarioId, true);

		// when
		scenarioDeleteEventListener.handleDelete(event);

		// then
		verify(notificationCacheService).deleteCache(eq(memberId), eq(scenarioId));
	}


	@Test
	void Given_DifferentMemberIdAndScenarioId_When_HandleDelete_Then_DeleteCorrectCache() {
		// given
		Long differentMemberId = 2L;
		Long differentScenarioId = 3L;
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(differentMemberId, differentScenarioId, true);

		// when
		scenarioDeleteEventListener.handleDelete(event);

		// then
		verify(notificationCacheService).deleteCache(eq(differentMemberId), eq(differentScenarioId));
	}

}
