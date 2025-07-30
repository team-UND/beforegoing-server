package com.und.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.entity.Member;
import com.und.server.oauth.IdTokenPayload;
import com.und.server.oauth.Provider;
import com.und.server.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@InjectMocks
	private MemberService memberService;

	@Mock
	private MemberRepository memberRepository;

	private final Long memberId = 1L;
	private final String providerId = "test-provider-id";
	private final String nickname = "test-nickname";
	private final Provider provider = Provider.KAKAO;

	@Test
	@DisplayName("Finds an existing member without creating a new one")
	void Given_ExistingMember_When_FindOrCreateMember_Then_ReturnsExistingMember() {
		// given
		final IdTokenPayload payload = new IdTokenPayload(providerId, nickname);
		final Member existingMember = Member.builder()
			.id(memberId)
			.kakaoId(providerId)
			.nickname(nickname)
			.build();

		doReturn(Optional.of(existingMember)).when(memberRepository).findByKakaoId(providerId);

		// when
		final Member foundMember = memberService.findOrCreateMember(provider, payload);

		// then
		verify(memberRepository).findByKakaoId(providerId);
		verify(memberRepository, never()).save(any(Member.class));
		assertThat(foundMember).isEqualTo(existingMember);
	}

	@Test
	@DisplayName("Creates a new member if one does not exist")
	void Given_NonExistingMember_When_FindOrCreateMember_Then_CreatesAndReturnsNewMember() {
		// given
		final IdTokenPayload payload = new IdTokenPayload(providerId, nickname);
		final Member newMember = Member.builder()
			.id(memberId)
			.kakaoId(providerId)
			.nickname(nickname)
			.build();

		doReturn(Optional.empty()).when(memberRepository).findByKakaoId(providerId);
		doReturn(newMember).when(memberRepository).save(any(Member.class));

		// when
		final Member createdMember = memberService.findOrCreateMember(provider, payload);

		// then
		verify(memberRepository).findByKakaoId(providerId);
		verify(memberRepository).save(any(Member.class));
		assertThat(createdMember).isEqualTo(newMember);
	}

	@Test
	@DisplayName("Returns an empty Optional when finding a non-existent member by ID")
	void Given_NonExistingMemberId_When_FindById_Then_ReturnsEmptyOptional() {
		// given
		doReturn(Optional.empty()).when(memberRepository).findById(memberId);

		// when
		final Optional<Member> foundMemberOptional = memberService.findById(memberId);

		// then
		assertThat(foundMemberOptional).isEmpty();
	}
}
