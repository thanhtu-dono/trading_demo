package com.example.trading.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class OpenApiConfig {
    @Bean
    public OpenAPI metaData() {
        return new OpenAPI()
                .info(new Info().title("Swagger TRADING - SERVICE")
                        .description("APIs Document for TRADING - SERVICE")
                        .version("1.0.0"));
    }
}
