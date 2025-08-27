package com.und.server.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Nickname Request DTO")
public record NicknameRequest(
	@Schema(description = "Member's nickname", example = "Chori")
	@NotBlank(message = "Nickname must not be blank") String nickname
) { }
