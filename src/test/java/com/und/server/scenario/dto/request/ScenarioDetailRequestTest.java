package com.und.server.scenario.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.request.NotificationRequest;
import com.und.server.notification.dto.request.TimeNotificationRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ScenarioDetailRequestTest {

	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

	@Test
	void Given_ActiveNotificationWithCondition_When_Validate_Then_Success() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(0, 1, 2))
			.build();

		TimeNotificationRequest notificationCondition = TimeNotificationRequest.builder()
			.notificationType(NotificationType.TIME)
			.startHour(9)
			.startMinute(30)
			.build();

		BasicMissionRequest mission = BasicMissionRequest.builder()
			.content("Test")
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("Test")
			.memo("Test")
			.basicMissions(List.of(mission))
			.notification(notification)
			.notificationCondition(notificationCondition)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isEmpty();
	}

	@Test
	void Given_ActiveNotificationWithoutCondition_When_Validate_Then_Fail() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(true)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(NotificationMethodType.PUSH)
			.daysOfWeekOrdinal(List.of(0, 1, 2))
			.build();

		BasicMissionRequest mission = BasicMissionRequest.builder()
			.content("Test")
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("Test")
			.memo("Test")
			.basicMissions(List.of(mission))
			.notification(notification)
			.notificationCondition(null)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations.iterator().next().getMessage())
			.contains("Notification condition required when notification is active");
	}

	@Test
	void Given_InactiveNotificationWithoutCondition_When_Validate_Then_Success() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		BasicMissionRequest mission = BasicMissionRequest.builder()
			.content("Test")
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("Test")
			.memo("Test")
			.basicMissions(List.of(mission))
			.notification(notification)
			.notificationCondition(null)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isEmpty();
	}

	@Test
	void Given_InactiveNotificationWithCondition_When_Validate_Then_Fail() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		TimeNotificationRequest notificationCondition = TimeNotificationRequest.builder()
			.notificationType(NotificationType.TIME)
			.startHour(9)
			.startMinute(30)
			.build();

		BasicMissionRequest mission = BasicMissionRequest.builder()
			.content("Test")
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("Test")
			.memo("Test")
			.basicMissions(List.of(mission))
			.notification(notification)
			.notificationCondition(notificationCondition)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		assertThat(violations.iterator().next().getMessage())
			.contains("Notification condition not allowed when notification is inactive");
	}

	@Test
	void Given_EmptyScenarioName_When_Validate_Then_Fail() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("")
			.memo("Test")
			.basicMissions(List.of())
			.notification(notification)
			.notificationCondition(null)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		boolean hasScenarioNameError = violations.stream()
			.anyMatch(v -> v.getMessage().contains("Scenario name must not be blank"));
		assertThat(hasScenarioNameError).isTrue();
	}

	@Test
	void Given_TooLongScenarioName_When_Validate_Then_Fail() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("TooLongName")  // 10자 초과
			.memo("Test")
			.basicMissions(List.of())
			.notification(notification)
			.notificationCondition(null)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		boolean hasScenarioNameError = violations.stream()
			.anyMatch(v -> v.getMessage().contains("Scenario name must be at most 10 characters"));
		assertThat(hasScenarioNameError).isTrue();
	}

	@Test
	void Given_TooLongMemo_When_Validate_Then_Fail() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("Test")
			.memo("TooLongMemoText23123123123")
			.basicMissions(List.of())
			.notification(notification)
			.notificationCondition(null)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		boolean hasMemoError = violations.stream()
			.anyMatch(v -> v.getMessage().contains("Memo must be at most 15 characters"));
		assertThat(hasMemoError).isTrue();
	}

	@Test
	void Given_TooManyMissions_When_Validate_Then_Fail() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		List<BasicMissionRequest> missions = List.of(
			BasicMissionRequest.builder().content("1").build(),
			BasicMissionRequest.builder().content("2").build(),
			BasicMissionRequest.builder().content("3").build(),
			BasicMissionRequest.builder().content("4").build(),
			BasicMissionRequest.builder().content("5").build(),
			BasicMissionRequest.builder().content("6").build(),
			BasicMissionRequest.builder().content("7").build(),
			BasicMissionRequest.builder().content("8").build(),
			BasicMissionRequest.builder().content("9").build(),
			BasicMissionRequest.builder().content("10").build(),
			BasicMissionRequest.builder().content("11").build(),
			BasicMissionRequest.builder().content("12").build(),
			BasicMissionRequest.builder().content("13").build(),
			BasicMissionRequest.builder().content("14").build(),
			BasicMissionRequest.builder().content("15").build(),
			BasicMissionRequest.builder().content("16").build(),
			BasicMissionRequest.builder().content("17").build(),
			BasicMissionRequest.builder().content("18").build(),
			BasicMissionRequest.builder().content("19").build(),
			BasicMissionRequest.builder().content("20").build(),
			BasicMissionRequest.builder().content("21").build()
		);

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("Test")
			.memo("Test")
			.basicMissions(missions)
			.notification(notification)
			.notificationCondition(null)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isNotEmpty();
		boolean hasMissionCountError = violations.stream()
			.anyMatch(v -> v.getMessage().contains("Maximum mission count exceeded"));
		assertThat(hasMissionCountError).isTrue();
	}

	@Test
	void Given_ValidMinimalRequest_When_Validate_Then_Success() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("Test")
			.memo("")
			.basicMissions(List.of())
			.notification(notification)
			.notificationCondition(null)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isEmpty();
	}

	@Test
	void Given_MaxLengthValues_When_Validate_Then_Success() {
		// given
		NotificationRequest notification = NotificationRequest.builder()
			.isActive(false)
			.notificationType(NotificationType.TIME)
			.notificationMethodType(null)
			.daysOfWeekOrdinal(null)
			.build();

		ScenarioDetailRequest request = ScenarioDetailRequest.builder()
			.scenarioName("1234567890")
			.memo("123456789012345")
			.basicMissions(List.of())
			.notification(notification)
			.notificationCondition(null)
			.build();

		// when
		Set<ConstraintViolation<ScenarioDetailRequest>> violations = validator.validate(request);

		// then
		assertThat(violations).isEmpty();
	}

}
