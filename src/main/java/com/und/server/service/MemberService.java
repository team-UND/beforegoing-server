package com.und.server.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.entity.Member;
import com.und.server.exception.ServerErrorResult;
import com.und.server.exception.ServerException;
import com.und.server.oauth.IdTokenPayload;
import com.und.server.oauth.Provider;
import com.und.server.repository.MemberRepository;

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

	public Optional<Member> findById(final Long memberId) {
		return memberRepository.findById(memberId);
	}

	private Optional<Member> findMemberByProviderId(final Provider provider, final String providerId) {
		return switch (provider) {
			case KAKAO -> memberRepository.findByKakaoId(providerId);
			default -> throw new ServerException(ServerErrorResult.INVALID_PROVIDER);
		};
	}

	private Member createMember(final Provider provider, final String providerId, final String nickname) {
		return memberRepository.save(Member.builder()
			.kakaoId(provider == Provider.KAKAO ? providerId : null)
			.nickname(nickname)
			.build());
	}
}
