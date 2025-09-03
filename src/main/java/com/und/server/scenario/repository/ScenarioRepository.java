package com.und.server.scenario.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.und.server.notification.constants.NotificationType;
import com.und.server.scenario.entity.Scenario;

import jakarta.validation.constraints.NotNull;

public interface ScenarioRepository extends JpaRepository<Scenario, Long>, ScenarioRepositoryCustom {

	Optional<Scenario> findByIdAndMemberId(@NotNull Long id, @NotNull Long memberId);

	@Query("""
		SELECT s FROM Scenario s
		LEFT JOIN FETCH s.notification
		LEFT JOIN FETCH s.missions m
		WHERE s.id = :id
			AND s.member.id = :memberId
			AND m.missionType = 'BASIC'
			AND m.useDate IS NULL
		""")
	Optional<Scenario> findScenarioDetailFetchByIdAndMemberId(@NotNull Long memberId, @NotNull Long id);

	@Query("""
		SELECT s FROM Scenario s
		LEFT JOIN FETCH s.notification
		LEFT JOIN FETCH s.missions m
		WHERE s.id = :id
			AND s.member.id = :memberId
			AND (m.useDate IS NULL OR m.useDate = :date)
		""")
	Optional<Scenario> findTodayScenarioFetchByIdAndMemberId(
		@NotNull Long memberId, @NotNull Long id, @NotNull LocalDate date);

	@Query("""
		SELECT s FROM Scenario s
		LEFT JOIN FETCH s.notification
		WHERE s.id = :id
			AND s.member.id = :memberId
		""")
	Optional<Scenario> findNotificationFetchByIdAndMemberId(@NotNull Long memberId, @NotNull Long id);

	@Query("""
		SELECT s FROM Scenario s
		WHERE s.member.id = :memberId
			AND s.notification.notificationType = :notificationType
		ORDER BY s.scenarioOrder
		""")
	@NotNull
	List<Scenario> findByMemberIdAndNotificationType(
		@NotNull Long memberId, @NotNull NotificationType notificationType);

	@Query("""
		SELECT s.scenarioOrder
		FROM Scenario s
		WHERE s.member.id = :memberId
			AND s.notification.notificationType = :notificationType
		ORDER BY s.scenarioOrder
		""")
	@NotNull
	List<Integer> findOrdersByMemberIdAndNotificationType(
		@NotNull Long memberId, @NotNull NotificationType notificationType);

}
