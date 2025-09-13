package com.und.server.scenario.event.listener;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.event.ScenarioCreateEvent;
import com.und.server.scenario.event.ScenarioDeleteEvent;
import com.und.server.scenario.event.ScenarioOrderUpdateEvent;
import com.und.server.scenario.event.ScenarioUpdateEvent;
import com.und.server.scenario.service.ScenarioCacheService;

@ExtendWith(MockitoExtension.class)
class ScenarioCacheEvictListenerTest {

	@Mock
	private ScenarioCacheService scenarioCacheService;

	@InjectMocks
	private ScenarioCacheEvictListener scenarioCacheEvictListener;

	private static final Long MEMBER_ID = 1L;
	private static final Long SCENARIO_ID = 100L;

	@Test
	void Given_ScenarioCreateEvent_When_OnScenarioCreate_Then_EvictUserScenarioCache() {
		Scenario scenario = mock(Scenario.class);
		ScenarioCreateEvent event = new ScenarioCreateEvent(
			MEMBER_ID, scenario, NotificationType.TIME);

		scenarioCacheEvictListener.onScenarioCreate(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
	}

	@Test
	void Given_ScenarioUpdateEvent_When_OnScenarioUpdate_Then_EvictUserScenarioCache() {
		Scenario scenario = mock(Scenario.class);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, NotificationType.LOCATION);

		scenarioCacheEvictListener.onScenarioUpdate(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.LOCATION);
	}

	@Test
	void Given_ScenarioOrderUpdateEvent_When_OnScenarioOrderUpdate_Then_EvictUserScenarioCache() {
		ScenarioOrderUpdateEvent event = new ScenarioOrderUpdateEvent(MEMBER_ID);

		scenarioCacheEvictListener.onScenarioOrderUpdate(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID);
	}

	@Test
	void Given_ScenarioDeleteEvent_When_OnScenarioDelete_Then_EvictUserScenarioCache() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, NotificationType.TIME);

		scenarioCacheEvictListener.onScenarioDelete(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
	}

	@Test
	void Given_ScenarioCreateEventWithException_When_OnScenarioCreate_Then_LogAndContinue() {
		Scenario scenario = mock(Scenario.class);
		ScenarioCreateEvent event = new ScenarioCreateEvent(
			MEMBER_ID, scenario, NotificationType.TIME);
		doThrow(new RuntimeException("Cache eviction failed"))
			.when(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);

		scenarioCacheEvictListener.onScenarioCreate(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
	}

	@Test
	void Given_ScenarioUpdateEventWithException_When_OnScenarioUpdate_Then_LogAndContinue() {
		Scenario scenario = mock(Scenario.class);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, NotificationType.LOCATION);
		doThrow(new RuntimeException("Cache eviction failed"))
			.when(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.LOCATION);

		scenarioCacheEvictListener.onScenarioUpdate(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.LOCATION);
	}

	@Test
	void Given_ScenarioOrderUpdateEventWithException_When_OnScenarioOrderUpdate_Then_LogAndContinue() {
		ScenarioOrderUpdateEvent event = new ScenarioOrderUpdateEvent(MEMBER_ID);
		doThrow(new RuntimeException("Cache eviction failed"))
			.when(scenarioCacheService).evictUserScenarioCache(MEMBER_ID);

		scenarioCacheEvictListener.onScenarioOrderUpdate(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID);
	}

	@Test
	void Given_ScenarioDeleteEventWithException_When_OnScenarioDelete_Then_LogAndContinue() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, NotificationType.TIME);
		doThrow(new RuntimeException("Cache eviction failed"))
			.when(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);

		scenarioCacheEvictListener.onScenarioDelete(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
	}

	@Test
	void Given_DifferentMemberIds_When_OnScenarioCreate_Then_EvictWithCorrectMemberId() {
		Long memberId1 = 1L;
		Long memberId2 = 2L;
		Scenario scenario1 = mock(Scenario.class);
		Scenario scenario2 = mock(Scenario.class);
		ScenarioCreateEvent event1 = new ScenarioCreateEvent(
			memberId1, scenario1, NotificationType.TIME);
		ScenarioCreateEvent event2 = new ScenarioCreateEvent(
			memberId2, scenario2, NotificationType.LOCATION);

		scenarioCacheEvictListener.onScenarioCreate(event1);
		scenarioCacheEvictListener.onScenarioCreate(event2);

		verify(scenarioCacheService).evictUserScenarioCache(memberId1, NotificationType.TIME);
		verify(scenarioCacheService).evictUserScenarioCache(memberId2, NotificationType.LOCATION);
	}

	@Test
	void Given_AllNotificationTypes_When_OnScenarioUpdate_Then_EvictCorrectly() {
		Scenario scenario = mock(Scenario.class);
		ScenarioUpdateEvent timeEvent = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, NotificationType.TIME);
		ScenarioUpdateEvent locationEvent = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, false, NotificationType.LOCATION);

		scenarioCacheEvictListener.onScenarioUpdate(timeEvent);
		scenarioCacheEvictListener.onScenarioUpdate(locationEvent);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.LOCATION);
	}

	@Test
	void Given_AllNotificationTypes_When_OnScenarioDelete_Then_EvictCorrectly() {
		ScenarioDeleteEvent timeEvent = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, NotificationType.TIME);
		ScenarioDeleteEvent locationEvent = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, false, NotificationType.LOCATION);

		scenarioCacheEvictListener.onScenarioDelete(timeEvent);
		scenarioCacheEvictListener.onScenarioDelete(locationEvent);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.LOCATION);
	}

	@Test
	void Given_ExtremeValues_When_OnScenarioCreate_Then_HandleCorrectly() {
		Long maxMemberId = Long.MAX_VALUE;
		Scenario scenario = mock(Scenario.class);
		ScenarioCreateEvent event = new ScenarioCreateEvent(
			maxMemberId, scenario, NotificationType.TIME);

		scenarioCacheEvictListener.onScenarioCreate(event);

		verify(scenarioCacheService).evictUserScenarioCache(maxMemberId, NotificationType.TIME);
	}

	@Test
	void Given_MinValues_When_OnScenarioOrderUpdate_Then_HandleCorrectly() {
		Long minMemberId = 1L;
		ScenarioOrderUpdateEvent event = new ScenarioOrderUpdateEvent(minMemberId);

		scenarioCacheEvictListener.onScenarioOrderUpdate(event);

		verify(scenarioCacheService).evictUserScenarioCache(minMemberId);
	}

	@Test
	void Given_NullPointerException_When_OnScenarioCreate_Then_LogAndContinue() {
		Scenario scenario = mock(Scenario.class);
		ScenarioCreateEvent event = new ScenarioCreateEvent(
			MEMBER_ID, scenario, NotificationType.TIME);
		doThrow(new NullPointerException("Null pointer in cache service"))
			.when(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);

		scenarioCacheEvictListener.onScenarioCreate(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
	}

	@Test
	void Given_IllegalArgumentException_When_OnScenarioUpdate_Then_LogAndContinue() {
		Scenario scenario = mock(Scenario.class);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, NotificationType.LOCATION);
		doThrow(new IllegalArgumentException("Invalid argument"))
			.when(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.LOCATION);

		scenarioCacheEvictListener.onScenarioUpdate(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.LOCATION);
	}

	@Test
	void Given_ConcurrentEvents_When_OnMultipleEvents_Then_ProcessAllEvents() {
		Scenario scenario = mock(Scenario.class);
		ScenarioCreateEvent createEvent = new ScenarioCreateEvent(
			MEMBER_ID, scenario, NotificationType.TIME);
		ScenarioUpdateEvent updateEvent = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, NotificationType.LOCATION);
		ScenarioOrderUpdateEvent orderEvent = new ScenarioOrderUpdateEvent(MEMBER_ID);
		ScenarioDeleteEvent deleteEvent = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, NotificationType.TIME);

		scenarioCacheEvictListener.onScenarioCreate(createEvent);
		scenarioCacheEvictListener.onScenarioUpdate(updateEvent);
		scenarioCacheEvictListener.onScenarioOrderUpdate(orderEvent);
		scenarioCacheEvictListener.onScenarioDelete(deleteEvent);

		verify(scenarioCacheService, times(2))
			.evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.LOCATION);
		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID);
	}

	@Test
	void Given_DifferentNotificationTypes_When_OnScenarioCreate_Then_EvictWithCorrectType() {
		Scenario scenario = mock(Scenario.class);
		ScenarioCreateEvent timeEvent = new ScenarioCreateEvent(
			MEMBER_ID, scenario, NotificationType.TIME);
		ScenarioCreateEvent locationEvent = new ScenarioCreateEvent(
			MEMBER_ID, scenario, NotificationType.LOCATION);

		scenarioCacheEvictListener.onScenarioCreate(timeEvent);
		scenarioCacheEvictListener.onScenarioCreate(locationEvent);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.LOCATION);
	}

	@Test
	void Given_MultipleOrderUpdates_When_OnScenarioOrderUpdate_Then_EvictMultipleTimes() {
		Long memberId1 = 1L;
		Long memberId2 = 2L;
		ScenarioOrderUpdateEvent event1 = new ScenarioOrderUpdateEvent(memberId1);
		ScenarioOrderUpdateEvent event2 = new ScenarioOrderUpdateEvent(memberId2);

		scenarioCacheEvictListener.onScenarioOrderUpdate(event1);
		scenarioCacheEvictListener.onScenarioOrderUpdate(event2);

		verify(scenarioCacheService).evictUserScenarioCache(memberId1);
		verify(scenarioCacheService).evictUserScenarioCache(memberId2);
	}

	@Test
	void Given_SecurityException_When_OnScenarioDelete_Then_LogAndContinue() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, NotificationType.TIME);
		doThrow(new SecurityException("Access denied"))
			.when(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);

		scenarioCacheEvictListener.onScenarioDelete(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID, NotificationType.TIME);
	}

	@Test
	void Given_TimeoutException_When_OnScenarioOrderUpdate_Then_LogAndContinue() {
		ScenarioOrderUpdateEvent event = new ScenarioOrderUpdateEvent(MEMBER_ID);
		doThrow(new RuntimeException("Operation timeout"))
			.when(scenarioCacheService).evictUserScenarioCache(MEMBER_ID);

		scenarioCacheEvictListener.onScenarioOrderUpdate(event);

		verify(scenarioCacheService).evictUserScenarioCache(MEMBER_ID);
	}

}
