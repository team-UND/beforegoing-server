package com.und.server.terms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.und.server.terms.entity.Terms;

public interface TermsRepository extends JpaRepository<Terms, Long> {

	Optional<Terms> findByMemberId(Long memberId);

	boolean existsByMemberId(Long memberId);

}
