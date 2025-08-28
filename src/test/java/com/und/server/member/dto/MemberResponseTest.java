package com.und.server.member.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.member.dto.response.MemberResponse;
import com.und.server.member.entity.Member;

class MemberResponseTest {

	@Test
	@DisplayName("Correctly converts a Kakao Member entity to a MemberResponse DTO")
	void Given_KakaoMemberEntity_When_From_Then_ReturnsCorrectMemberResponse() {
		// given
		final Member member = Member.builder()
			.id(1L)
			.nickname("KakaoUser")
			.kakaoId("1234567890")
			.build();

		// when
		final MemberResponse response = MemberResponse.from(member);

		// then
		assertThat(response.id()).isEqualTo(member.getId());
		assertThat(response.nickname()).isEqualTo(member.getNickname());
		assertThat(response.kakaoId()).isEqualTo(member.getKakaoId());
		assertThat(response.appleId()).isNull();
		assertThat(response.createdAt()).isEqualTo(member.getCreatedAt());
		assertThat(response.updatedAt()).isEqualTo(member.getUpdatedAt());
	}

	@Test
	@DisplayName("Correctly converts an Apple Member entity to a MemberResponse DTO")
	void Given_AppleMemberEntity_When_From_Then_ReturnsCorrectMemberResponse() {
		// given
		final Member member = Member.builder()
			.id(2L)
			.nickname("AppleUser")
			.appleId("000123.abc.456def")
			.build();

		// when
		final MemberResponse response = MemberResponse.from(member);

		// then
		assertThat(response.id()).isEqualTo(member.getId());
		assertThat(response.nickname()).isEqualTo(member.getNickname());
		assertThat(response.kakaoId()).isNull();
		assertThat(response.appleId()).isEqualTo(member.getAppleId());
		assertThat(response.createdAt()).isEqualTo(member.getCreatedAt());
		assertThat(response.updatedAt()).isEqualTo(member.getUpdatedAt());
	}

}
