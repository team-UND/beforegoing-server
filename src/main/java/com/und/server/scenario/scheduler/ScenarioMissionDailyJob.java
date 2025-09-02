package com.und.server.scenario.scheduler;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.scenario.repository.MissionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScenarioMissionDailyJob {

	private static final int DEFAULT_DELETE_LIMIT = 10_000;
	private static final int DAYS_TO_SUBTRACT = 1;
	private static final int MONTHS_TO_SUBTRACT = 1;
	private final MissionRepository missionRepository;
	private final Clock clock;

	/**
	 * Daily job at midnight (00:00) - BASIC 미션 백업, DEFAULT BASIC 미션 체크상태 리셋
	 * 테스트용: 3분마다 실행
	 */
	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	@Transactional
	public void runDailyBackupJob() {
		LocalDate today = LocalDate.now(clock);
		LocalDate yesterday = today.minusDays(DAYS_TO_SUBTRACT);

		try {
			int cloned = missionRepository.bulkCloneBasicToYesterday(yesterday);
			int reset = missionRepository.bulkResetBasicIsChecked(today);
			int deleteChildBasic = missionRepository.deleteTodayChildBasics(today);

			log.info("[MISSION DAILY] Daily Mission Job: cloned={}, reset={} deleteChildBasic={}",
				cloned, reset, deleteChildBasic);
		} catch (Exception e) {
			log.error("[MISSION DAILY] Backup and reset failed, rolling back", e);
			throw e;
		}
	}

	/**
	 * Daily cleanup job at 1 AM (01:00) - 기간 만료 미션 삭제
	 */
	@Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
	@Transactional
	public void runExpiredMissionCleanupJob() {
		LocalDate today = LocalDate.now(clock);
		LocalDate expireBefore = today.minusMonths(MONTHS_TO_SUBTRACT);

		int totalDeleted = 0;

		try {
			int batchDeleted;

			do {
				batchDeleted = missionRepository.bulkDeleteExpired(expireBefore, DEFAULT_DELETE_LIMIT);
				totalDeleted += batchDeleted;
			} while (batchDeleted == DEFAULT_DELETE_LIMIT);

			log.info("[MISSION DAILY] Expired mission cleanup completed: deleted={}", totalDeleted);
		} catch (Exception e) {
			log.error("[MISSION DAILY] Expired mission cleanup failed. expireBefore={}, deletedUntilError={}",
				expireBefore, totalDeleted, e);
		}
	}

}
