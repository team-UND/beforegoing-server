package com.und.server.scenario.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;

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

	//테스트를 위해ㅏ며 3분마다
	@Scheduled(cron = "0 */2 * * * *", zone = "Asia/Seoul")
	@Transactional
	public void runDailyJob() {
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		LocalDate yesterday = today.minusDays(DAYS_TO_SUBTRACT);
		LocalDate expireBefore = today.minusMonths(MONTHS_TO_SUBTRACT);

		// 1. BASIC 백업
		int cloned = missionRepository.bulkCloneBasicToYesterday(yesterday);

		// 2. BASIC 원본 초기화
		int reset = missionRepository.resetBasicIsChecked();

		// 3. 만료 미션 삭제
		int deleted = missionRepository.bulkDeleteExpired(expireBefore, DEFAULT_DELETE_LIMIT);

		log.info("[BACK UP] Daily Mission Job: cloned={}, reset={}, deleted={}", cloned, reset, deleted);
	}
	/**
	 * 확인해야할 사항
	 * 1. 미션 백업 - 체크상태는 그대로
	 * 2. 미션 체크리스트 초기화 - useDate가 null인 경우만
	 * 3. Today는 그대로인지
	 *
	 * 1. 미션 생성
	 */

}
