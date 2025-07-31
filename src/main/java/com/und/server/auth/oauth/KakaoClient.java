package com.und.server.auth.oauth;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.und.server.auth.dto.OidcPublicKeys;

@FeignClient(name = "KakaoClient", url = "${oauth.kakao.base-url}")
public interface KakaoClient extends OidcClient {

	@Override
	@Cacheable(cacheNames = "OidcKakao", cacheManager = "oidcCacheManager")
	@GetMapping("${oauth.kakao.public-key-url}")
	OidcPublicKeys getOidcPublicKeys();

}
