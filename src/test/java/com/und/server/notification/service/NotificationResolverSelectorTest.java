package com.und.server.notification.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import com.und.server.notification.exception.NotificationErrorResult;
import com.und.server.scenario.dto.NotificationInfoDto;

@ExtendWith(MockitoExtension.class)
class NotificationResolverSelectorTest {

	@InjectMocks
	private NotificationResolverSelector selector;

	@Mock
	private TImeNotifResolver timeNotifResolver;

	@BeforeEach
	void setUp() {
		selector = new NotificationResolverSelector(List.of(timeNotifResolver));
	}


	@Test
	void Given_SupportedNotifType_When_Resolve_Then_ReturnNotificationInfoDto() {
		// given
		Notification notification = Notification.builder()
			.id(1L)
			.isActive(true)
			.notifType(NotifType.TIME)
			.build();

		NotificationInfoDto expected = new NotificationInfoDto(
			true,
			List.of(),
			TimeNotifResponse.builder().hour(9).minute(30).build()
		);

		Mockito.when(timeNotifResolver.supports(NotifType.TIME)).thenReturn(true);
		Mockito.when(timeNotifResolver.resolve(notification)).thenReturn(expected);

		// when
		NotificationInfoDto result = selector.resolve(notification);

		// then
		assertThat(result).isNotNull();
		assertThat(result.notificationDetail()).isInstanceOf(TimeNotifResponse.class);
		TimeNotifResponse detail = (TimeNotifResponse) result.notificationDetail();
		assertThat(detail.getHour()).isEqualTo(9);
	}


	@Test
	void Given_UnsupportedNotifType_When_Resolve_Then_ThrowException() {
		// given
		Notification notification = Notification.builder()
			.id(2L)
			.isActive(true)
			.notifType(null)
			.build();

		Mockito.when(timeNotifResolver.supports(null)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> selector.resolve(notification))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(NotificationErrorResult.UNSUPPORTED_NOTIF.getMessage());
	}

}
