#!/bin/sh
# Entrypoint script for auth-service
# This ensures environment variables are properly passed to Spring Boot

# Run the Java application with explicit datasource properties
exec java \
  -Dspring.application.name=auth-service \
  -Dspring.datasource.url="${DB_URL_AUTH}" \
  -Dspring.datasource.username="${DB_USERNAME}" \
  -Dspring.datasource.password="${DB_PASSWORD}" \
  -Djwt.secret="${JWT_SECRET}" \
  -Djwt.expiration="${JWT_EXPIRATION}" \
  -Dspring.security.oauth2.client.registration.google.client-id="${GOOGLE_CLIENT_ID}" \
  -Dspring.security.oauth2.client.registration.google.client-secret="${GOOGLE_CLIENT_SECRET}" \
  -Dspring.security.oauth2.client.registration.google.redirect-uri="${GOOGLE_REDIRECT_URI}" \
  -Dspring.mail.host="${EMAIL_HOST}" \
  -Dspring.mail.port="${EMAIL_PORT}" \
  -Dspring.mail.username="${EMAIL_USERNAME}" \
  -Dspring.mail.password="${EMAIL_PASSWORD}" \
  -Dserver.port="${AUTH_SERVICE_PORT}" \
  -Deureka.instance.hostname="${RAILWAY_PRIVATE_DOMAIN}" \
  -Deureka.instance.prefer-ip-address=false \
  -Deureka.client.service-url.defaultZone="${EUREKA_URL}" \
  -Dapp.base-url="${BASE_URL}" \
  -Dapp.frontend-url="${FRONTEND_URL}" \
  -jar app.jar
