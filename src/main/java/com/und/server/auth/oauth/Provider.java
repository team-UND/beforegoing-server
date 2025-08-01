package com.und.server.auth.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {

	KAKAO("kakao"),
	APPLE("apple");

	private final String name;

}
