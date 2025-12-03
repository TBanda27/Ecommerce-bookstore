package com.inventory.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class ApiGatewayConfiguration {
    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p.path("/books/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://BOOK-SERVICE"))
                .route(p -> p.path("/price/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://PRICE-SERVICE"))
                .route(p -> p.path("/inventory/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://INVENTORY-SERVICE"))
                .route(p -> p.path("/category/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://CATEGORY-SERVICE"))
                .route(p -> p.path("/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://AUTH-SERVICE"))
                .route(p -> p.path("/user/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://AUTH-SERVICE"))
                .build();
    }
}
