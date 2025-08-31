package com.und.server.scenario.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.und.server.scenario.entity.Mission;

import jakarta.validation.constraints.NotNull;

public interface MissionRepository extends JpaRepository<Mission, Long> {

	@EntityGraph(attributePaths = {"scenario", "scenario.member"})
	Optional<Mission> findById(@NotNull Long id);

	@Query("""
		SELECT m FROM Mission m
		LEFT JOIN m.scenario s
		WHERE s.id = :scenarioId
			AND s.member.id = :memberId
			AND (m.useDate IS NULL OR m.useDate = :date)
		""")
	@NotNull
	List<Mission> findDefaultMissions(@NotNull Long memberId, @NotNull Long scenarioId, @NotNull LocalDate date);

	@Query("""
		SELECT m FROM Mission m
		LEFT JOIN m.scenario s
		WHERE s.id = :scenarioId
			AND s.member.id = :memberId
			AND m.useDate = :date
		""")
	@NotNull
	List<Mission> findMissionsByDate(@NotNull Long memberId, @NotNull Long scenarioId, @NotNull LocalDate date);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM Mission m WHERE m.scenario.id = :scenarioId")
	int deleteByScenarioId(Long scenarioId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		UPDATE Mission m
		SET m.isChecked = false
		WHERE m.useDate IS NULL
		  AND m.missionType = 'BASIC'
		""")
	int resetBasicIsChecked();

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
		INSERT INTO mission (
		  scenario_id, content, is_checked, mission_order, use_date, mission_type, created_at, updated_at
		)
		SELECT m.scenario_id, m.content, m.is_checked, m.mission_order, :yesterday, m.mission_type,
		       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
		FROM mission m
		WHERE m.use_date IS NULL
		  AND m.mission_type = 'BASIC'
		""", nativeQuery = true)
	int bulkCloneBasicToYesterday(LocalDate yesterday);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		DELETE FROM Mission m
		WHERE m.useDate IS NOT NULL
		  AND m.useDate < :expireBefore
		""")
	int bulkDeleteExpired(LocalDate expireBefore);

}
