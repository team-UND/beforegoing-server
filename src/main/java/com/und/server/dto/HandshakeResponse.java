package com.und.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HandshakeResponse(
	@JsonProperty ("nonce") String nonce
) { }
