# OAuth2 Integration with Streamlit - Deployment Guide

This guide explains how to configure and deploy your Books E-commerce application with Google OAuth2 authentication working seamlessly with the Streamlit frontend.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Local Development Setup](#local-development-setup)
3. [Google Cloud Console Configuration](#google-cloud-console-configuration)
4. [Deployment Scenarios](#deployment-scenarios)
5. [Environment Variables](#environment-variables)
6. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

### OAuth2 Flow
```
User clicks "Login with Google" in Streamlit
    ↓
Streamlit redirects to Spring Boot Auth Service (/api/v1/oauth2/login/google)
    ↓
Auth Service redirects to Google OAuth2 consent screen
    ↓
User authorizes the application
    ↓
Google redirects back to Auth Service with authorization code
    ↓
Auth Service exchanges code for user info, creates/finds user, generates JWT
    ↓
Auth Service redirects to Streamlit callback page with JWT token
    ↓
Streamlit stores JWT token in session and user is authenticated
```

### Components
- **Backend**: Spring Boot microservices with OAuth2 and JWT
- **Frontend**: Streamlit app
- **Authentication**: Google OAuth2 + JWT tokens
- **API Gateway**: Routes requests through port 9090

---

## Local Development Setup

### 1. Configure Google OAuth2

#### Create OAuth2 Credentials
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable **Google+ API**
4. Go to **Credentials** → **Create Credentials** → **OAuth 2.0 Client ID**
5. Configure OAuth consent screen (if not done already)
6. Select **Web application** as application type
7. Add **Authorized redirect URIs**:
   - For local development: `http://localhost:9090/login/oauth2/code/google`
   - For production: `https://your-backend-domain.com/login/oauth2/code/google`

#### Copy Credentials
- Copy **Client ID** and **Client Secret**

### 2. Configure Environment Variables

Copy `.env.example` to `.env` in the project root:

```bash
cp .env.example .env
```

Update the following variables:

```bash
# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id_from_cloud_console
GOOGLE_CLIENT_SECRET=your_google_client_secret_from_cloud_console
GOOGLE_REDIRECT_URI=http://localhost:9090/login/oauth2/code/google

# Application URLs
BASE_URL=http://localhost:9090
FRONTEND_URL=http://localhost:8501

# JWT Configuration
JWT_SECRET=your_secure_random_secret_minimum_256_bits
JWT_EXPIRATION=86400000

# Database, Email, and other configurations...
```

### 3. Start Services Locally

#### Start Backend Services
```bash
# Start Eureka Discovery Server
cd eureka
./mvnw spring-boot:run

# Start Auth Service
cd ../auth-service
./mvnw spring-boot:run

# Start other microservices (books, category, price, inventory, reviews)
# ...

# Start API Gateway
cd ../api-gateway
./mvnw spring-boot:run
```

#### Start Streamlit Frontend
```bash
cd streamlit-frontend
pip install -r requirements.txt
streamlit run app.py
```

### 4. Test OAuth2 Login

1. Open browser: `http://localhost:8501`
2. Click **"Login with Google"** button in sidebar
3. You'll be redirected to Google consent screen
4. Authorize the application
5. You'll be redirected back to Streamlit with authentication complete

---

## Deployment Scenarios

### Scenario 1: Deploy Everything to Docker

#### Backend Services (Docker Compose)

Update `docker-compose.yml`:

```yaml
version: '3.8'

services:
  auth-service:
    environment:
      - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
      - GOOGLE_REDIRECT_URI=http://your-backend-domain:9090/login/oauth2/code/google
      - BASE_URL=http://your-backend-domain:9090
      - FRONTEND_URL=http://your-frontend-domain:8501
```

#### Streamlit Frontend (Separate Docker)

Create `streamlit-frontend/.env`:
```bash
API_GATEWAY_URL=http://your-backend-domain:9090
```

### Scenario 2: Deploy Backend to Cloud + Streamlit Cloud

This is the most common scenario for deploying Streamlit apps.

#### Backend Deployment (AWS/GCP/Azure/etc.)

1. Deploy your Spring Boot services to cloud platform
2. Get your backend URL (e.g., `https://api.yourdomain.com`)

#### Update Google OAuth2 Redirect URIs

Add production redirect URI in Google Cloud Console:
```
https://api.yourdomain.com/login/oauth2/code/google
```

#### Configure Backend Environment Variables

```bash
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret
GOOGLE_REDIRECT_URI=https://api.yourdomain.com/login/oauth2/code/google
BASE_URL=https://api.yourdomain.com
FRONTEND_URL=https://your-streamlit-app.streamlit.app
```

#### Streamlit Cloud Deployment

1. Push your code to GitHub
2. Go to [Streamlit Cloud](https://streamlit.io/cloud)
3. Create new app from your repository
4. Configure **Secrets** in Streamlit Cloud:

```toml
# .streamlit/secrets.toml
API_GATEWAY_URL = "https://api.yourdomain.com"
```

5. Update `streamlit-frontend/.env.example` to use secrets:

```python
# In utils/api_client.py
import os
import streamlit as st

class APIClient:
    def __init__(self):
        # Try to get from Streamlit secrets first (for cloud deployment)
        # Fall back to environment variable for local development
        self.base_url = st.secrets.get("API_GATEWAY_URL",
                                       os.getenv("API_GATEWAY_URL", "http://localhost:9090"))
```

### Scenario 3: Backend in Docker + Frontend on Streamlit Cloud

#### Backend Environment (.env)
```bash
FRONTEND_URL=https://your-app.streamlit.app
GOOGLE_REDIRECT_URI=http://your-docker-host:9090/login/oauth2/code/google
BASE_URL=http://your-docker-host:9090
```

#### Streamlit Secrets
```toml
API_GATEWAY_URL = "http://your-docker-host:9090"
```

---

## Environment Variables Reference

### Backend Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `GOOGLE_CLIENT_ID` | OAuth2 client ID from Google Cloud Console | `123456-abc.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | OAuth2 client secret | `GOCSPX-xxxxx` |
| `GOOGLE_REDIRECT_URI` | Redirect URI registered in Google Console | `http://localhost:9090/login/oauth2/code/google` |
| `BASE_URL` | Backend API Gateway URL | `http://localhost:9090` |
| `FRONTEND_URL` | Streamlit frontend URL for OAuth2 callbacks | `http://localhost:8501` |
| `JWT_SECRET` | Secret key for JWT signing (min 256 bits) | `your-secret-key` |
| `JWT_EXPIRATION` | JWT expiration time in milliseconds | `86400000` (24 hours) |

### Frontend Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `API_GATEWAY_URL` | Backend API Gateway base URL | `http://localhost:9090` |

---

## Important Configuration Notes

### 1. CORS Configuration

Ensure your API Gateway allows requests from your Streamlit frontend domain:

```java
// In ApiGatewayConfiguration.java
.cors(cors -> cors.configurationSource(request -> {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "http://localhost:8501",           // Local Streamlit
        "https://your-app.streamlit.app"   // Production Streamlit
    ));
    config.setAllowedMethods(List.of("*"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    return config;
}))
```

### 2. Google OAuth2 Redirect URIs

**CRITICAL**: The redirect URI in your `.env` file **MUST EXACTLY MATCH** one of the URIs configured in Google Cloud Console.

Common mistakes:
- ❌ `http://localhost:9090/login/oauth2/code/google` vs `https://localhost:9090/login/oauth2/code/google`
- ❌ Trailing slashes: `/google` vs `/google/`
- ❌ Different ports

### 3. Streamlit Page Names

The OAuth2 callback page is named `6_🔐_OAuth2_Callback.py`. Streamlit converts this to the URL path automatically. If you rename this file, update the redirect URL in `OAuth2LoginSuccessHandler.java`:

```java
String redirectUrl = String.format("%s/6_🔐_OAuth2_Callback?token=%s&username=%s&email=%s",
        frontendUrl, jwtToken, user.getUsername(), user.getEmail());
```

---

## Troubleshooting

### Issue: "redirect_uri_mismatch" Error

**Cause**: The redirect URI in your request doesn't match Google Cloud Console configuration.

**Solution**:
1. Check backend logs for the exact redirect URI being used
2. Ensure it exactly matches one in Google Cloud Console → Credentials → OAuth 2.0 Client IDs
3. Remember: `http` vs `https`, `localhost` vs `127.0.0.1`, trailing slashes all matter

### Issue: OAuth2 Login Works but Streamlit Shows "No authentication data found"

**Cause**: The callback page isn't receiving the query parameters.

**Solution**:
1. Check that `FRONTEND_URL` in backend matches your Streamlit URL
2. Verify the page name in redirect URL matches the actual Streamlit page
3. Check browser console for any errors
4. Ensure Streamlit isn't stripping query parameters

### Issue: "Invalid JWT token" After OAuth2 Login

**Cause**: JWT secret mismatch between auth-service and api-gateway.

**Solution**:
1. Ensure both services use the same `JWT_SECRET` environment variable
2. Restart both services after updating

### Issue: CORS Errors in Browser Console

**Cause**: API Gateway not configured to allow requests from Streamlit domain.

**Solution**:
1. Update CORS configuration in API Gateway
2. Add your Streamlit domain to allowed origins
3. Restart API Gateway

### Issue: OAuth2 Redirect Goes to Wrong URL

**Cause**: `FRONTEND_URL` environment variable not set or incorrect.

**Solution**:
1. Set `FRONTEND_URL` in backend `.env` file
2. Restart auth-service
3. Check logs to confirm redirect URL

---

## Security Best Practices

### Production Deployment

1. **Use HTTPS**: Always use HTTPS for production deployments
   - Update redirect URIs to use `https://`
   - Update `BASE_URL` and `FRONTEND_URL` to use `https://`

2. **Secure JWT Secret**: Use a strong, randomly generated secret
   ```bash
   # Generate secure secret (256 bits)
   openssl rand -base64 32
   ```

3. **Environment Variables**: Never commit `.env` files with real credentials
   - Use secret management services (AWS Secrets Manager, etc.)
   - In Streamlit Cloud, use Secrets management

4. **Token Expiration**: Set reasonable JWT expiration times
   - Development: 24 hours (`86400000`)
   - Production: Consider shorter times (1-2 hours)

5. **Validate Redirect URIs**: Only add trusted domains to Google OAuth2 configuration

---

## Testing Your Deployment

### Quick Test Checklist

- [ ] Click "Login with Google" button in Streamlit sidebar
- [ ] Redirected to Google consent screen
- [ ] Can authorize the application
- [ ] Redirected back to Streamlit
- [ ] Token is stored and user is shown as logged in
- [ ] Can access protected endpoints (Profile, My Reviews)
- [ ] JWT token is included in API requests
- [ ] Logout works correctly

### Backend Health Checks

```bash
# Check if services are running
curl http://localhost:8761/  # Eureka
curl http://localhost:8080/actuator/health  # Auth Service
curl http://localhost:9090/actuator/health  # API Gateway
```

### Frontend Health Check

```bash
# Check if Streamlit is accessible
curl http://localhost:8501
```

---

## Additional Resources

- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Streamlit Secrets Management](https://docs.streamlit.io/streamlit-community-cloud/deploy-your-app/secrets-management)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)

---

## Support

If you encounter issues not covered in this guide:
1. Check backend logs for error messages
2. Check browser console for frontend errors
3. Verify all environment variables are set correctly
4. Ensure Google OAuth2 credentials are valid and redirect URIs match

For more help, refer to the main project README or create an issue in the repository.
