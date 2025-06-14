package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.und.server.oauth.Provider;

import jakarta.validation.constraints.NotNull;

public record AuthRequest(
	@NotNull @JsonProperty("provider") Provider provider,
	@NotNull @JsonProperty("id_token") String idToken
) { }
