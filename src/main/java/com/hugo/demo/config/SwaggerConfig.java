package com.hugo.demo.config;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI myCustomConfig() {
        return new OpenAPI().info(
            new Info().title("Hugo Demo Project APIs").description("API Documentation for Hugo Demo Project").version("1.0")
        ).servers(List.of(new Server().url("https://localhost:8081").description("Hugo Demo Project")));
    }
}
