package com.und.server.scenario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.und.server.notification.constants.NotifType;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.entity.Scenario;

import jakarta.validation.constraints.NotNull;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

	Optional<Scenario> findByIdAndMemberId(@NotNull Long id, @NotNull Long memberId);

	@Query("""
		SELECT s FROM Scenario s
		WHERE s.member.id = :memberId
			AND s.notification.notificationType = :notifType
		ORDER BY s.scenarioOrder
		""")
	List<Scenario> findByMemberIdAndNotificationType(Long memberId, NotifType notifType);

	@Query("""
		SELECT s FROM Scenario s
		LEFT JOIN FETCH s.notification
		LEFT JOIN FETCH s.missionList
		WHERE s.id = :id
			AND s.member.id = :memberId
		""")
	Optional<Scenario> findFetchByIdAndMemberId(@NotNull Long memberId, @NotNull Long id);

	@Query("""
		SELECT s FROM Scenario s
		LEFT JOIN FETCH s.notification
		LEFT JOIN s.member m
		LEFT JOIN FETCH s.missionList mission
		WHERE s.id = :scenarioId
			AND m.id = :memberId
			AND mission.missionType = :missionType
			AND mission.useDate IS NULL
		""")
	Optional<Scenario> findByIdWithDefaultBasicMissions(Long memberId, Long scenarioId, MissionType missionType);

	@Query("""
		SELECT MAX(s.scenarioOrder)
		FROM Scenario s
		WHERE s.member.id = :memberId
			AND s.notification.notificationType = :notifType
		""")
	Optional<Integer> findMaxOrderByMemberIdAndNotifType(Long memberId, NotifType notifType);

	@Query("""
		SELECT s.scenarioOrder
		FROM Scenario s
		WHERE s.member.id = :memberId
			AND s.notification.notificationType = :notifType
		ORDER BY s.scenarioOrder
		""")
	List<Integer> findOrdersByMemberIdAndNotification_NotifType(Long memberId, NotifType notifType);

}
