package com.und.server.scenario.repository;

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
	Optional<Scenario> findById(@NotNull Long id);

	List<Scenario> findByMemberIdAndNotification_NotifTypeOrderByOrder(Long memberId, NotifType notifType);

	@Query("""
			SELECT MAX(s.order)
			FROM Scenario s
			WHERE s.member.id = :memberId
				AND s.notification.notifType = :notifType
		""")
	Optional<Integer> findMaxOrderByMemberIdAndNotifType(Long memberId, NotifType notifType);

}
