package com.priceservice.price.config;

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
        gatewayServer.setUrl("http://localhost:9090/price-service");
        gatewayServer.setDescription("API Gateway");

        return new OpenAPI()
                .info(new Info()
                        .title("Price Service API")
                        .version("1.0")
                        .description("Price management service"))
                .servers(List.of(gatewayServer));
    }
}
