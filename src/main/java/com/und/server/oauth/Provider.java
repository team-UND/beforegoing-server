package com.und.server.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {

	KAKAO("kakao");

	private final String name;

}
