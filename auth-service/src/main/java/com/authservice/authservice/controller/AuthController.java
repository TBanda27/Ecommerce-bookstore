package com.authservice.authservice.controller;

import com.authservice.authservice.dto.LoginRequestDTO;
import com.authservice.authservice.entity.User;
import com.authservice.authservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints for login and token generation")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(
        summary = "User Login",
        description = "Authenticate user with email and password. Returns a JWT token for subsequent API calls.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Login successful, JWT token returned",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"message\":\"Login Successful\",\"token\":\"eyJhbGc...\",\"type\":\"Bearer\",\"expiresIn\":86400000,\"username\":\"john_doe\",\"email\":\"john@example.com\"}"
                    )
                )
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
        }
    )
    @SecurityRequirements()
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) throws IOException {
        log.info("Login Controller: Login request received {}", loginRequestDTO.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.email(),
                        loginRequestDTO.password()
                )
        );
        User user = (User) authentication.getPrincipal();
        if(user==null){
            throw new UsernameNotFoundException("User not found with email: " + loginRequestDTO.email());
        }
        String token = jwtUtil.generateToken(user);
        log.info("Generated JWT Token for user {}: {}", user.getUsername(), token);
        String jsonResponse = """
            {
                "message": "Login Successful",
                "token": "%s",
                "type": "Bearer",
                "expiresIn": 86400000,
                "username": "%s",
                "email": "%s"
            }
            """.formatted(token, user.getUsername(), user.getEmail());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResponse);
    }
}
