package com.und.server.member.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.und.server.member.entity.Member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Member Response DTO")
public record MemberResponse(
	@Schema(description = "Member ID", example = "1")
	@JsonProperty("id") Long id,

	@Schema(description = "Member's nickname", example = "Chori")
	@JsonProperty("nickname") String nickname,

	@Schema(description = "Kakao ID", example = "1234567890")
	@JsonProperty("kakao_id") String kakaoId,

	@Schema(description = "Creation timestamp of the member", example = "2025-07-31T22:27:36.037717")
	@JsonProperty("created_at") LocalDateTime createdAt,

	@Schema(description = "Last update timestamp of the member", example = "2025-07-31T22:27:36.037744")
	@JsonProperty("updated_at") LocalDateTime updatedAt
) {
	public static MemberResponse from(final Member member) {
		return new MemberResponse(
			member.getId(),
			member.getNickname(),
			member.getKakaoId(),
			member.getCreatedAt(),
			member.getUpdatedAt()
		);
	}
}
