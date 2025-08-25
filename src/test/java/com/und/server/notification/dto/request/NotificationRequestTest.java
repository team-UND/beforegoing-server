package com.und.server.notification.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.entity.Notification;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class NotificationRequestTest {

	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

	@Test
	void Given_ActiveNotificationWithValidFields_When_Validate_Then_Success() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(0, 1, 2))
			.build();

		// when
		Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isEmpty();
	}

	@Test
	void Given_ActiveNotificationWithoutMethodType_When_Validate_Then_Fail() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(List.of(0, 1, 2))
			.build();

		// when
		Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations.iterator().next().getMessage())
			.contains("Notification method and days required when isActive is true");
	}

	@Test
	void Given_ActiveNotificationWithoutDays_When_Validate_Then_Fail() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(null)
			.build();

		// when
		Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations.iterator().next().getMessage())
			.contains("Notification method and days required when isActive is true");
	}

	@Test
	void Given_ActiveNotificationWithEmptyDays_When_Validate_Then_Fail() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of())
			.build();

		// when
		Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations.iterator().next().getMessage())
			.contains("Notification method and days required when isActive is true");
	}

	@Test
	void Given_InactiveNotificationWithoutFields_When_Validate_Then_Success() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		// when
		Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isEmpty();
	}

	@Test
	void Given_InactiveNotificationWithEmptyDays_When_Validate_Then_Success() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(List.of())
			.build();

		// when
		Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isEmpty();
	}

	@Test
	void Given_InactiveNotificationWithMethodType_When_Validate_Then_Fail() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(null)
			.build();

		// when
		Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations.iterator().next().getMessage())
			.contains("Notification method and days not allowed when isActive is false");
	}

	@Test
	void Given_InactiveNotificationWithDays_When_Validate_Then_Fail() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(List.of(0, 1))
			.build();

		// when
		Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations.iterator().next().getMessage())
			.contains("Notification method and days not allowed when isActive is false");
	}

	@Test
	void Given_ValidNotificationRequest_When_ToEntity_Then_ReturnNotification() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(0, 1, 2))
			.build();

		// when
		Notification notification = request.toEntity();

		// then
		assertThat(notification.getIsActive()).isTrue();
		assertThat(notification.getNotificationType()).isEqualTo(NotificationType.TIME);
		assertThat(notification.getNotificationMethodType()).isEqualTo(NotificationMethodType.PUSH);
	}

	@Test
	void Given_InactiveNotificationRequest_When_ToEntity_Then_ReturnInactiveNotification() {
		// given
		NotificationRequest request = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.LOCATION)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		// when
		Notification notification = request.toEntity();

		// then
		assertThat(notification.getIsActive()).isFalse();
		assertThat(notification.getNotificationType()).isEqualTo(NotificationType.LOCATION);
		assertThat(notification.getNotificationMethodType()).isNull();
	}

}
