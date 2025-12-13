package com.inventory.apigateway.config;

import com.inventory.apigateway.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(auth -> auth
                        // Public endpoints - Auth (with gateway prefix)
                        .pathMatchers("/auth/api/v1/auth/register").permitAll()
                        .pathMatchers("/auth/api/v1/auth/login").permitAll()
                        .pathMatchers("/auth/login/oauth2/**").permitAll()
                        .pathMatchers("/oauth2/**").permitAll()

                        // OAuth2 endpoints (direct paths through gateway)
                        .pathMatchers("/api/v1/oauth2/**").permitAll()
                        .pathMatchers("/login/**").permitAll()

                        // Public endpoints - Auth (direct paths for Swagger)
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/user").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/auth/verify").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/resend-verification").permitAll()

                        // Public endpoints - Documentation and monitoring
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()

                        // Allow API docs for each service
                        .pathMatchers("/auth/api-docs", "/auth/v3/api-docs/**").permitAll()
                        .pathMatchers("/books/api-docs", "/books/v3/api-docs/**").permitAll()
                        .pathMatchers("/category/api-docs", "/category/v3/api-docs/**").permitAll()
                        .pathMatchers("/price/api-docs", "/price/v3/api-docs/**").permitAll()
                        .pathMatchers("/inventory/api-docs", "/inventory/v3/api-docs/**").permitAll()
                        .pathMatchers("/review/api-docs", "/review/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/review").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/review/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/review/book/**").permitAll()

                        // Public read access - GET only
                        .pathMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/category/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/price/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/inventory/**").permitAll()

                        // Admin-only endpoints - Book Service
                        .pathMatchers(HttpMethod.POST, "/api/v1/books").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/books/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/books/**").hasRole("ADMIN")

                        // Admin-only endpoints - Category Service
                        .pathMatchers(HttpMethod.POST, "/api/v1/category").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/category/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/category/**").hasRole("ADMIN")

                        // Admin-only endpoints - Price Service
                        .pathMatchers(HttpMethod.POST, "/api/v1/price").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/price/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/price/**").hasRole("ADMIN")

                        // Admin-only endpoints - Inventory Service
                        .pathMatchers(HttpMethod.POST, "/api/v1/inventory").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/inventory/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/inventory/**").hasRole("ADMIN")

                        // User profile management - Authenticated users can manage their own profile
                        .pathMatchers(HttpMethod.GET, "/api/v1/user/me").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/user/me").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/user/me").hasAnyRole("USER", "ADMIN")

                        // Admin-only endpoints - User Management (view and delete only, no updates)
                        .pathMatchers(HttpMethod.GET, "/api/v1/user").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/user/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/user/**").hasRole("ADMIN")
                        // Admin-and user only endpoints - Review Service
                        .pathMatchers(HttpMethod.GET, "/api/v1/review/me").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/review").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/review/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/review/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/review/**").hasAnyRole("USER", "ADMIN")
                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                // Disable ALL default authentication mechanisms
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .anonymous(ServerHttpSecurity.AnonymousSpec::disable)
                .requestCache(ServerHttpSecurity.RequestCacheSpec::disable)
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                )
                .build();
    }
}
