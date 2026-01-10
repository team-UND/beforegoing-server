package com.und.server.member.dto;

import com.und.server.member.entity.Member;

public record MemberCreationResult(
	Member member,
	boolean isNewMember
) { }
