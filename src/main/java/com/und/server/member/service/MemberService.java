package com.und.server.member.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.auth.oauth.Provider;
import com.und.server.auth.service.RefreshTokenService;
import com.und.server.common.exception.ServerException;
import com.und.server.member.dto.MemberResponse;
import com.und.server.member.dto.NicknameRequest;
import com.und.server.member.entity.Member;
import com.und.server.member.exception.MemberErrorResult;
import com.und.server.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;

	public List<MemberResponse> getMemberList() {
		return memberRepository.findAll()
			.stream().map(MemberResponse::from).toList();
	}

	@Transactional
	public Member findOrCreateMember(final Provider provider, final String providerId) {
		validateProviderIsNotNull(provider);
		validateProviderIdIsNotNull(providerId);

		return findMemberByProviderId(provider, providerId)
			.orElseGet(() -> createMember(provider, providerId));
	}

	public Member findMemberById(final Long memberId) {
		validateMemberIdIsNotNull(memberId);

		return memberRepository.findById(memberId)
			.orElseThrow(() -> new ServerException(MemberErrorResult.MEMBER_NOT_FOUND));
	}

	public void checkMemberExists(final Long memberId) {
		validateMemberIdIsNotNull(memberId);

		if (!memberRepository.existsById(memberId)) {
			throw new ServerException(MemberErrorResult.MEMBER_NOT_FOUND);
		}
	}

	@Transactional
	public MemberResponse updateNickname(final Long memberId, final NicknameRequest nicknameRequest) {
		final Member member = findMemberById(memberId);
		member.updateNickname(nicknameRequest.nickname());

		return MemberResponse.from(member);
	}

	@Transactional
	public void deleteMemberById(final Long memberId) {
		validateMemberIdIsNotNull(memberId);
		checkMemberExists(memberId);

		refreshTokenService.deleteRefreshToken(memberId);
		memberRepository.deleteById(memberId);
	}

	private Optional<Member> findMemberByProviderId(final Provider provider, final String providerId) {
		return switch (provider) {
			case KAKAO -> memberRepository.findByKakaoId(providerId);
			case APPLE -> memberRepository.findByAppleId(providerId);
		};
	}

	private Member createMember(final Provider provider, final String providerId) {
		final Member.MemberBuilder memberBuilder = Member.builder();
		switch (provider) {
			case KAKAO -> memberBuilder.kakaoId(providerId);
			case APPLE -> memberBuilder.appleId(providerId);
		}

		return memberRepository.save(memberBuilder.build());
	}

	private void validateMemberIdIsNotNull(final Long memberId) {
		if (memberId == null) {
			throw new ServerException(MemberErrorResult.INVALID_MEMBER_ID);
		}
	}

	private void validateProviderIsNotNull(final Provider provider) {
		if (provider == null) {
			throw new ServerException(AuthErrorResult.INVALID_PROVIDER);
		}
	}

	private void validateProviderIdIsNotNull(final String providerId) {
		if (providerId == null) {
			throw new ServerException(AuthErrorResult.INVALID_PROVIDER_ID);
		}
	}

}
