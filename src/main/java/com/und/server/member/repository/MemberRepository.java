package com.und.server.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.und.server.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByKakaoId(final String kakaoId);

	Optional<Member> findByAppleId(final String appleId);

}
