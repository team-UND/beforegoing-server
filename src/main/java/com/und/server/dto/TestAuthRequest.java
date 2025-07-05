package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.und.server.oauth.Provider;

public record TestAuthRequest(
	@JsonProperty("provider") Provider provider,
	@JsonProperty("provider_id") String providerId,
	@JsonProperty("nickname") String nickname
) { }
