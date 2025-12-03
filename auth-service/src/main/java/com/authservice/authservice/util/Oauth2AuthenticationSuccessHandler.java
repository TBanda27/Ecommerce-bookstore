package com.authservice.authservice.util;

import com.authservice.authservice.entity.User;
import com.authservice.authservice.enums.Role;
import com.authservice.authservice.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@Slf4j
public class Oauth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public Oauth2AuthenticationSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        if (oauthUser == null) {
            throw new IllegalStateException("OAuth2User is null");
        }
        String email = oauthUser.getAttribute("email");
        String username = oauthUser.getAttribute("name");
        if (email == null || username == null) {
            throw new IllegalStateException("Email or Username not found in OAuth2User attributes");
        }
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(username)
                            .email(email)
                            .roles(Set.of(Role.ROLE_USER))
                            .build();
                    return userRepository.save(newUser);
                });
        String token = jwtUtil.generateToken(user);
        log.info("Generated JWT Token for user {}: {}", user.getUsername(), token);
        response.setContentType("application/json");
        response.getWriter().write("{\"token\": \"" + token + "\"}");
        response.getWriter().write(String.format(
                "{\"message\":\"OAuth2 login successful\",\"token\":\"%s\",\"type\":\"Bearer\",\"expiresIn\":86400000,\"username\":\"%s\",\"email\":\"%s\"}",
                token,
                user.getUsername(),
                user.getEmail()
        ));
        response.getWriter().flush();
    }
}
