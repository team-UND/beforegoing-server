package com.und.server.scenario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.und.server.scenario.entity.Mission;

public interface MissionRepository extends JpaRepository<Mission, Long> {

	@EntityGraph(attributePaths = {"scenario"})
	List<Mission> findAllByScenarioIdOrderByOrder(Long scenarioId);

}
