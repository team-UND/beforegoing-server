package com.und.server.auth.oauth;

import com.und.server.auth.dto.OidcPublicKeys;

public interface OidcProvider {

	String getProviderId(final String token, final OidcPublicKeys oidcPublicKeys);

}
