package com.und.server.member.dto;

import java.time.LocalDateTime;

import com.und.server.member.entity.Member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Member Response DTO")
public record MemberResponse(
	@Schema(description = "Member ID", example = "1")
	Long id,

	@Schema(description = "Member's nickname", example = "Chori")
	String nickname,

	@Schema(description = "Kakao ID", example = "1234567890")
	String kakaoId,

	@Schema(description = "Apple ID", example = "1234567890")
	String appleId,

	@Schema(description = "Creation timestamp of the member", example = "2025-07-31T22:27:36.037717")
	LocalDateTime createdAt,

	@Schema(description = "Last update timestamp of the member", example = "2025-07-31T22:27:36.037744")
	LocalDateTime updatedAt
) {
	public static MemberResponse from(final Member member) {
		return new MemberResponse(
			member.getId(),
			member.getNickname(),
			member.getKakaoId(),
			member.getAppleId(),
			member.getCreatedAt(),
			member.getUpdatedAt()
		);
	}
}
