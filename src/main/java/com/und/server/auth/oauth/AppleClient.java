package com.und.server.auth.oauth;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.und.server.auth.dto.OidcPublicKeys;

@FeignClient(name = "AppleClient", url = "${oauth.apple.base-url}")
public interface AppleClient extends OidcClient {

	@Override
	@Cacheable(cacheNames = "OidcApple", cacheManager = "oidcCacheManager")
	@GetMapping("${oauth.apple.public-key-url}")
	OidcPublicKeys getOidcPublicKeys();

}
