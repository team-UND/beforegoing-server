package com.und.server.notification.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.DayOfWeek;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.notification.constants.NotifType;
import com.und.server.notification.dto.TimeNotifResponse;
import com.und.server.notification.entity.Notification;
import com.und.server.notification.entity.TimeNotif;
import com.und.server.notification.exception.NotificationErrorResult;
import com.und.server.notification.repository.TimeNotifRepository;
import com.und.server.scenario.dto.NotificationInfoDto;

@ExtendWith(MockitoExtension.class)
class TImeNotifResolverTest {

	@InjectMocks
	private TImeNotifResolver timeNotifResolver;

	@Mock
	private TimeNotifRepository timeNotifRepository;


	@Test
	void Given_NotifTypeTime_When_Supports_Then_ReturnTrue() {
		assertThat(timeNotifResolver.supports(NotifType.TIME)).isTrue();
	}


	@Test
	void Given_OtherNotifType_When_Supports_Then_ReturnFalse() {
		assertThat(timeNotifResolver.supports(null)).isFalse();
	}


	@Test
	void Given_ValidTimeNotifList_When_Resolve_Then_ReturnNotificationInfoDto() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.notifType(NotifType.TIME)
			.isActive(true)
			.build();

		TimeNotif timeNotif = TimeNotif.builder()
			.id(10L)
			.notification(notification)
			.hour(9)
			.minute(30)
			.dayOfWeek(DayOfWeek.MONDAY)
			.build();

		List<TimeNotif> timeNotifList = List.of(timeNotif);

		Mockito.when(timeNotifRepository.findByNotificationId(1L))
			.thenReturn(timeNotifList);

		// when
		NotificationInfoDto result = timeNotifResolver.resolve(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result.isEveryDay()).isFalse();
		assertThat(result.dayOfWeekOrdinalList()).hasSize(1);
		assertThat(result.notificationDetail()).isInstanceOf(TimeNotifResponse.class);

		TimeNotifResponse detail = (TimeNotifResponse) result.notificationDetail();
		assertThat(detail.getHour()).isEqualTo(9);
		assertThat(detail.getMinute()).isEqualTo(30);
	}


	@Test
	void Given_EmptyTimeNotifList_When_Resolve_Then_ThrowException() {
		// given
		Notification notification = Notification.builder()
			.id(2L)
			.notifType(NotifType.TIME)
			.build();

		Mockito.when(timeNotifRepository.findByNotificationId(2L))
			.thenReturn(List.of());

		// when & then
		assertThatThrownBy(() -> timeNotifResolver.resolve(notification))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(NotificationErrorResult.NOT_FOUND_NOTIF.getMessage());
	}


	@Test
	void Given_NullDayOfWeek_When_Resolve_Then_IsEveryDayTrueAndNoDayOfWeekList() {
		// given
		Notification notification = Notification.builder()
			.id(3L)
			.notifType(NotifType.TIME)
			.build();

		TimeNotif timeNotif = TimeNotif.builder()
			.id(30L)
			.notification(notification)
			.hour(8)
			.minute(0)
			.dayOfWeek(null) // 핵심 포인트
			.build();

		Mockito.when(timeNotifRepository.findByNotificationId(3L))
			.thenReturn(List.of(timeNotif));

		// when
		NotificationInfoDto result = timeNotifResolver.resolve(notification);

		// then
		assertThat(result.isEveryDay()).isTrue();
		assertThat(result.dayOfWeekOrdinalList()).isEmpty();
	}

}
