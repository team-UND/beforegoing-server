package com.und.server.auth.oauth;

import com.und.server.auth.dto.OidcPublicKeys;

public interface OidcClient {

	OidcPublicKeys getOidcPublicKeys();

}
