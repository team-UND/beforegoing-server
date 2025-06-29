package com.und.server.oauth;

import com.und.server.dto.OidcPublicKeys;

public interface OidcProvider {

	IdTokenPayload getIdTokenPayload(String token, OidcPublicKeys oidcPublicKeys);

}
