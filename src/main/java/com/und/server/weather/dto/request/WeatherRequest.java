package com.und.server.weather.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Weather request")
public record WeatherRequest(

	@Schema(description = "Latitude", example = "37.5663")
	@NotNull(message = "Latitude must not be null")
	@DecimalMin(value = "-90.0", message = "Latitude must be at least -90 degrees")
	@DecimalMax(value = "90.0", message = "Latitude must be at most 90 degrees")
	@Digits(integer = 3, fraction = 6,
		message = "Latitude must have at most 3 integer digits and 6 decimal places")
	Double latitude,

	@Schema(description = "Longitude", example = "126.9779")
	@NotNull(message = "Longitude must not be null")
	@DecimalMin(value = "-180.0", message = "Longitude must be at least -180 degrees")
	@DecimalMax(value = "180.0", message = "Longitude must be at most 180 degrees")
	@Digits(integer = 3, fraction = 6,
		message = "Longitude must have at most 3 integer digits and 6 decimal places")
	Double longitude

) { }
