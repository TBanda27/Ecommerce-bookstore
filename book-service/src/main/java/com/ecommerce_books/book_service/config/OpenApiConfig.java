package com.ecommerce_books.book_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:9090/book-service");
        gatewayServer.setDescription("API Gateway");

        return new OpenAPI()
                .info(new Info()
                        .title("Book Service API")
                        .version("1.0")
                        .description("Book management service"))
                .servers(List.of(gatewayServer));
    }
}
