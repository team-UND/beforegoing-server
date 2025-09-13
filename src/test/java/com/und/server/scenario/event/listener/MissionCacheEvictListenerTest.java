package com.und.server.scenario.event.listener;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.entity.Scenario;
import com.und.server.scenario.event.MissionCheckUpdateEvent;
import com.und.server.scenario.event.MissionCreateEvent;
import com.und.server.scenario.event.MissionDeleteEvent;
import com.und.server.scenario.event.ScenarioDeleteEvent;
import com.und.server.scenario.event.ScenarioUpdateEvent;
import com.und.server.scenario.service.MissionCacheService;

@ExtendWith(MockitoExtension.class)
class MissionCacheEvictListenerTest {

	@Mock
	private MissionCacheService missionCacheService;

	@InjectMocks
	private MissionCacheEvictListener missionCacheEvictListener;

	private static final Long MEMBER_ID = 1L;
	private static final Long SCENARIO_ID = 100L;
	private static final LocalDate DATE = LocalDate.of(2024, 1, 15);

	@Test
	void Given_MissionCreateEvent_When_OnMissionCreate_Then_EvictUserMissionCache() {
		MissionCreateEvent event = new MissionCreateEvent(MEMBER_ID, SCENARIO_ID, DATE);

		missionCacheEvictListener.onMissionCreate(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, DATE);
	}

	@Test
	void Given_ScenarioUpdateEvent_When_OnScenarioUpdate_Then_EvictUserMissionCache() {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getId()).thenReturn(SCENARIO_ID);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, NotificationType.TIME);

		missionCacheEvictListener.onScenarioUpdate(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_MissionCheckUpdateEvent_When_OnMissionCheckUpdate_Then_EvictUserMissionCache() {
		MissionCheckUpdateEvent event = new MissionCheckUpdateEvent(MEMBER_ID, SCENARIO_ID, DATE);

		missionCacheEvictListener.onMissionCheckUpdate(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, DATE);
	}

	@Test
	void Given_ScenarioDeleteEvent_When_OnScenarioDelete_Then_EvictUserMissionCache() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, NotificationType.TIME);

		missionCacheEvictListener.onScenarioDelete(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_MissionDeleteEvent_When_OnMissionDelete_Then_EvictUserMissionCache() {
		MissionDeleteEvent event = new MissionDeleteEvent(MEMBER_ID, SCENARIO_ID);

		missionCacheEvictListener.onMissionDelete(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_MissionCreateEventWithException_When_OnMissionCreate_Then_LogAndContinue() {
		MissionCreateEvent event = new MissionCreateEvent(MEMBER_ID, SCENARIO_ID, DATE);
		doThrow(new RuntimeException("Cache eviction failed"))
			.when(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, DATE);

		missionCacheEvictListener.onMissionCreate(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, DATE);
	}

	@Test
	void Given_ScenarioUpdateEventWithException_When_OnScenarioUpdate_Then_LogAndContinue() {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getId()).thenReturn(SCENARIO_ID);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, NotificationType.TIME);
		doThrow(new RuntimeException("Cache eviction failed"))
			.when(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);

		missionCacheEvictListener.onScenarioUpdate(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_MissionCheckUpdateEventWithException_When_OnMissionCheckUpdate_Then_LogAndContinue() {
		MissionCheckUpdateEvent event = new MissionCheckUpdateEvent(MEMBER_ID, SCENARIO_ID, DATE);
		doThrow(new RuntimeException("Cache eviction failed"))
			.when(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, DATE);

		missionCacheEvictListener.onMissionCheckUpdate(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, DATE);
	}

	@Test
	void Given_ScenarioDeleteEventWithException_When_OnScenarioDelete_Then_LogAndContinue() {
		ScenarioDeleteEvent event = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, NotificationType.TIME);
		doThrow(new RuntimeException("Cache eviction failed"))
			.when(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);

		missionCacheEvictListener.onScenarioDelete(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_MissionDeleteEventWithException_When_OnMissionDelete_Then_LogAndContinue() {
		MissionDeleteEvent event = new MissionDeleteEvent(MEMBER_ID, SCENARIO_ID);
		doThrow(new RuntimeException("Cache eviction failed"))
			.when(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);

		missionCacheEvictListener.onMissionDelete(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_DifferentMemberIds_When_OnMissionCreate_Then_EvictWithCorrectMemberId() {
		Long memberId1 = 1L;
		Long memberId2 = 2L;
		MissionCreateEvent event1 = new MissionCreateEvent(memberId1, SCENARIO_ID, DATE);
		MissionCreateEvent event2 = new MissionCreateEvent(memberId2, SCENARIO_ID, DATE);

		missionCacheEvictListener.onMissionCreate(event1);
		missionCacheEvictListener.onMissionCreate(event2);

		verify(missionCacheService).evictUserMissionCache(memberId1, SCENARIO_ID, DATE);
		verify(missionCacheService).evictUserMissionCache(memberId2, SCENARIO_ID, DATE);
	}

	@Test
	void Given_DifferentScenarioIds_When_OnScenarioUpdate_Then_EvictWithCorrectScenarioId() {
		Long scenarioId1 = 100L;
		Long scenarioId2 = 200L;
		Scenario scenario1 = mock(Scenario.class);
		when(scenario1.getId()).thenReturn(scenarioId1);
		Scenario scenario2 = mock(Scenario.class);
		when(scenario2.getId()).thenReturn(scenarioId2);
		ScenarioUpdateEvent event1 = new ScenarioUpdateEvent(
			MEMBER_ID, scenario1, true, NotificationType.TIME);
		ScenarioUpdateEvent event2 = new ScenarioUpdateEvent(
			MEMBER_ID, scenario2, false, NotificationType.LOCATION);

		missionCacheEvictListener.onScenarioUpdate(event1);
		missionCacheEvictListener.onScenarioUpdate(event2);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, scenarioId1);
		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, scenarioId2);
	}

	@Test
	void Given_DifferentDates_When_OnMissionCheckUpdate_Then_EvictWithCorrectDate() {
		LocalDate date1 = LocalDate.of(2024, 1, 1);
		LocalDate date2 = LocalDate.of(2024, 12, 31);
		MissionCheckUpdateEvent event1 = new MissionCheckUpdateEvent(MEMBER_ID, SCENARIO_ID, date1);
		MissionCheckUpdateEvent event2 = new MissionCheckUpdateEvent(MEMBER_ID, SCENARIO_ID, date2);

		missionCacheEvictListener.onMissionCheckUpdate(event1);
		missionCacheEvictListener.onMissionCheckUpdate(event2);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, date1);
		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, date2);
	}

	@Test
	void Given_AllNotificationTypes_When_OnScenarioDelete_Then_EvictCorrectly() {
		ScenarioDeleteEvent timeEvent = new ScenarioDeleteEvent(
			MEMBER_ID, SCENARIO_ID, true, NotificationType.TIME);
		ScenarioDeleteEvent locationEvent =
			new ScenarioDeleteEvent(MEMBER_ID, SCENARIO_ID, false, NotificationType.LOCATION);

		missionCacheEvictListener.onScenarioDelete(timeEvent);
		missionCacheEvictListener.onScenarioDelete(locationEvent);

		verify(missionCacheService, times(2)).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_AllNotificationTypes_When_OnScenarioUpdate_Then_EvictCorrectly() {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getId()).thenReturn(SCENARIO_ID);
		ScenarioUpdateEvent timeEvent = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, NotificationType.TIME);
		ScenarioUpdateEvent locationEvent =
			new ScenarioUpdateEvent(MEMBER_ID, scenario, false, NotificationType.LOCATION);

		missionCacheEvictListener.onScenarioUpdate(timeEvent);
		missionCacheEvictListener.onScenarioUpdate(locationEvent);

		verify(missionCacheService, times(2)).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_ExtremeValues_When_OnMissionCreate_Then_HandleCorrectly() {
		Long maxMemberId = Long.MAX_VALUE;
		Long maxScenarioId = Long.MAX_VALUE;
		LocalDate maxDate = LocalDate.MAX;
		MissionCreateEvent event = new MissionCreateEvent(maxMemberId, maxScenarioId, maxDate);

		missionCacheEvictListener.onMissionCreate(event);

		verify(missionCacheService).evictUserMissionCache(maxMemberId, maxScenarioId, maxDate);
	}

	@Test
	void Given_MinValues_When_OnMissionDelete_Then_HandleCorrectly() {
		Long minMemberId = 1L;
		Long minScenarioId = 1L;
		MissionDeleteEvent event = new MissionDeleteEvent(minMemberId, minScenarioId);

		missionCacheEvictListener.onMissionDelete(event);

		verify(missionCacheService).evictUserMissionCache(minMemberId, minScenarioId);
	}

	@Test
	void Given_NullPointerException_When_OnMissionCreate_Then_LogAndContinue() {
		MissionCreateEvent event = new MissionCreateEvent(MEMBER_ID, SCENARIO_ID, DATE);
		doThrow(new NullPointerException("Null pointer in cache service"))
			.when(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, DATE);

		missionCacheEvictListener.onMissionCreate(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID, DATE);
	}

	@Test
	void Given_IllegalArgumentException_When_OnScenarioUpdate_Then_LogAndContinue() {
		Scenario scenario = mock(Scenario.class);
		when(scenario.getId()).thenReturn(SCENARIO_ID);
		ScenarioUpdateEvent event = new ScenarioUpdateEvent(
			MEMBER_ID, scenario, true, NotificationType.TIME);
		doThrow(new IllegalArgumentException("Invalid argument"))
			.when(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);

		missionCacheEvictListener.onScenarioUpdate(event);

		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

	@Test
	void Given_ConcurrentEvents_When_OnMultipleEvents_Then_ProcessAllEvents() {
		MissionCreateEvent createEvent = new MissionCreateEvent(MEMBER_ID, SCENARIO_ID, DATE);
		MissionCheckUpdateEvent updateEvent = new MissionCheckUpdateEvent(MEMBER_ID, SCENARIO_ID, DATE);
		MissionDeleteEvent deleteEvent = new MissionDeleteEvent(MEMBER_ID, SCENARIO_ID);

		missionCacheEvictListener.onMissionCreate(createEvent);
		missionCacheEvictListener.onMissionCheckUpdate(updateEvent);
		missionCacheEvictListener.onMissionDelete(deleteEvent);

		verify(missionCacheService, times(2))
			.evictUserMissionCache(MEMBER_ID, SCENARIO_ID, DATE);
		verify(missionCacheService).evictUserMissionCache(MEMBER_ID, SCENARIO_ID);
	}

}
