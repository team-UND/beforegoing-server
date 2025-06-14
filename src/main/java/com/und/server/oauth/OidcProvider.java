package com.und.server.oauth;

import com.und.server.dto.OidcPublicKeys;

public interface OidcProvider {

	String getOidcProviderId(String token, OidcPublicKeys oidcPublicKeys);

}
