package com.und.server.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.und.server.auth.exception.AuthErrorResult;
import com.und.server.auth.oauth.Provider;
import com.und.server.auth.service.RefreshTokenService;
import com.und.server.common.exception.ServerException;
import com.und.server.member.dto.MemberResponse;
import com.und.server.member.dto.NicknameRequest;
import com.und.server.member.entity.Member;
import com.und.server.member.exception.MemberErrorResult;
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

	@Test
	@DisplayName("Finds an existing member with a Kakao ID")
	void Given_ExistingKakaoMember_When_FindOrCreateMember_Then_ReturnsExistingMember() {
		// given
		final Provider kakaoProvider = Provider.KAKAO;
		final Member existingMember = Member.builder()
			.id(memberId)
			.kakaoId(providerId)
			.nickname(nickname)
			.build();

		doReturn(Optional.of(existingMember)).when(memberRepository).findByKakaoId(providerId);

		// when
		final Member foundMember = memberService.findOrCreateMember(kakaoProvider, providerId);

		// then
		verify(memberRepository).findByKakaoId(providerId);
		verify(memberRepository, never()).save(any(Member.class));
		assertThat(foundMember).isEqualTo(existingMember);
	}

	@Test
	@DisplayName("Creates a new member with a Kakao ID")
	void Given_NonExistingKakaoMember_When_FindOrCreateMember_Then_CreatesAndReturnsNewMember() {
		// given
		final Provider kakaoProvider = Provider.KAKAO;
		final Member newMember = Member.builder()
			.id(memberId)
			.kakaoId(providerId)
			.nickname(nickname)
			.build();

		doReturn(Optional.empty()).when(memberRepository).findByKakaoId(providerId);
		doReturn(newMember).when(memberRepository).save(any(Member.class));

		// when
		final Member createdMember = memberService.findOrCreateMember(kakaoProvider, providerId);

		// then
		verify(memberRepository).findByKakaoId(providerId);
		verify(memberRepository).save(any(Member.class));
		assertThat(createdMember).isEqualTo(newMember);
	}

	@Test
	@DisplayName("Finds an existing member with an Apple ID")
	void Given_ExistingAppleMember_When_FindOrCreateMember_Then_ReturnsExistingMember() {
		// given
		final Provider appleProvider = Provider.APPLE;
		final Member existingMember = Member.builder()
			.id(memberId)
			.appleId(providerId)
			.nickname(nickname)
			.build();

		doReturn(Optional.of(existingMember)).when(memberRepository).findByAppleId(providerId);

		// when
		final Member foundMember = memberService.findOrCreateMember(appleProvider, providerId);

		// then
		verify(memberRepository).findByAppleId(providerId);
		verify(memberRepository, never()).save(any(Member.class));
		assertThat(foundMember).isEqualTo(existingMember);
	}

	@Test
	@DisplayName("Creates a new member with an Apple ID")
	void Given_NonExistingAppleMember_When_FindOrCreateMember_Then_CreatesAndReturnsNewMember() {
		// given
		final Provider appleProvider = Provider.APPLE;
		final Member newMember = Member.builder()
			.id(memberId)
			.appleId(providerId)
			.nickname(nickname)
			.build();

		doReturn(Optional.empty()).when(memberRepository).findByAppleId(providerId);
		doReturn(newMember).when(memberRepository).save(any(Member.class));

		// when
		final Member createdMember = memberService.findOrCreateMember(appleProvider, providerId);

		// then
		verify(memberRepository).findByAppleId(providerId);
		verify(memberRepository).save(any(Member.class));
		assertThat(createdMember).isEqualTo(newMember);
	}

	@Test
	@DisplayName("Throws an exception when finding or creating a member with a null provider")
	void Given_NullProvider_When_FindOrCreateMember_Then_ThrowsException() {
		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> memberService.findOrCreateMember(null, providerId));

		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_PROVIDER);
	}

	@Test
	@DisplayName("Throws an exception when finding or creating a member with a null provider ID")
	void Given_NullProviderId_When_FindOrCreateMember_Then_ThrowsException() {
		// when & then
		final Provider provider = Provider.KAKAO;
		final ServerException exception = assertThrows(ServerException.class,
			() -> memberService.findOrCreateMember(provider, null));

		assertThat(exception.getErrorResult()).isEqualTo(AuthErrorResult.INVALID_PROVIDER_ID);
	}

	@Test
	@DisplayName("Throws an exception when finding a non-existent member by ID")
	void Given_NonExistingMemberId_When_FindMemberById_Then_ThrowsException() {
		// given
		doReturn(Optional.empty()).when(memberRepository).findById(memberId);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> memberService.findMemberById(memberId));

		assertThat(exception.getErrorResult()).isEqualTo(MemberErrorResult.MEMBER_NOT_FOUND);
	}

	@Test
	@DisplayName("Throws an exception when finding a member with a null ID")
	void Given_NullMemberId_When_FindMemberById_Then_ThrowsException() {
		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> memberService.findMemberById(null));

		assertThat(exception.getErrorResult()).isEqualTo(MemberErrorResult.INVALID_MEMBER_ID);
	}

	@Test
	@DisplayName("Throws an exception when validating a null member ID")
	void Given_NullMemberId_When_CheckMemberExists_Then_ThrowsException() {
		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> memberService.checkMemberExists(null));
		assertThat(exception.getErrorResult()).isEqualTo(MemberErrorResult.INVALID_MEMBER_ID);
	}

	@Test
	@DisplayName("Throws an exception when validating a non-existent member")
	void Given_NonExistentMemberId_When_CheckMemberExists_Then_ThrowsException() {
		// given
		doReturn(false).when(memberRepository).existsById(memberId);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> memberService.checkMemberExists(memberId));
		assertThat(exception.getErrorResult()).isEqualTo(MemberErrorResult.MEMBER_NOT_FOUND);
	}

	@Test
	@DisplayName("Does not throw an exception when validating an existing member")
	void Given_ExistingMemberId_When_CheckMemberExists_Then_Succeeds() {
		// given
		doReturn(true).when(memberRepository).existsById(memberId);

		// when & then
		assertDoesNotThrow(() -> memberService.checkMemberExists(memberId));
		verify(memberRepository).existsById(memberId);
	}

	@Test
	@DisplayName("Updates a member's nickname successfully")
	void Given_ValidNickname_When_UpdateNickname_Then_SucceedsAndReturnsUpdatedResponse() {
		// given
		final String newNickname = "new-nickname";
		final NicknameRequest request = new NicknameRequest(newNickname);
		final Member member = Member.builder()
			.id(memberId)
			.kakaoId(providerId)
			.nickname("old-nickname")
			.build();

		doReturn(Optional.of(member)).when(memberRepository).findById(memberId);

		// when
		final MemberResponse response = memberService.updateNickname(memberId, request);

		// then
		verify(memberRepository).findById(memberId);
		assertThat(member.getNickname()).isEqualTo(newNickname);
		assertThat(response.id()).isEqualTo(memberId);
		assertThat(response.nickname()).isEqualTo(newNickname);
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
	@DisplayName("Deletes a member and their refresh token by ID when the member exists")
	void Given_ExistingMemberId_When_DeleteMemberById_Then_DeletesMemberAndRefreshToken() {
		// given
		final Long memberIdToDelete = 1L;
		doReturn(true).when(memberRepository).existsById(memberIdToDelete);

		// when
		memberService.deleteMemberById(memberIdToDelete);

		// then
		verify(memberRepository).existsById(memberIdToDelete);
		verify(refreshTokenService).deleteRefreshToken(memberIdToDelete);
		verify(memberRepository).deleteById(memberIdToDelete);
	}

	@Test
	@DisplayName("Throws an exception when deleting a non-existent member")
	void Given_NonExistentMemberId_When_DeleteMemberById_Then_ThrowsException() {
		// given
		final Long memberIdToDelete = 1L;
		doReturn(false).when(memberRepository).existsById(memberIdToDelete);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> memberService.deleteMemberById(memberIdToDelete));

		assertThat(exception.getErrorResult()).isEqualTo(MemberErrorResult.MEMBER_NOT_FOUND);
		verify(refreshTokenService, never()).deleteRefreshToken(any());
		verify(memberRepository, never()).deleteById(any());
	}

}
