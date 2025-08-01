package com.und.server.scenario.repository;

import com.und.server.scenario.entity.Scenario;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

	@EntityGraph(attributePaths = {"notification", "missionList"})
	Optional<Scenario> findById(@NotNull Long id);

	List<Scenario> findByMemberIdOrderByOrder(Long memberId);

}
