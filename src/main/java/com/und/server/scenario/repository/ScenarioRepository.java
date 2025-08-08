package com.und.server.scenario.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.und.server.notification.constants.NotifType;
import com.und.server.scenario.entity.Scenario;

import jakarta.validation.constraints.NotNull;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

	@EntityGraph(attributePaths = {"notification", "missionList"})
	Optional<Scenario> findByIdAndMemberId(@NotNull Long memberId, @NotNull Long id);

	List<Scenario> findByMemberIdAndNotification_NotifTypeOrderByOrder(Long memberId, NotifType notifType);

	@Query("""
		SELECT s FROM Scenario s
		LEFT JOIN FETCH s.notification
		LEFT JOIN s.member m
		LEFT JOIN FETCH s.missionList mission
		WHERE s.id = :scenarioId
			AND m.id = :memberId
			AND (mission.useDate IS NULL OR mission.useDate = :today)
		""")
	Optional<Scenario> findByIdWithDefaultMissions(Long memberId, Long scenarioId, LocalDate today);

	@Query("""
		SELECT MAX(s.order)
		FROM Scenario s
		WHERE s.member.id = :memberId
			AND s.notification.notifType = :notifType
		""")
	Optional<Integer> findMaxOrderByMemberIdAndNotifType(Long memberId, NotifType notifType);

}
