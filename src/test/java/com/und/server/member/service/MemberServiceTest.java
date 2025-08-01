package com.und.server.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.auth.oauth.IdTokenPayload;
import com.und.server.auth.oauth.Provider;
import com.und.server.auth.service.RefreshTokenService;
import com.und.server.member.dto.MemberResponse;
import com.und.server.member.entity.Member;
import com.und.server.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@InjectMocks
	private MemberService memberService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private RefreshTokenService refreshTokenService;

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
	void Given_NonExistingMemberId_When_FindMemberById_Then_ReturnsEmptyOptional() {
		// given
		doReturn(Optional.empty()).when(memberRepository).findById(memberId);

		// when
		final Optional<Member> foundMemberOptional = memberService.findMemberById(memberId);

		// then
		assertThat(foundMemberOptional).isEmpty();
	}

	@Test
	@DisplayName("Retrieves all members and returns them as a list of MemberResponse DTOs")
	void Given_ExistingMembers_When_GetMemberList_Then_ReturnsListOfMemberResponses() {
		// given
		final Member member1 = Member.builder().id(1L).kakaoId("kakao1").nickname("user1").build();
		final Member member2 = Member.builder().id(2L).kakaoId("kakao2").nickname("user2").build();
		doReturn(List.of(member1, member2)).when(memberRepository).findAll();

		// when
		final List<MemberResponse> memberList = memberService.getMemberList();

		// then
		assertThat(memberList).hasSize(2);
		assertThat(memberList.get(0).id()).isEqualTo(1L);
		assertThat(memberList.get(1).id()).isEqualTo(2L);
		verify(memberRepository).findAll();
	}

	@Test
	@DisplayName("Deletes a member and their refresh token by ID")
	void Given_MemberId_When_DeleteMemberById_Then_DeletesMemberAndRefreshToken() {
		// given
		final Long memberIdToDelete = 1L;

		// when
		memberService.deleteMemberById(memberIdToDelete);

		// then
		verify(memberRepository).deleteById(memberIdToDelete);
		verify(refreshTokenService).deleteRefreshToken(memberIdToDelete);
	}

}
