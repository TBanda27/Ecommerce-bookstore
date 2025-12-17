package com.authservice.authservice.controller;

import com.authservice.authservice.entity.User;
import com.authservice.authservice.enums.Role;
import com.authservice.authservice.repository.UserRepository;
import com.authservice.authservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/oauth2")
@Slf4j
@Tag(name = "OAuth2 Authentication", description = "OAuth2 authentication endpoints for Google login")
public class OAuth2Controller {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public OAuth2Controller(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/login/google")
    @Operation(
        summary = "Initiate Google OAuth2 Login",
        description = "Redirects to Google OAuth2 login page. After successful authentication, " +
                     "user will be redirected to the callback URL with authentication details."
    )
    @SecurityRequirements()
    @ApiResponse(responseCode = "302", description = "Redirect to Google login")
    public RedirectView loginWithGoogle() {
        log.info("OAuth2: Redirecting to Google login");
        return new RedirectView("/oauth2/authorization/google");
    }

    @GetMapping("/callback/google")
    @Operation(
        summary = "Google OAuth2 Callback",
        description = "Handles the callback from Google after successful authentication. " +
                     "Creates or updates user and returns a JWT token.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "OAuth2 authentication failed")
        }
    )
    @SecurityRequirements()
    public ResponseEntity<Map<String, Object>> handleGoogleCallback(
            OAuth2AuthenticationToken authentication) {

        log.info("OAuth2 Callback: Received authentication from Google");

        // Extract user information from Google
        OAuth2User oAuth2User = authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        log.info("OAuth2: User authenticated - Email: {}, Name: {}", email, name);

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("OAuth2: Creating new user for email: {}", email);
                    User newUser = User.builder()
                            .email(email)
                            .username(name != null ? name.replaceAll("\\s+", "_").toLowerCase() : email.split("@")[0])
                            .password("") // OAuth2 users don't have passwords
                            .roles(Set.of(Role.ROLE_USER))
                            .enabled(true) // Auto-enable OAuth2 users
                            .build();
                    return userRepository.save(newUser);
                });

        // Generate JWT token
        String jwtToken = jwtUtil.generateToken(user);

        log.info("OAuth2: Generated JWT token for user: {}", user.getUsername());

        // Prepare response
        Map<String, Object> response = Map.of(
            "message", "OAuth2 Login Successful",
            "token", jwtToken,
            "type", "Bearer",
            "expiresIn", 86400000,
            "username", user.getUsername(),
            "email", user.getEmail(),
            "authProvider", "google"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-info")
    @Operation(
        summary = "Get OAuth2 User Info",
        description = "Returns the currently authenticated OAuth2 user information",
        responses = {
            @ApiResponse(responseCode = "200", description = "User info retrieved"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
        }
    )
    public ResponseEntity<Map<String, Object>> getUserInfo(OAuth2AuthenticationToken authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        OAuth2User oAuth2User = authentication.getPrincipal();

        Map<String, Object> userInfo = Map.of(
            "email", oAuth2User.getAttribute("email"),
            "name", oAuth2User.getAttribute("name"),
            "picture", oAuth2User.getAttribute("picture"),
            "emailVerified", oAuth2User.getAttribute("email_verified"),
            "provider", "google"
        );

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/token-display")
    @Operation(
        summary = "Display JWT Token",
        description = "Displays the JWT token after OAuth2 login (for Swagger/testing purposes)",
        hidden = true
    )
    @SecurityRequirements()
    public ResponseEntity<String> displayToken(
            @RequestParam String token,
            @RequestParam String username,
            @RequestParam String email) {

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>OAuth2 Login Success</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 50px; background: #f5f5f5; }
                    .container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .token { background: #f0f0f0; padding: 15px; border-radius: 5px; word-break: break-all; margin: 20px 0; }
                    .success { color: #28a745; font-size: 24px; margin-bottom: 20px; }
                    .info { margin: 10px 0; }
                    .copy-btn { background: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; }
                    .copy-btn:hover { background: #0056b3; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success">✓ OAuth2 Login Successful!</div>
                    <div class="info"><strong>Username:</strong> %s</div>
                    <div class="info"><strong>Email:</strong> %s</div>
                    <div class="info"><strong>JWT Token:</strong></div>
                    <div class="token" id="token">%s</div>
                    <button class="copy-btn" onclick="copyToken()">Copy Token</button>
                    <p>Use this token in Swagger by clicking "Authorize" and pasting it in the "bearerAuth" field.</p>
                </div>
                <script>
                    function copyToken() {
                        const token = document.getElementById('token').textContent;
                        navigator.clipboard.writeText(token);
                        alert('Token copied to clipboard!');
                    }
                </script>
            </body>
            </html>
            """.formatted(username, email, token);

        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(html);
    }
}
