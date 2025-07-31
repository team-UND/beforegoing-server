package com.und.server.scenario.repository;

import com.und.server.scenario.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

	List<Scenario> findByMemberIdOrderByOrder(Long memberId);

}
