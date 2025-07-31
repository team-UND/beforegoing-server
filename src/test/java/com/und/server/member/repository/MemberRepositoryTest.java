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
	@DisplayName("Saves a member and verifies its properties")
	void Given_MemberDetails_When_SaveMember_Then_MemberIsPersistedWithCorrectDetails() {
		// given
		final Member member = Member.builder()
			.nickname("Chori")
			.kakaoId("166959")
			.build();

		// when
		final Member result = memberRepository.save(member);

		// then
		assertThat(result.getId()).isNotNull();
		assertThat(result.getNickname()).isEqualTo("Chori");
		assertThat(result.getKakaoId()).isEqualTo("166959");
		assertThat(result.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("Finds a member by their Kakao ID")
	void Given_ExistingMember_When_FindByKakaoId_Then_ReturnsCorrectMember() {
		// given
		final Member member = Member.builder()
			.nickname("Chori")
			.kakaoId("166959")
			.build();
		memberRepository.save(member);

		// when
		final Optional<Member> foundMember = memberRepository.findByKakaoId("166959");

		// then
		assertThat(foundMember).isPresent().hasValueSatisfying(result -> {
			assertThat(result.getId()).isNotNull();
			assertThat(result.getNickname()).isEqualTo("Chori");
			assertThat(result.getKakaoId()).isEqualTo("166959");
			assertThat(result.getCreatedAt()).isNotNull();
		});
	}

}
