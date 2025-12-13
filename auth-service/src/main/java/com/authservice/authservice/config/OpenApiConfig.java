package com.authservice.authservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Authentication Service API",
        version = "1.0",
        description = "Authentication and User Management API with JWT-based security",
        contact = @Contact(
            name = "Tawanda Banda",
            email = "thebanda27@gmail.com"
        )
    ),
    servers = {
        @Server(
            description = "Local Development Server",
//            url = "http://localhost:8080"
            url = "/"
        )
    },
    security = {
        @SecurityRequirement(name = "bearerAuth"),
        @SecurityRequirement(name = "oauth2")
    }
)
@SecuritySchemes({
    @SecurityScheme(
        name = "bearerAuth",
        description = "JWT Bearer Token Authentication. Obtain token by logging in via /api/v1/auth/login",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
    ),
    @SecurityScheme(
        name = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        description = "OAuth2 Authentication with Google",
        flows = @OAuthFlows(
            authorizationCode = @OAuthFlow(
                authorizationUrl = "https://accounts.google.com/o/oauth2/v2/auth",
                tokenUrl = "https://oauth2.googleapis.com/token",
                scopes = {
                    @OAuthScope(name = "openid", description = "OpenID Connect"),
                    @OAuthScope(name = "email", description = "Access user email"),
                    @OAuthScope(name = "profile", description = "Access user profile")
                }
            )
        )
    )
})
public class OpenApiConfig {

}