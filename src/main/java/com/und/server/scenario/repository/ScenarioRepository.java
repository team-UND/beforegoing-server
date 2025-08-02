package com.und.server.scenario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.und.server.scenario.entity.Scenario;

import jakarta.validation.constraints.NotNull;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

	@EntityGraph(attributePaths = {"notification", "missionList"})
	Optional<Scenario> findById(@NotNull Long id);

	List<Scenario> findByMemberIdOrderByOrder(Long memberId);

}
