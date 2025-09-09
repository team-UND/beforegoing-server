package com.und.server.weather.controller;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.http.ResponseEntity;

import com.und.server.common.dto.response.ErrorResponse;
import com.und.server.weather.dto.request.WeatherRequest;
import com.und.server.weather.dto.response.WeatherResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

public interface WeatherApiDocs {

	@Operation(summary = "Get Weather Information API")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Successfully retrieved weather information",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = WeatherResponse.class)
					)
			),
			@ApiResponse(
					responseCode = "400",
					description = "Bad request",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class),
							examples = {
								@ExampleObject(
										name = "Latitude must not be null",
										value = """
											{
											  "code": "INVALID_PARAMETER",
											  "message": "Latitude must not be null"
											}
											"""
								),
								@ExampleObject(
										name = "Longitude must not be null",
										value = """
											{
											  "code": "INVALID_PARAMETER",
											  "message": "Longitude must not be null"
											}
											"""
								),
								@ExampleObject(
										name = "Latitude out of range",
										value = """
											{
											  "code": "INVALID_PARAMETER",
											  "message": "Latitude must be at least -90 degrees"
											}
											"""
								),
								@ExampleObject(
										name = "Longitude out of range",
										value = """
											{
											  "code": "INVALID_PARAMETER",
											  "message": "Longitude must be at most 180 degrees"
											}
											"""
								),
								@ExampleObject(
										name = "Invalid coordinates",
										value = """
											{
											  "code": "INVALID_COORDINATES",
											  "message": "Invalid location coordinates"
											}
											"""
								),
								@ExampleObject(
										name = "Date out of range",
										value = """
											{
											  "code": "DATE_OUT_OF_RANGE",
											  "message": "Date is out of range (maximum +3 days)"
											}
											"""
								)
							}
					)
			),
			@ApiResponse(
					responseCode = "503",
					description = "Service unavailable",
					content = @Content(
							mediaType = "application/json",
							schema = @Schema(implementation = ErrorResponse.class),
							examples = {
								@ExampleObject(
										name = "Weather service error",
										value = """
											{
											  "code": "SERVICE_UNAVAILABLE",
											  "message": "An error occurred while processing weather service"
											}
											"""
								)
							}
					)
			)
	})
	ResponseEntity<WeatherResponse> getWeather(
			@Parameter(description = "Weather request information") @Valid final WeatherRequest request,
			@Parameter(description = "Target date for weather information (yyyy-MM-dd)") final LocalDate date,
			@Parameter(description = "Target TimeZone") final ZoneId timeZone
	);

}
