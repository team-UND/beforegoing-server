package com.und.server.terms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.und.server.terms.entity.Terms;

public interface TermsRepository extends JpaRepository<Terms, Long> {

	@Override
	@EntityGraph(attributePaths = {"member"})
	List<Terms> findAll();

	Optional<Terms> findByMemberId(Long memberId);

	boolean existsByMemberId(Long memberId);

}
