package com.und.server.member.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.member.entity.Member;

class MemberResponseTest {

	@Test
	@DisplayName("Correctly converts a Member entity to a MemberResponse DTO")
	void Given_MemberEntity_When_From_Then_ReturnsCorrectMemberResponse() {
		// given
		final Member member = Member.builder()
			.id(1L)
			.nickname("Chori")
			.kakaoId("1234567890")
			.build();

		// when
		final MemberResponse response = MemberResponse.from(member);

		// then
		assertThat(response.id()).isEqualTo(member.getId());
		assertThat(response.nickname()).isEqualTo(member.getNickname());
		assertThat(response.kakaoId()).isEqualTo(member.getKakaoId());
		assertThat(response.createdAt()).isEqualTo(member.getCreatedAt());
		assertThat(response.updatedAt()).isEqualTo(member.getUpdatedAt());
	}

}
