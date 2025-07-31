package com.und.server.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openApi() {
		final String jwt = "JWT";

		return new OpenAPI()
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

	@Configuration
	@Profile("local")
	@OpenAPIDefinition(servers = {
		@Server(url = "http://localhost:8080/server", description = "Local Server")
	})
	static class LocalConfig {
	}

	@Configuration
	@Profile("dev")
	@OpenAPIDefinition(servers = {
		@Server(url = "https://api.beforegoing.store/server", description = "Development Server")
	})
	static class DevConfig {
	}

}
