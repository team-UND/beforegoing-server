package com.und.server.scenario.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import com.und.server.notification.constants.NotificationType;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ScenarioCacheServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@InjectMocks
	private ScenarioCacheService scenarioCacheService;

	@Test
	void Given_CursorReturnsKeys_When_EvictAllScenarioCache_Then_DeleteCalled() throws Exception {
		// mock cursor
		Cursor<byte[]> cursor = mock(Cursor.class);
		when(cursor.hasNext()).thenReturn(true, false); // 1개만 반환
		when(cursor.next()).thenReturn("scenarios::1:TIME".getBytes(StandardCharsets.UTF_8));

		// mock RedisKeyCommands
		RedisKeyCommands keyCommands = mock(RedisKeyCommands.class);
		when(keyCommands.scan(any(ScanOptions.class))).thenReturn(cursor);

		// mock RedisConnection
		RedisConnection connection = mock(RedisConnection.class);
		when(connection.keyCommands()).thenReturn(keyCommands);

		// stub redisTemplate.execute(...)
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenAnswer(invocation -> {
				RedisCallback<Set<String>> cb = invocation.getArgument(0);
				return cb.doInRedis(connection);
			});

		// when
		scenarioCacheService.evictAllScenarioCache();

		// then
		verify(redisTemplate).delete(Set.of("scenarios::1:TIME"));
	}

	@Test
	void Given_EmptyCursor_When_EvictAllScenarioCache_Then_SkipDelete() throws Exception {
		Cursor<byte[]> cursor = mock(Cursor.class);
		when(cursor.hasNext()).thenReturn(false);

		RedisKeyCommands keyCommands = mock(RedisKeyCommands.class);
		when(keyCommands.scan(any())).thenReturn(cursor);

		RedisConnection connection = mock(RedisConnection.class);
		when(connection.keyCommands()).thenReturn(keyCommands);

		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenAnswer(invocation -> {
				RedisCallback<Set<String>> cb = invocation.getArgument(0);
				return cb.doInRedis(connection);
			});

		scenarioCacheService.evictAllScenarioCache();

		verify(redisTemplate, never()).delete(any(Set.class));
	}

	@Test
	void Given_KeysReturnedFromRedisCallback_When_EvictAllScenarioCache_Then_DeleteCalled() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenReturn(Set.of("scenarios::1:TIME"));

		scenarioCacheService.evictAllScenarioCache();

		verify(redisTemplate).delete(Set.of("scenarios::1:TIME"));
	}

	@Test
	void Given_EmptyKeysFromRedisCallback_When_EvictAllScenarioCache_Then_SkipDelete() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenReturn(Set.of());

		scenarioCacheService.evictAllScenarioCache();

		verify(redisTemplate, never()).delete(any(Set.class));
	}

	@Test
	void Given_NullKeysFromRedisCallback_When_EvictAllScenarioCache_Then_SkipDelete() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenReturn(null);

		scenarioCacheService.evictAllScenarioCache();

		verify(redisTemplate, never()).delete(any(Set.class));
	}

	@Test
	void Given_RedisExecuteThrowsException_When_EvictAllScenarioCache_Then_NoExceptionThrown() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenThrow(new RuntimeException("scan fail"));

		assertDoesNotThrow(() -> scenarioCacheService.evictAllScenarioCache());
	}

	@Test
	void Given_KeysReturnedFromRedisCallback_When_EvictUserScenarioCache_Then_DeleteCalled() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenReturn(Set.of("scenarios::99:TIME"));

		scenarioCacheService.evictUserScenarioCache(99L);

		verify(redisTemplate).delete(Set.of("scenarios::99:TIME"));
	}

	@Test
	void Given_ValidKey_When_EvictUserScenarioCacheWithType_Then_DeleteCalled() {
		scenarioCacheService.evictUserScenarioCache(100L, NotificationType.TIME);

		verify(redisTemplate).delete("scenarios::100:TIME");
	}

	@Test
	void Given_DeleteThrowsException_When_EvictUserScenarioCacheWithType_Then_NoExceptionThrown() {
		doThrow(new RuntimeException("delete fail"))
			.when(redisTemplate).delete(anyString());

		assertDoesNotThrow(() ->
			scenarioCacheService.evictUserScenarioCache(100L, NotificationType.TIME));
	}

}
