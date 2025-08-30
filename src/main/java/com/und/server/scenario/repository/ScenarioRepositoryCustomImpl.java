package com.und.server.scenario.repository;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;

import jakarta.persistence.EntityManager;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ScenarioRepositoryCustomImpl implements ScenarioRepositoryCustom {

	private final EntityManager em;

	@Override
	public List<ScenarioNotificationResponse> findTimeScenarioNotifications(Long memberId) {
		String jpql = """
			SELECT new com.und.server.scenario.repository.ScenarioRepositoryCustomImpl$TimeNotificationQueryDto(
				s.id,
				s.scenarioName,
				s.memo,
				n.id,
				n.notificationType,
				n.notificationMethodType,
				n.daysOfWeek,
				t.startHour,
				t.startMinute
			)
			FROM Scenario s
			JOIN s.notification n
			JOIN TimeNotification t ON n.id = t.notification.id
			WHERE s.member.id = :memberId
				AND n.notificationType = :timeType
				AND n.isActive = true
			""";

		List<TimeNotificationQueryDto> queryResults = em.createQuery(jpql, TimeNotificationQueryDto.class)
			.setParameter("memberId", memberId)
			.setParameter("timeType", NotificationType.TIME)
			.getResultList();

		return queryResults.stream()
			.map(TimeNotificationQueryDto::toResponse)
			.toList();
	}


	@Builder
	public record TimeNotificationQueryDto(
		Long scenarioId,
		String scenarioName,
		String memo,
		Long notificationId,
		NotificationType notificationType,
		NotificationMethodType notificationMethodType,
		String daysOfWeek,
		Integer startHour,
		Integer startMinute
	) {

		public ScenarioNotificationResponse toResponse() {
			List<Integer> days = (daysOfWeek == null || daysOfWeek.isBlank())
				? List.of()
				: Arrays.stream(daysOfWeek.split(","))
				.map(String::trim)
				.map(Integer::parseInt)
				.toList();

			TimeNotificationResponse timeNotificationResponse =
				TimeNotificationResponse.builder()
					.notificationType(NotificationType.TIME)
					.startHour(startHour)
					.startMinute(startMinute)
					.build();

			return ScenarioNotificationResponse.builder()
				.scenarioId(scenarioId)
				.scenarioName(scenarioName)
				.memo(memo)
				.notificationId(notificationId)
				.notificationType(notificationType)
				.notificationMethodType(notificationMethodType)
				.daysOfWeekOrdinal(days)
				.notificationCondition(timeNotificationResponse)
				.build();
		}
	}

}
