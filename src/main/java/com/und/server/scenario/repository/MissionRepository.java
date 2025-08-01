package com.und.server.scenario.repository;

import com.und.server.scenario.entity.Mission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

	@EntityGraph(attributePaths = {"scenario"})
	List<Mission> findAllByScenarioIdOrderByOrder(Long scenarioId);

}
