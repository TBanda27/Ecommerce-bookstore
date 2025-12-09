package com.inventory.apigateway.filter;

import com.inventory.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        // If no Authorization header or not Bearer token, continue without authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        try {
            if (jwtUtil.isTokenExpired(token)) {
                return chain.filter(exchange);
            }

            String username = jwtUtil.extractUsername(token);
            Claims claims = jwtUtil.extractAllClaims(token);

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            if (username != null && roles != null && !roles.isEmpty()) {
                // IMPORTANT: Add "ROLE_" prefix if not already present
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> {
                            if (role.startsWith("ROLE_")) {
                                return new SimpleGrantedAuthority(role);
                            } else {
                                return new SimpleGrantedAuthority("ROLE_" + role);
                            }
                        })
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                // Extract userId from JWT and add to headers for downstream services
                Object userIdObj = claims.get("userId");
                String userId = userIdObj != null ? userIdObj.toString() : null;

                // Create modified exchange with user context headers
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(r -> {
                            if (userId != null) {
                                r.header("X-User-Id", userId);
                            }
                            r.header("X-Username", username);
                            r.header("X-User-Roles", String.join(",", roles));
                        })
                        .build();

                return chain.filter(modifiedExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            }

        } catch (Exception e) {
            System.err.println("JWT validation error: " + e.getMessage());
            // Continue without setting authentication
        }

        // Continue without authentication
        return chain.filter(exchange);
    }
}