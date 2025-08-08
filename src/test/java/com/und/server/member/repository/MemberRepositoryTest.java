package com.und.server.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.und.server.member.entity.Member;

@DataJpaTest
class MemberRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@Test
	@DisplayName("Saves a member with a Kakao ID and verifies its properties")
	void Given_MemberWithKakaoId_When_SaveMember_Then_MemberIsPersistedWithCorrectDetails() {
		// given
		final Member member = Member.builder()
			.nickname("KakaoUser")
			.kakaoId("kakao-id-123")
			.build();

		// when
		final Member result = memberRepository.save(member);

		// then
		assertThat(result.getId()).isNotNull();
		assertThat(result.getNickname()).isEqualTo("KakaoUser");
		assertThat(result.getKakaoId()).isEqualTo("kakao-id-123");
		assertThat(result.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("Finds a member by their Kakao ID")
	void Given_ExistingMemberWithKakaoId_When_FindByKakaoId_Then_ReturnsCorrectMember() {
		// given
		final Member member = Member.builder()
			.nickname("KakaoUser")
			.kakaoId("kakao-id-123")
			.build();
		memberRepository.save(member);

		// when
		final Optional<Member> foundMember = memberRepository.findByKakaoId("kakao-id-123");

		// then
		assertThat(foundMember).isPresent().hasValueSatisfying(result -> {
			assertThat(result.getId()).isNotNull();
			assertThat(result.getNickname()).isEqualTo("KakaoUser");
			assertThat(result.getKakaoId()).isEqualTo("kakao-id-123");
			assertThat(result.getCreatedAt()).isNotNull();
		});
	}

	@Test
	@DisplayName("Saves a member with an Apple ID and verifies its properties")
	void Given_MemberWithAppleId_When_SaveMember_Then_MemberIsPersistedWithCorrectDetails() {
		// given
		final Member member = Member.builder()
			.nickname("AppleUser")
			.appleId("apple-id-123")
			.build();

		// when
		final Member result = memberRepository.save(member);

		// then
		assertThat(result.getId()).isNotNull();
		assertThat(result.getNickname()).isEqualTo("AppleUser");
		assertThat(result.getAppleId()).isEqualTo("apple-id-123");
		assertThat(result.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("Finds a member by their Apple ID")
	void Given_ExistingMemberWithAppleId_When_FindByAppleId_Then_ReturnsCorrectMember() {
		// given
		final Member member = Member.builder()
			.nickname("AppleUser")
			.appleId("apple-id-123")
			.build();
		memberRepository.save(member);

		// when
		final Optional<Member> foundMember = memberRepository.findByAppleId("apple-id-123");

		// then
		assertThat(foundMember).isPresent().hasValueSatisfying(result -> {
			assertThat(result.getId()).isNotNull();
			assertThat(result.getNickname()).isEqualTo("AppleUser");
			assertThat(result.getAppleId()).isEqualTo("apple-id-123");
			assertThat(result.getCreatedAt()).isNotNull();
		});
	}

}
