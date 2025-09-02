package com.und.server.scenario.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
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
	List<Mission> findTodayAndFutureMissions(
		@NotNull Long memberId, @NotNull Long scenarioId, @NotNull LocalDate date);

	@Query("""
		SELECT m FROM Mission m
		LEFT JOIN m.scenario s
		WHERE s.id = :scenarioId
			AND s.member.id = :memberId
			AND m.useDate = :date
		""")
	@NotNull
	List<Mission> findPastMissionsByDate(
		@NotNull Long memberId, @NotNull Long scenarioId, @NotNull LocalDate date);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM Mission m WHERE m.scenario.id = :scenarioId")
	int deleteByScenarioId(Long scenarioId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
    UPDATE Mission p
       SET p.isChecked = COALESCE(
           (SELECT c.isChecked
              FROM Mission c
             WHERE c.parentMissionId = p.id
               AND c.useDate = :today
               AND c.missionType = 'BASIC'
           ), false
       )
     WHERE p.useDate IS NULL
       AND p.missionType = 'BASIC'
    """)
	int bulkResetBasicIsChecked(LocalDate today);

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
	@Query(value = """
		DELETE FROM mission
		WHERE use_date IS NOT NULL
			AND use_date < :expireBefore
		LIMIT :limit
		""", nativeQuery = true)
	int bulkDeleteExpired(LocalDate expireBefore, int limit);

	/// 새롭게 추가한 쿼리들--------------------------------------------

	@Modifying
	@Query("""
    DELETE FROM Mission m
    WHERE m.useDate = :today
      AND m.parentMissionId IS NOT NULL
      AND m.missionType = 'BASIC'
    """)
	int deleteTodayChildBasics(LocalDate today);


	Optional<Mission> findByParentMissionIdAndUseDate(Long parentMissionId, LocalDate useDate);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM Mission m WHERE m.parentMissionId IN :parentMissionIds")
	void deleteByParentMissionIdIn(@NotNull List<Long> parentMissionIds);

}
