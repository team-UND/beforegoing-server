package com.und.server.scenario.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
