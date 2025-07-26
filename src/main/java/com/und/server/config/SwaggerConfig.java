package com.und.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

	@Value("${springdoc.server.url}")
	private String serverUrl;

	@Value("${springdoc.server.description}")
	private String serverDescription;

	@Bean
	public OpenAPI openApi() {
		final String jwt = "JWT";

		return new OpenAPI()
			.addServersItem(new Server().url(serverUrl).description(serverDescription))
			.info(apiInfo())
			.components(new Components()
				.addSecuritySchemes(jwt, new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("Bearer")
					.bearerFormat(jwt)
				)
			)
			.addSecurityItem(new SecurityRequirement().addList(jwt));
	}

	private Info apiInfo() {
		return new Info()
			.title("API Specification")
			.description("Swagger UI")
			.version("0.0.1");
	}

}
