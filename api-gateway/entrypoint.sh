#!/bin/sh
# Entrypoint script for api-gateway
# This ensures environment variables are properly passed to Spring Boot

# Use Railway's PORT or fall back to API_GATEWAY_PORT
PORT=${PORT:-${API_GATEWAY_PORT:-9090}}

# Run the Java application with explicit properties
exec java \
  -Dspring.application.name=api-gateway \
  -Dspring.cloud.gateway.server.webflux.globalcors.cors-configurations.[/**].allowed-origins[3]="${FRONTEND_URL}" \
  -Dserver.port="${PORT}" \
  -Deureka.client.service-url.defaultZone="${EUREKA_URL}" \
  -Djwt.secret="${JWT_SECRET}" \
  -Djwt.expiration="${JWT_EXPIRATION}" \
  -jar app.jar
