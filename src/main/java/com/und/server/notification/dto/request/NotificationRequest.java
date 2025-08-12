package com.und.server.notification.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.UniqueElements;

import com.und.server.notification.constants.NotificationMethodType;
import com.und.server.notification.constants.NotificationType;
import com.und.server.notification.entity.Notification;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@ToString
@Schema(description = "Notification request")
public class NotificationRequest {

	@Schema(description = "Notification type", example = "TIME")
	@NotNull(message = "notificationType must not be null")
	private NotificationType notificationType;

	@Schema(description = "Notification method type", example = "PUSH")
	@NotNull(message = "notificationMethod must not be null")
	private NotificationMethodType notificationMethodType;

	@ArraySchema(
		uniqueItems = true,
		arraySchema = @Schema(description = "List of days in week when notification is active (0=Monday ... 6=Sunday)"),
		schema = @Schema(type = "integer", minimum = "0", maximum = "6")
	)
	@Schema(example = "[0,1,2,3,4,5,6]")
	@Size(max = 7, message = "DayOfWeek list must contain at most 7 items")
	@UniqueElements(message = "DayOfWeek must not contain duplicates")
	private List<
		@NotNull(message = "DayOfWeek must not be null")
		@Min(value = 0, message = "DayOfWeek must be between 0 and 6")
		@Max(value = 6, message = "DayOfWeek must be between 0 and 6") Integer> dayOfWeekOrdinalList;

	public Notification toEntity() {
		return Notification.builder()
			.isActive(true)
			.notificationType(notificationType)
			.notificationMethodType(notificationMethodType)
			.build();
	}

}
