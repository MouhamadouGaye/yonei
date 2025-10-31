package com.mgaye.yonei.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI supplierOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sneder Receiver API")
                        .version("1.0")
                        .description("Endpoints for authentication, transfer, and user management"));
    }
}
