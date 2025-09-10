package com.und.server.notification.event;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.service.NotificationCacheService;

@ExtendWith(MockitoExtension.class)
class ActiveUpdateEventListenerTest {

	@InjectMocks
	private ActiveUpdateEventListener activeUpdateEventListener;

	@Mock
	private NotificationCacheService notificationCacheService;

	private final Long memberId = 1L;


	@Test
	void Given_ActiveUpdateEventWithTrue_When_HandleActiveUpdate_Then_RefreshCacheFromDatabase() {
		// given
		ActiveUpdateEvent event = new ActiveUpdateEvent(memberId, true);

		// when
		activeUpdateEventListener.handleActiveUpdate(event);

		// then
		verify(notificationCacheService).refreshCacheFromDatabase(memberId);
	}


	@Test
	void Given_ActiveUpdateEventWithFalse_When_HandleActiveUpdate_Then_DeleteMemberAllCache() {
		// given
		ActiveUpdateEvent event = new ActiveUpdateEvent(memberId, false);

		// when
		activeUpdateEventListener.handleActiveUpdate(event);

		// then
		verify(notificationCacheService).deleteMemberAllCache(memberId);
	}


	@Test
	void Given_ActiveUpdateEventWithDifferentMemberId_When_HandleActiveUpdate_Then_RefreshCacheFromDatabase() {
		// given
		Long differentMemberId = 2L;
		ActiveUpdateEvent event = new ActiveUpdateEvent(differentMemberId, true);

		// when
		activeUpdateEventListener.handleActiveUpdate(event);

		// then
		verify(notificationCacheService).refreshCacheFromDatabase(differentMemberId);
	}


	@Test
	void Given_ActiveUpdateEventWithDifferentMemberIdAndFalse_When_HandleActiveUpdate_Then_DeleteMemberAllCache() {
		// given
		Long differentMemberId = 3L;
		ActiveUpdateEvent event = new ActiveUpdateEvent(differentMemberId, false);

		// when
		activeUpdateEventListener.handleActiveUpdate(event);

		// then
		verify(notificationCacheService).deleteMemberAllCache(differentMemberId);
	}


	@Test
	void Given_ExceptionOccursWhenActiveTrue_When_HandleActiveUpdate_Then_DeleteMemberAllCache() {
		// given
		ActiveUpdateEvent event = new ActiveUpdateEvent(memberId, true);

		doThrow(new RuntimeException("Cache refresh failed"))
			.when(notificationCacheService).refreshCacheFromDatabase(anyLong());

		// when
		activeUpdateEventListener.handleActiveUpdate(event);

		// then
		verify(notificationCacheService).refreshCacheFromDatabase(memberId);
		verify(notificationCacheService).deleteMemberAllCache(memberId);
	}


	@Test
	void Given_ExceptionOccursWhenActiveFalse_When_HandleActiveUpdate_Then_DeleteMemberAllCache() {
		// given
		ActiveUpdateEvent event = new ActiveUpdateEvent(memberId, false);

		doAnswer(invocation -> {
			throw new RuntimeException("Cache delete failed");
		}).doNothing().when(notificationCacheService).deleteMemberAllCache(anyLong());

		// when
		activeUpdateEventListener.handleActiveUpdate(event);

		// then
		verify(notificationCacheService, times(2)).deleteMemberAllCache(memberId);
	}

}
