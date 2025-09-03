package com.und.server.scenario.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.scenario.repository.MissionRepository;

@ExtendWith(MockitoExtension.class)
class ScenarioMissionDailyJobTest {

	@Mock
	private MissionRepository missionRepository;

	@InjectMocks
	private ScenarioMissionDailyJob job;

	private Clock fixedClock;


	@BeforeEach
	void setUp() {
		fixedClock = Clock.fixed(
			LocalDate.of(2025, 9, 1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(),
			ZoneId.of("Asia/Seoul")
		);
		job = new ScenarioMissionDailyJob(missionRepository, fixedClock);
	}


	@Test
	void Given_NormalCase_When_RunDailyBackupJob_Then_CloneAndResetCalled() {
		// given
		when(missionRepository.bulkCloneBasicToYesterday(any(LocalDate.class))).thenReturn(7);
		when(missionRepository.bulkResetBasicIsChecked()).thenReturn(100);

		// when
		job.runDailyBackupJob();

		// then
		ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
		verify(missionRepository).bulkCloneBasicToYesterday(dateCaptor.capture());
		verify(missionRepository).bulkResetBasicIsChecked();

		LocalDate captured = dateCaptor.getValue();
		LocalDate expectedYesterday = LocalDate.of(2025, 8, 31);
		assertThat(captured).isEqualTo(expectedYesterday);
	}


	@Test
	void Given_RepositoryThrows_When_RunDailyBackupJob_Then_Throws() {
		// given
		when(missionRepository.bulkCloneBasicToYesterday(any(LocalDate.class)))
			.thenThrow(new RuntimeException("db error"));

		// then
		assertThatThrownBy(() -> job.runDailyBackupJob())
			.isInstanceOf(RuntimeException.class);
	}


	@Test
	void Given_ZeroDeleted_When_RunExpiredCleanupJob_Then_CallOnceAndNoThrow() {
		// given
		when(missionRepository.bulkDeleteExpired(any(LocalDate.class), anyInt())).thenReturn(0);

		// when & then
		assertThatCode(() -> job.runExpiredMissionCleanupJob()).doesNotThrowAnyException();
		verify(missionRepository, times(1)).bulkDeleteExpired(any(LocalDate.class), anyInt());
	}


	@Test
	void Given_DefaultBatchThenSmaller_When_RunExpiredCleanupJob_Then_LoopsAndStops() {
		// given
		when(missionRepository.bulkDeleteExpired(any(LocalDate.class), anyInt()))
			.thenReturn(10_000)
			.thenReturn(5_000);

		// when
		assertThatCode(() -> job.runExpiredMissionCleanupJob()).doesNotThrowAnyException();

		// then
		verify(missionRepository, times(2)).bulkDeleteExpired(any(LocalDate.class), anyInt());
	}


	@Test
	void Given_RepositoryThrows_When_RunExpiredCleanupJob_Then_NoThrow() {
		// given
		when(missionRepository.bulkDeleteExpired(any(LocalDate.class), anyInt()))
			.thenThrow(new RuntimeException("db error"));

		// when & then
		assertThatCode(() -> job.runExpiredMissionCleanupJob()).doesNotThrowAnyException();
	}

}
