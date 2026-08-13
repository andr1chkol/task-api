package com.andr1chkol.taskapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Task API",
                version = "1.0",
                description = "REST API for managing tasks"
        )
)
public class OpenApiConfig {
}
