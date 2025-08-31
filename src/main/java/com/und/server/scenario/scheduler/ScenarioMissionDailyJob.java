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

	private final MissionRepository missionRepository;

	//테스트를 위해ㅏ며 3분마다
	@Scheduled(cron = "0 */3 * * * *", zone = "Asia/Seoul")
	@Transactional
	public void runDailyJob() {
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		LocalDate yesterday = today.minusDays(1);
		LocalDate expireBefore = today.minusMonths(1);

		// 1. BASIC 백업
		int cloned = missionRepository.bulkCloneBasicToYesterday(yesterday);

		// 2. BASIC 원본 초기화
		int reset = missionRepository.resetBasicIsChecked();

		// 3. 만료 미션 삭제
		int deleted = missionRepository.bulkDeleteExpired(expireBefore);

		log.info("[BACK UP] Daily Mission Job: cloned={}, reset={}, deleted={}", cloned, reset, deleted);
	}

}
