package com.und.server.notification.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationCacheKeyGeneratorTest {

	private NotificationCacheKeyGenerator notificationCacheKeyGenerator;

	@BeforeEach
	void setUp() {
		notificationCacheKeyGenerator = new NotificationCacheKeyGenerator();
	}


	@Test
	void Given_ValidMemberId_When_GenerateNotificationCacheKey_Then_ReturnCorrectKey() {
		// given
		Long memberId = 1L;

		// when
		String result = notificationCacheKeyGenerator.generateNotificationCacheKey(memberId);

		// then
		assertThat(result).isEqualTo("notif:1");
	}


	@Test
	void Given_ValidMemberId_When_GenerateEtagKey_Then_ReturnCorrectKey() {
		// given
		Long memberId = 1L;

		// when
		String result = notificationCacheKeyGenerator.generateEtagKey(memberId);

		// then
		assertThat(result).isEqualTo("notif:etag:1");
	}


	@Test
	void Given_DifferentMemberIds_When_GenerateNotificationCacheKey_Then_ReturnDifferentKeys() {
		// given
		Long memberId1 = 1L;
		Long memberId2 = 2L;

		// when
		String result1 = notificationCacheKeyGenerator.generateNotificationCacheKey(memberId1);
		String result2 = notificationCacheKeyGenerator.generateNotificationCacheKey(memberId2);

		// then
		assertThat(result1).isEqualTo("notif:1");
		assertThat(result2).isEqualTo("notif:2");
		assertThat(result1).isNotEqualTo(result2);
	}


	@Test
	void Given_DifferentMemberIds_When_GenerateEtagKey_Then_ReturnDifferentKeys() {
		// given
		Long memberId1 = 1L;
		Long memberId2 = 2L;

		// when
		String result1 = notificationCacheKeyGenerator.generateEtagKey(memberId1);
		String result2 = notificationCacheKeyGenerator.generateEtagKey(memberId2);

		// then
		assertThat(result1).isEqualTo("notif:etag:1");
		assertThat(result2).isEqualTo("notif:etag:2");
		assertThat(result1).isNotEqualTo(result2);
	}


	@Test
	void Given_LargeMemberId_When_GenerateNotificationCacheKey_Then_ReturnCorrectKey() {
		// given
		Long memberId = 999999L;

		// when
		String result = notificationCacheKeyGenerator.generateNotificationCacheKey(memberId);

		// then
		assertThat(result).isEqualTo("notif:999999");
	}


	@Test
	void Given_LargeMemberId_When_GenerateEtagKey_Then_ReturnCorrectKey() {
		// given
		Long memberId = 999999L;

		// when
		String result = notificationCacheKeyGenerator.generateEtagKey(memberId);

		// then
		assertThat(result).isEqualTo("notif:etag:999999");
	}


	@Test
	void Given_ZeroMemberId_When_GenerateNotificationCacheKey_Then_ReturnCorrectKey() {
		// given
		Long memberId = 0L;

		// when
		String result = notificationCacheKeyGenerator.generateNotificationCacheKey(memberId);

		// then
		assertThat(result).isEqualTo("notif:0");
	}


	@Test
	void Given_ZeroMemberId_When_GenerateEtagKey_Then_ReturnCorrectKey() {
		// given
		Long memberId = 0L;

		// when
		String result = notificationCacheKeyGenerator.generateEtagKey(memberId);

		// then
		assertThat(result).isEqualTo("notif:etag:0");
	}

}
