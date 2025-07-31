package com.und.server.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.auth.oauth.IdTokenPayload;
import com.und.server.auth.oauth.Provider;
import com.und.server.member.entity.Member;
import com.und.server.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional
	public Member findOrCreateMember(final Provider provider, final IdTokenPayload payload) {
		final String providerId = payload.providerId();
		return findMemberByProviderId(provider, providerId)
			.orElseGet(() -> createMember(provider, providerId, payload.nickname()));
	}

	public Optional<Member> findMemberById(final Long memberId) {
		return memberRepository.findById(memberId);
	}

	private Optional<Member> findMemberByProviderId(final Provider provider, final String providerId) {
		return switch (provider) {
			case KAKAO -> memberRepository.findByKakaoId(providerId);
		};
	}

	private Member createMember(final Provider provider, final String providerId, final String nickname) {
		final Member.MemberBuilder memberBuilder = Member.builder().nickname(nickname);
		switch (provider) {
			case KAKAO -> memberBuilder.kakaoId(providerId);
		}
		return memberRepository.save(memberBuilder.build());
	}

}
