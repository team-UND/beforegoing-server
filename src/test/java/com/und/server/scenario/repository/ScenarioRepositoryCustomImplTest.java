package com.und.server.scenario.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.dto.response.ScenarioNotificationResponse;
import com.und.server.notification.dto.response.TimeNotificationResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;


@ExtendWith(MockitoExtension.class)
class ScenarioRepositoryCustomImplTest {

	@InjectMocks
	private ScenarioRepositoryCustomImpl scenarioRepositoryCustomImpl;

	@Mock
	private EntityManager entityManager;

	@Mock
	private TypedQuery<ScenarioRepositoryCustomImpl.TimeNotificationQueryDto> typedQuery;

	@Mock
	private Query query;

	private final Long memberId = 1L;


	@Test
	void Given_ValidMemberId_When_FindTimeScenarioNotifications_Then_ReturnTimeNotifications() {
		// given
		ScenarioRepositoryCustomImpl.TimeNotificationQueryDto queryDto =
			ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.builder()
				.scenarioId(1L)
				.scenarioName("아침 루틴")
				.memo("아침에 할 일들")
				.notificationId(1L)
				.notificationType(NotificationType.TIME)
				.notificationMethodType(NotificationMethodType.PUSH)
				.daysOfWeek("1,2,3,4,5")
				.startHour(9)
				.startMinute(30)
				.build();

		when(entityManager.createQuery(anyString(), eq(ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.class)))
			.thenReturn(typedQuery);
		when(typedQuery.setParameter(eq("memberId"), eq(memberId)))
			.thenReturn(typedQuery);
		when(typedQuery.setParameter(eq("timeType"), eq(NotificationType.TIME)))
			.thenReturn(typedQuery);
		when(typedQuery.getResultList())
			.thenReturn(List.of(queryDto));

		// when
		List<ScenarioNotificationResponse> result =
			scenarioRepositoryCustomImpl.findTimeScenarioNotifications(memberId);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).scenarioId()).isEqualTo(1L);
		assertThat(result.get(0).scenarioName()).isEqualTo("아침 루틴");
		assertThat(result.get(0).memo()).isEqualTo("아침에 할 일들");
		assertThat(result.get(0).notificationType()).isEqualTo(NotificationType.TIME);
		assertThat(result.get(0).notificationMethodType()).isEqualTo(NotificationMethodType.PUSH);
		assertThat(result.get(0).daysOfWeekOrdinal()).containsExactly(1, 2, 3, 4, 5);
		assertThat(result.get(0).notificationCondition()).isInstanceOf(TimeNotificationResponse.class);
	}


	@Test
	void Given_ValidMemberId_When_FindTimeScenarioNotifications_Then_ReturnMultipleTimeNotifications() {
		// given
		ScenarioRepositoryCustomImpl.TimeNotificationQueryDto morningDto =
			ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.builder()
				.scenarioId(1L)
				.scenarioName("아침 루틴")
				.memo("아침에 할 일들")
				.notificationId(1L)
				.notificationType(NotificationType.TIME)
				.notificationMethodType(NotificationMethodType.PUSH)
				.daysOfWeek("1,2,3,4,5")
				.startHour(9)
				.startMinute(30)
				.build();

		ScenarioRepositoryCustomImpl.TimeNotificationQueryDto eveningDto =
			ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.builder()
				.scenarioId(2L)
				.scenarioName("저녁 루틴")
				.memo("저녁에 할 일들")
				.notificationId(2L)
				.notificationType(NotificationType.TIME)
				.notificationMethodType(NotificationMethodType.ALARM)
				.daysOfWeek("1,2,3,4,5,6,7")
				.startHour(18)
				.startMinute(0)
				.build();

		when(entityManager.createQuery(anyString(), eq(ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.class)))
			.thenReturn(typedQuery);
		when(typedQuery.setParameter(eq("memberId"), eq(memberId)))
			.thenReturn(typedQuery);
		when(typedQuery.setParameter(eq("timeType"), eq(NotificationType.TIME)))
			.thenReturn(typedQuery);
		when(typedQuery.getResultList())
			.thenReturn(List.of(morningDto, eveningDto));

		// when
		List<ScenarioNotificationResponse> result =
			scenarioRepositoryCustomImpl.findTimeScenarioNotifications(memberId);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).scenarioId()).isEqualTo(1L);
		assertThat(result.get(0).scenarioName()).isEqualTo("아침 루틴");
		assertThat(result.get(1).scenarioId()).isEqualTo(2L);
		assertThat(result.get(1).scenarioName()).isEqualTo("저녁 루틴");
	}


	@Test
	void Given_ValidMemberId_When_FindTimeScenarioNotifications_Then_ReturnEmptyList() {
		// given
		when(entityManager.createQuery(anyString(), eq(ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.class)))
			.thenReturn(typedQuery);
		when(typedQuery.setParameter(eq("memberId"), eq(memberId)))
			.thenReturn(typedQuery);
		when(typedQuery.setParameter(eq("timeType"), eq(NotificationType.TIME)))
			.thenReturn(typedQuery);
		when(typedQuery.getResultList())
			.thenReturn(List.of());

		// when
		List<ScenarioNotificationResponse> result =
			scenarioRepositoryCustomImpl.findTimeScenarioNotifications(memberId);

		// then
		assertThat(result).isEmpty();
	}


	@Test
	void Given_TimeNotificationQueryDtoWithNullDaysOfWeek_When_ToResponse_Then_ReturnEmptyDaysList() {
		// given
		ScenarioRepositoryCustomImpl.TimeNotificationQueryDto queryDto =
			ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.builder()
				.scenarioId(1L)
				.scenarioName("테스트 루틴")
				.memo("테스트 메모")
				.notificationId(1L)
				.notificationType(NotificationType.TIME)
				.notificationMethodType(NotificationMethodType.PUSH)
				.daysOfWeek(null)
				.startHour(10)
				.startMinute(0)
				.build();

		// when
		ScenarioNotificationResponse result = queryDto.toResponse();

		// then
		assertThat(result.daysOfWeekOrdinal()).isEmpty();
		assertThat(result.scenarioId()).isEqualTo(1L);
		assertThat(result.scenarioName()).isEqualTo("테스트 루틴");
	}


	@Test
	void Given_TimeNotificationQueryDtoWithBlankDaysOfWeek_When_ToResponse_Then_ReturnEmptyDaysList() {
		// given
		ScenarioRepositoryCustomImpl.TimeNotificationQueryDto queryDto =
			ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.builder()
				.scenarioId(1L)
				.scenarioName("테스트 루틴")
				.memo("테스트 메모")
				.notificationId(1L)
				.notificationType(NotificationType.TIME)
				.notificationMethodType(NotificationMethodType.PUSH)
				.daysOfWeek("")
				.startHour(10)
				.startMinute(0)
				.build();

		// when
		ScenarioNotificationResponse result = queryDto.toResponse();

		// then
		assertThat(result.daysOfWeekOrdinal()).isEmpty();
		assertThat(result.scenarioId()).isEqualTo(1L);
		assertThat(result.scenarioName()).isEqualTo("테스트 루틴");
	}


	@Test
	void Given_TimeNotificationQueryDtoWithSpacedDaysOfWeek_When_ToResponse_Then_ReturnCorrectDaysList() {
		// given
		ScenarioRepositoryCustomImpl.TimeNotificationQueryDto queryDto =
			ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.builder()
				.scenarioId(1L)
				.scenarioName("테스트 루틴")
				.memo("테스트 메모")
				.notificationId(1L)
				.notificationType(NotificationType.TIME)
				.notificationMethodType(NotificationMethodType.PUSH)
				.daysOfWeek("1, 2, 3, 4, 5")
				.startHour(10)
				.startMinute(0)
				.build();

		// when
		ScenarioNotificationResponse result = queryDto.toResponse();

		// then
		assertThat(result.daysOfWeekOrdinal()).containsExactly(1, 2, 3, 4, 5);
		assertThat(result.scenarioId()).isEqualTo(1L);
		assertThat(result.scenarioName()).isEqualTo("테스트 루틴");
	}


	@Test
	void Given_TimeNotificationQueryDtoWithSingleDay_When_ToResponse_Then_ReturnCorrectDaysList() {
		// given
		ScenarioRepositoryCustomImpl.TimeNotificationQueryDto queryDto =
			ScenarioRepositoryCustomImpl.TimeNotificationQueryDto.builder()
				.scenarioId(1L)
				.scenarioName("테스트 루틴")
				.memo("테스트 메모")
				.notificationId(1L)
				.notificationType(NotificationType.TIME)
				.notificationMethodType(NotificationMethodType.PUSH)
				.daysOfWeek("1")
				.startHour(10)
				.startMinute(0)
				.build();

		// when
		ScenarioNotificationResponse result = queryDto.toResponse();

		// then
		assertThat(result.daysOfWeekOrdinal()).containsExactly(1);
		assertThat(result.scenarioId()).isEqualTo(1L);
		assertThat(result.scenarioName()).isEqualTo("테스트 루틴");
	}

}
