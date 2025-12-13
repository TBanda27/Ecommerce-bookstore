package com.inventory.apigateway;

import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class ApiGatewayConfiguration {
    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
        return builder.routes()
                // OAuth2 routes (must be first for proper matching)
                .route(p -> p.path("/api/v1/oauth2/**")
                        .filters(GatewayFilterSpec::preserveHostHeader)
                        .uri("lb://AUTH-SERVICE"))
                .route(p -> p.path("/oauth2/**")
                        .filters(GatewayFilterSpec::preserveHostHeader)
                        .uri("lb://AUTH-SERVICE"))
                .route(p -> p.path("/login/**")
                        .filters(GatewayFilterSpec::preserveHostHeader)
                        .uri("lb://AUTH-SERVICE"))

                // Direct API routes (for Swagger UI)
                .route(p -> p.path("/api/v1/auth/**")
                        .uri("lb://AUTH-SERVICE"))
                .route(p -> p.path("/api/v1/user/**")
                        .uri("lb://AUTH-SERVICE"))
                .route(p -> p.path("/api/v1/books/**")
                        .uri("lb://BOOK-SERVICE"))
                .route(p -> p.path("/api/v1/category/**")
                        .uri("lb://CATEGORY-SERVICE"))
                .route(p -> p.path("/api/v1/price/**")
                        .uri("lb://PRICE-SERVICE"))
                .route(p -> p.path("/api/v1/review/**")
                        .uri("lb://REVIEW-SERVICE"))
                .route(p -> p.path("/api/v1/inventory/**")
                        .uri("lb://INVENTORY-SERVICE"))

                // Prefixed routes (for manual API calls)
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
                .route(p -> p.path("/review/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://REVIEW-SERVICE"))
                .route(p -> p.path("/user/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://AUTH-SERVICE"))
                .build();
    }
}
