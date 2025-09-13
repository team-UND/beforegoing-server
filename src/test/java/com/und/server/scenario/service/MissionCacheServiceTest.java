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
import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MissionCacheServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@InjectMocks
	private MissionCacheService missionCacheService;

	@Test
	void Given_CursorReturnsKeys_When_EvictAllMissionCache_Then_DeleteCalled() throws Exception {
		Cursor<byte[]> cursor = mock(Cursor.class);
		when(cursor.hasNext()).thenReturn(true, false);
		when(cursor.next()).thenReturn("missions::1:10".getBytes(StandardCharsets.UTF_8));

		RedisKeyCommands keyCommands = mock(RedisKeyCommands.class);
		when(keyCommands.scan(any(ScanOptions.class))).thenReturn(cursor);

		RedisConnection connection = mock(RedisConnection.class);
		when(connection.keyCommands()).thenReturn(keyCommands);

		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenAnswer(invocation -> {
				RedisCallback<Set<String>> cb = invocation.getArgument(0);
				return cb.doInRedis(connection);
			});

		missionCacheService.evictAllMissionCache();

		verify(redisTemplate).delete(Set.of("missions::1:10"));
	}

	@Test
	void Given_EmptyCursor_When_EvictAllMissionCache_Then_SkipDelete() throws Exception {
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

		missionCacheService.evictAllMissionCache();

		verify(redisTemplate, never()).delete(any(Set.class));
	}

	@Test
	void Given_KeysReturnedFromRedisCallback_When_EvictAllMissionCache_Then_DeleteCalled() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenReturn(Set.of("missions::1:10"));

		missionCacheService.evictAllMissionCache();

		verify(redisTemplate).delete(Set.of("missions::1:10"));
	}

	@Test
	void Given_EmptyKeysFromRedisCallback_When_EvictAllMissionCache_Then_SkipDelete() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenReturn(Set.of());

		missionCacheService.evictAllMissionCache();

		verify(redisTemplate, never()).delete(any(Set.class));
	}

	@Test
	void Given_NullKeysFromRedisCallback_When_EvictAllMissionCache_Then_SkipDelete() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenReturn(null);

		missionCacheService.evictAllMissionCache();

		verify(redisTemplate, never()).delete(any(Set.class));
	}

	@Test
	void Given_RedisExecuteThrowsException_When_EvictAllMissionCache_Then_NoExceptionThrown() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenThrow(new RuntimeException("scan fail"));

		assertDoesNotThrow(() -> missionCacheService.evictAllMissionCache());
	}

	@Test
	void Given_KeysReturnedFromRedisCallback_When_EvictUserMissionCacheByMember_Then_DeleteCalled() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenReturn(Set.of("missions::5:*"));

		missionCacheService.evictUserMissionCache(5L);

		verify(redisTemplate).delete(Set.of("missions::5:*"));
	}

	@Test
	void Given_KeysReturnedFromRedisCallback_When_EvictUserMissionCacheByMemberAndScenario_Then_DeleteCalled() {
		when(redisTemplate.execute(ArgumentMatchers.<RedisCallback<Set<String>>>any()))
			.thenReturn(Set.of("missions::5:7:*"));

		missionCacheService.evictUserMissionCache(5L, 7L);

		verify(redisTemplate).delete(Set.of("missions::5:7:*"));
	}

	@Test
	void Given_ValidKey_When_EvictUserMissionCacheWithDate_Then_DeleteCalled() {
		LocalDate today = LocalDate.now();

		missionCacheService.evictUserMissionCache(5L, 7L, today);

		verify(redisTemplate).delete("missions::5:7:" + today);
	}

	@Test
	void Given_DeleteThrowsException_When_EvictUserMissionCacheWithDate_Then_NoExceptionThrown() {
		LocalDate today = LocalDate.now();
		doThrow(new RuntimeException("delete fail"))
			.when(redisTemplate).delete(anyString());

		assertDoesNotThrow(() -> missionCacheService.evictUserMissionCache(5L, 7L, today));
	}

}
