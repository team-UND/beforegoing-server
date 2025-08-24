package com.und.server.notification.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.UniqueElements;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.entity.Notification;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Notification request")
public record NotificationRequest(

	@Schema(description = "Whether notification is active", example = "true")
	@NotNull(message = "isActive must not be null")
	Boolean isActive,

	@Schema(description = "Notification type", example = "time")
	@NotNull(message = "notificationType must not be null")
	NotificationType notificationType,

	@Schema(description = "Notification method type - required when isActive is true", example = "push")
	NotificationMethodType notificationMethodType,

	@ArraySchema(
		uniqueItems = true,
		arraySchema = @Schema(description = """
			List of days in week when notification is active (0=Monday ... 6=Sunday)
			- required when isActive is true"""),
		schema = @Schema(type = "integer", minimum = "0", maximum = "6")
	)
	@Schema(example = "[0,1,2,3,4,5,6]")
	@Size(max = 7, message = "DayOfWeek list must contain at most 7 items")
	@UniqueElements(message = "DayOfWeek must not contain duplicates")
	List<
		@NotNull(message = "DayOfWeek must not be null")
		@Min(value = 0, message = "DayOfWeek must be between 0 and 6")
		@Max(value = 6, message = "DayOfWeek must be between 0 and 6") Integer> daysOfWeekOrdinal

) {

	@AssertTrue(message = "Notification method and days required when isActive is true")
	private boolean isValidActiveNotification() {
		if (!isActive) {
			return true;
		}
		return notificationMethodType != null && daysOfWeekOrdinal != null && !daysOfWeekOrdinal.isEmpty();
	}

	@AssertTrue(message = "Notification method and days not allowed when isActive is false")
	private boolean isValidInactiveNotification() {
		if (isActive) {
			return true;
		}
		return notificationMethodType == null && (daysOfWeekOrdinal == null || daysOfWeekOrdinal.isEmpty());
	}

	public Notification toEntity() {
		Notification notification = Notification.builder()
			.isActive(isActive)
			.notificationType(notificationType)
			.notificationMethodType(notificationMethodType)
			.build();
		if (isActive) {
			notification.updateDaysOfWeekOrdinal(daysOfWeekOrdinal);
		}

		return notification;
	}

}
