package com.devops.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Spring Boot CI/CD Task API",
        version = "1.0.0",
        description = "Production-ready Task Management REST API with CI/CD pipeline",
        contact = @Contact(name = "Kabir Moulana", url = "https://github.com/KabirMoulana"),
        license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local development"),
        @Server(url = "https://staging.example.com", description = "Staging"),
        @Server(url = "https://api.example.com", description = "Production")
    }
)
public class OpenApiConfig {
}
