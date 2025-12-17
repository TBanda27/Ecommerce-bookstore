package com.reviews.reviews.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extract headers added by API Gateway
        String username = request.getHeader("X-Username");
        String rolesHeader = request.getHeader("X-User-Roles");
        String userIdHeader = request.getHeader("X-User-Id");

        // If headers are present, create authentication
        if (username != null && rolesHeader != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.debug("Setting authentication for user: {} with roles: {} and userId: {}", username, rolesHeader, userIdHeader);

            // Parse roles from comma-separated string
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(role -> {
                        // Ensure ROLE_ prefix is present
                        if (role.startsWith("ROLE_")) {
                            return new SimpleGrantedAuthority(role);
                        } else {
                            return new SimpleGrantedAuthority("ROLE_" + role);
                        }
                    })
                    .collect(Collectors.toList());

            // Create authentication token with userId as credential
            Long userId = userIdHeader != null ? Long.parseLong(userIdHeader) : null;
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, userId, authorities);

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
