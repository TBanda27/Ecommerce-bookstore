package com.authservice.authservice.config;

import com.authservice.authservice.entity.User;
import com.authservice.authservice.enums.Role;
import com.authservice.authservice.repository.UserRepository;
import com.authservice.authservice.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.base-url:http://localhost:9090}")
    private String baseUrl;

    @Value("${app.frontend-url:http://localhost:8501}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Generate a unique username by checking database and appending suffixes if needed
     * Strategy:
     * 1. Try base username (e.g., "john_doe")
     * 2. If exists, try base + email prefix (e.g., "john_doe_johndoe123")
     * 3. If still exists, append counter (e.g., "john_doe_johndoe123_1")
     */
    private String generateUniqueUsername(String baseUsername, String email) {
        String emailPrefix = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();

        // Try base username first
        if (!userRepository.findByUsername(baseUsername).isPresent()) {
            log.info("Using base username: {}", baseUsername);
            return baseUsername;
        }

        // Try base username + email prefix
        String usernameWithEmail = baseUsername + "_" + emailPrefix;
        if (!userRepository.findByUsername(usernameWithEmail).isPresent()) {
            log.info("Using username with email prefix: {}", usernameWithEmail);
            return usernameWithEmail;
        }

        // If still exists, append counter
        int counter = 1;
        String uniqueUsername;
        do {
            uniqueUsername = usernameWithEmail + "_" + counter;
            counter++;
        } while (userRepository.findByUsername(uniqueUsername).isPresent());

        log.info("Using username with counter: {}", uniqueUsername);
        return uniqueUsername;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 Login Success Handler triggered");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        log.info("OAuth2 User authenticated: {}", email);

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Creating new OAuth2 user: {}", email);

                    // Generate unique username
                    String baseUsername = name != null ? name.replaceAll("\\s+", "_").toLowerCase() : email.split("@")[0];
                    String uniqueUsername = generateUniqueUsername(baseUsername, email);

                    User newUser = User.builder()
                            .email(email)
                            .username(uniqueUsername)
                            .password("") // No password for OAuth2 users
                            .roles(Set.of(Role.ROLE_USER))
                            .enabled(true) // Auto-verify OAuth2 users
                            .build();
                    return userRepository.save(newUser);
                });

        // Generate JWT token
        String jwtToken = jwtUtil.generateToken(user);

        log.info("Generated JWT token for OAuth2 user: {}", user.getUsername());

        // Check if request is from Swagger (contains swagger-ui in referer)
        String referer = request.getHeader("Referer");
        boolean isSwaggerRequest = referer != null && referer.contains("swagger-ui");

        if (isSwaggerRequest) {
            // For Swagger, redirect to a page that displays the token
            String redirectUrl = String.format("/api/v1/oauth2/token-display?token=%s&username=%s&email=%s",
                    jwtToken, user.getUsername(), user.getEmail());
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else {
            // For regular OAuth2 flow, redirect to Streamlit frontend main page with token
            // Main page (app.py) will detect query params and handle OAuth2 login
            String redirectUrl = String.format("%s?token=%s&username=%s&email=%s",
                    frontendUrl, jwtToken, user.getUsername(), user.getEmail());
            log.info("Redirecting OAuth2 user to frontend: {}", redirectUrl);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }
}
