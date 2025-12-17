# Security Implementation Design for Books E-commerce Microservices

## Executive Summary

This document provides a comprehensive security design for your Books E-commerce microservices project. 
The design is based on your existing Spring Security implementation patterns (JWT-based authentication) and follows 
a **Hybrid Security Architecture** that balances security, performance, and maintainability.

---

## Current State Analysis

### Architecture Overview
Your microservices ecosystem consists of:
- **Eureka Server** (Port 8761) - Service discovery
- **API Gateway** (Port 9090) - Spring Cloud Gateway routing
- **Book Service** (Port 8000) - Main business service
- **Category Service** (Port 8100) - Category management
- **Price Service** (Port 8200) - Pricing information
- **Inventory Service** (Port 8300) - Stock management

### Security Gaps Identified
- ❌ No authentication mechanism
- ❌ All endpoints publicly accessible
- ❌ No authorization/role-based access control
- ❌ Inter-service communication unprotected
- ❌ CORS configured with wildcard (allowed-origins: "*")
- ❌ Hardcoded database credentials
- ❌ No user management system

### Your Existing Security Patterns
From `D:\Spring Boot\Spring Security`:
- ✅ JWT-based authentication (JJWT 0.12.5)
- ✅ Spring Security 6+ with SecurityFilterChain pattern
- ✅ BCryptPasswordEncoder (strength 12)
- ✅ JwtAuthenticationFilter extending OncePerRequestFilter
- ✅ User entity implementing UserDetails
- ✅ Role-based access control with @PreAuthorize
- ✅ Three roles: ROLE_USER, ROLE_ADMIN, ROLE_CUSTOMER
- ✅ OAuth2 integration with Google (optional feature)
- ✅ Modern @EnableMethodSecurity annotation

---

## Proposed Security Architecture

### Architecture Decision: Hybrid Security Model

**Strategy Overview:**

```
┌─────────────────────────────────────────────────────────────┐
│                    External Clients                         │
└────────────────────────┬────────────────────────────────────┘
                         │ JWT Token Required
                         ↓
        ┌────────────────────────────────┐
        │     API Gateway (9090)         │
        │  ✓ JWT Validation (Primary)    │
        │  ✓ Authentication Filter       │
        │  ✓ Route to Services           │
        └────────┬───────────┬───────────┘
                 │           │ JWT Token Forwarded
    ┌────────────┼───────────┼────────────┐
    │            │           │            │
    ↓            ↓           ↓            ↓
┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐
│ Auth   │  │ Book   │  │Category│  │ Price  │  │Inventor│
│Service │  │Service │  │Service │  │Service │  │Service │
│(8400)  │  │(8000)  │  │(8100)  │  │(8200)  │  │(8300)  │
│        │  │        │  │        │  │        │  │        │
│ Full   │  │ JWT    │  │ JWT    │  │ JWT    │  │ JWT    │
│Security│  │ Parsing│  │ Parsing│  │ Parsing│  │ Parsing│
│        │  │ @PreAut│  │ @PreAut│  │ @PreAut│  │ @PreAut│
└────────┘  └────────┘  └────────┘  └────────┘  └────────┘
     │           │           │           │           │
     └───────────┴───────────┴───────────┴───────────┘
                 │ Service Discovery
                 ↓
        ┌──────────────────────────────┐
        │   Eureka Server (8761)       │
        └──────────────────────────────┘
```

### Security Layers

**Layer 1: API Gateway (Primary Defense)**
- **Responsibility**: Validate ALL incoming JWT tokens from external clients
- **Action**: Extract token from `Authorization: Bearer <token>` header
- **Validation**: Full JWT signature verification, expiration check
- **On Success**: Forward request with token to downstream service
- **On Failure**: Return 401 Unauthorized with JSON error

**Layer 2: Auth Service (Token Generator)**
- **Responsibility**: User management and JWT token generation
- **Endpoints**: Registration, login, token validation
- **Storage**: User database with encrypted passwords
- **Token Claims**: username, email, roles, expiration

**Layer 3: Individual Services (Role Enforcement)**
- **Responsibility**: Parse JWT token and enforce role-based access
- **Validation**: Parse token only (trust Gateway's validation)
- **Authorization**: Use @PreAuthorize annotations on endpoints
- **Context**: Extract user details from token for business logic

**Layer 4: Inter-Service Communication (Trusted)**
- **Responsibility**: Service-to-service calls
- **Approach**: Propagate JWT token through Feign clients OR trust internal calls
- **Security**: Services behind Gateway = trusted network zone

---

## Detailed Design

### 1. New Service: Auth Service (Port 8400)

**Purpose**: Centralized authentication and user management

**Location**: `D:\Spring Boot\Books-Ecommerce Project\auth-service`

#### Database Schema
```
Database: auth_db

Table: users
- id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- username (VARCHAR(255), UNIQUE, NOT NULL)
- password (VARCHAR(255), NOT NULL) -- BCrypt encrypted
- email (VARCHAR(255), UNIQUE, NOT NULL)

Table: user_roles
- user_id (BIGINT, FOREIGN KEY -> users.id)
- role (VARCHAR(50)) -- ROLE_USER, ROLE_ADMIN, ROLE_CUSTOMER
```

#### Key Components to Implement

**1.1. Entity Layer**
- `User.java` - Implements UserDetails interface
  - Fields: id, username, password (encrypted), email
  - Collection: Set<Role> roles (EAGER fetch)
  - Methods: getAuthorities() returns roles as GrantedAuthority

- `Role.java` - Enum
  - Values: ROLE_USER, ROLE_ADMIN, ROLE_CUSTOMER

**1.2. Repository Layer**
- `UserRepository.java` - Extends JpaRepository
  - Methods: findByUsername(), findByEmail()

**1.3. Service Layer**
- `UserService.java` - Implements UserDetailsService
  - loadUserByUsername() - For Spring Security authentication
  - createUser() - Register new user with password encryption
  - getAllUsers() - Admin function
  - findUserById() - User management

**1.4. Security Configuration**
- `SecurityConfig.java`
  - SecurityFilterChain bean
  - Public endpoints: /api/v1/auth/register, /api/v1/auth/login
  - Authenticated endpoints: /api/v1/users/**
  - CSRF disabled (stateless API)
  - Session management: STATELESS
  - Add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter

- `PasswordEncoderConfig.java`
  - BCryptPasswordEncoder bean with strength 12

**1.5. JWT Components**
- `JwtUtil.java` - Core JWT utility
  - generateToken(UserDetails) - Create JWT with claims
  - extractUsername(String token) - Parse token
  - extractRoles(String token) - Get user roles
  - validateToken(String token, UserDetails) - Verify signature and expiration
  - Configuration: JWT secret, expiration time (24 hours recommended)

- `JwtAuthenticationFilter.java` - Extends OncePerRequestFilter
  - Extract JWT from Authorization header
  - Validate token
  - Load UserDetails from database
  - Create Authentication object
  - Set in SecurityContext

**1.6. Controller Layer**
- `AuthController.java`
  - POST /api/v1/auth/register - User registration
  - POST /api/v1/auth/login - Login and JWT generation
  - GET /api/v1/auth/validate - Token validation (for Gateway)

- `UserController.java`
  - GET /api/v1/users - List all users (@PreAuthorize("hasRole('ROLE_ADMIN')"))
  - GET /api/v1/users/{id} - Get user by ID (Admin or own profile)

**1.7. DTOs**
- `UserRequestDTO` - Registration data (username, email, password, verifyPassword)
- `UserResponseDTO` - User data without password
- `LoginRequestDTO` - Login credentials (username, password)
- `LoginResponseDTO` - Token response (token, type, expiresIn, username, email)

**1.8. Configuration (application.yaml)**
```yaml
spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8400

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

jwt:
  secret: ${JWT_SECRET:default-secret-change-in-production}
  expiration: 86400000  # 24 hours in milliseconds
```

#### Optional: OAuth2 Integration
- `OAuth2AuthenticationSuccessHandler.java`
  - Handle Google OAuth2 login success
  - Create/find user in database
  - Generate JWT token
  - Return token to client

---

### 2. API Gateway Security

**Purpose**: Validate all external requests before routing to services

**Location**: `D:\Spring Boot\Books-Ecommerce Project\api-gateway`

#### Key Components to Modify/Create

**2.1. JWT Components**
- `JwtUtil.java` (Gateway version)
  - Lightweight utility for token validation
  - extractUsername(), extractRoles(), isTokenValid()
  - Shared JWT secret with Auth Service

- `JwtAuthenticationGatewayFilter.java` - Implements GlobalFilter
  - Order: Highest priority
  - Check if request path is public (registration, login, GET endpoints)
  - Extract JWT from Authorization header
  - Validate token signature and expiration
  - Extract user context (username, roles)
  - Add user context as headers for downstream services:
    - X-User-Name: username
    - X-User-Roles: comma-separated roles
  - Return 401 if token invalid/missing for secured endpoints

**2.2. Configuration Updates**

**File**: `ApiGatewayConfiguration.java`
- Add route for auth-service:
  ```
  /auth/** -> lb://AUTH-SERVICE (strip prefix 1)
  ```

**File**: `application.yaml` additions
```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-change-in-production}

spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "http://localhost:3000"  # Your frontend URL
            allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
            allowed-headers: [Authorization, Content-Type]
            exposed-headers: [Authorization]
```

**2.3. Public vs. Secured Endpoints**

**Public Endpoints** (no token required):
- POST /auth/api/v1/auth/register
- POST /auth/api/v1/auth/login
- POST /auth/api/v1/auth/oauth2/**
- GET /books/api/v1/books (list books)
- GET /books/api/v1/books/{id} (view book details)
- GET /category/api/v1/category
- GET /price/api/v1/price/**
- GET /inventory/api/v1/inventory/**
- GET /actuator/health (all services)

**Secured Endpoints** (token required):
- POST /books/api/v1/books (requires ROLE_ADMIN)
- PUT /books/api/v1/books/{id} (requires ROLE_ADMIN)
- DELETE /books/api/v1/books/{id} (requires ROLE_ADMIN)
- Similar patterns for category, price, inventory services

**2.4. Dependencies to Add**
- io.jsonwebtoken:jjwt-api:0.12.5
- io.jsonwebtoken:jjwt-impl:0.12.5 (runtime)
- io.jsonwebtoken:jjwt-jackson:0.12.5 (runtime)

---

### 3. Individual Microservices Security

**Services to Secure**: Book, Category, Price, Inventory

**Purpose**: Parse JWT token and enforce role-based access control

#### Key Components for Each Service

**3.1. JWT Components**
- `JwtUtil.java` (Lightweight parser)
  - extractUsername() - Parse token claims
  - extractRoles() - Get user roles
  - **No validation needed** - Gateway already validated

- `JwtAuthenticationFilter.java` - Extends OncePerRequestFilter
  - Extract JWT from Authorization header
  - Parse token to get username and roles
  - Create UsernamePasswordAuthenticationToken with authorities
  - Set in SecurityContext
  - **Skip validation** - trust Gateway

**3.2. Security Configuration**
- `SecurityConfig.java`
  - Enable: @EnableWebSecurity, @EnableMethodSecurity
  - SecurityFilterChain configuration:
    - CSRF disabled
    - Session management: STATELESS
    - Public endpoints:
      - GET requests: /api/v1/** (public read access)
      - Internal endpoints: /api/v1/**/exists, /api/v1/*/book/** (for Feign calls)
    - Authenticated endpoints: POST, PUT, DELETE requests
    - Add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter

**3.3. Controller Authorization**

Apply `@PreAuthorize` annotations to controllers:

**Book Service** (`BookController.java`):
- POST /api/v1/books - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- PUT /api/v1/books/{id} - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- DELETE /api/v1/books/{id} - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- GET /api/v1/books - Public (no annotation)
- GET /api/v1/books/{id} - Public
- GET /api/v1/books/{id}/exists - Internal (permitted by SecurityConfig)

**Category Service** (`CategoryController.java`):
- POST /api/v1/category - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- PUT /api/v1/category/{id} - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- DELETE /api/v1/category/{id} - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- GET endpoints - Public

**Price Service** (`PriceController.java`):
- POST /api/v1/price - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- PUT /api/v1/price/{id} - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- DELETE /api/v1/price/{id} - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- DELETE /api/v1/price/book/{bookId} - Internal
- GET endpoints - Public

**Inventory Service** (`InventoryController.java`):
- POST /api/v1/inventory - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- PUT /api/v1/inventory/{id} - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- DELETE /api/v1/inventory/{id} - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- DELETE /api/v1/inventory/book/{bookId} - Internal
- GET endpoints - Public

**3.4. Configuration Updates**

**File**: `application.yaml` (add to each service)
```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-change-in-production}
```

**3.5. Dependencies to Add** (each service)
- spring-boot-starter-security
- io.jsonwebtoken:jjwt-api:0.12.5
- io.jsonwebtoken:jjwt-impl:0.12.5 (runtime)
- io.jsonwebtoken:jjwt-jackson:0.12.5 (runtime)

---

### 4. Inter-Service Communication Security

**Current State**: Services communicate via OpenFeign clients

#### Option 1: Token Propagation (Recommended)

**Approach**: Propagate JWT token from original request through Feign calls

**Implementation**:
- Create `FeignClientInterceptor.java` in each service with Feign clients
  - Implements RequestInterceptor
  - Extract JWT from SecurityContext
  - Add Authorization header to Feign request template

- Create `FeignConfig.java`
  - Register FeignClientInterceptor as bean

- Update Feign client interfaces:
  - Add `configuration = FeignConfig.class` to @FeignClient annotation

**Services Requiring This**:
- Book Service (calls Category, Price, Inventory)
- Price Service (calls Book)
- Inventory Service (calls Book)

#### Option 2: Trust Internal Calls (Simpler Alternative)

**Approach**: Services trust calls from other services (no token propagation)

**Rationale**:
- Services are behind API Gateway
- Gateway validates external requests
- Internal network is trusted zone

**Implementation**:
- In SecurityConfig, permit internal endpoints:
  ```
  .requestMatchers("/api/v1/**/exists", "/api/v1/*/book/**").permitAll()
  ```
- No Feign interceptor needed
- Simpler implementation, suitable for trusted environments

**Recommendation**: Start with Option 2 for simplicity. Upgrade to Option 1 if deploying to untrusted environments.

---

### 5. JWT Token Design

#### Token Structure
```json
{
  "sub": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER", "ROLE_ADMIN"],
  "iat": 1701234567,
  "exp": 1701320967
}
```

#### Token Configuration
- **Algorithm**: HMAC-SHA256 (HS256)
- **Secret Key**: Minimum 256 bits (32+ characters)
  - Development: Configured in application.yaml
  - Production: Environment variable `JWT_SECRET`
- **Expiration**: 24 hours (86400000 ms)
  - Configurable via `jwt.expiration` property
  - Can be adjusted based on security requirements
- **Header Format**: `Authorization: Bearer <token>`

#### Token Lifecycle
1. **Generation**: Auth Service creates token on successful login
2. **Validation**: API Gateway validates on every external request
3. **Parsing**: Individual services parse to extract user context
4. **Expiration**: Token expires after configured time, user must re-login

---

### 6. Role-Based Access Control Matrix

| Service | Endpoint | Method | Public | ROLE_USER | ROLE_ADMIN |
|---------|----------|--------|--------|-----------|------------|
| **Auth** | /auth/register | POST | ✓ | - | - |
| **Auth** | /auth/login | POST | ✓ | - | - |
| **Auth** | /users | GET | - | - | ✓ |
| **Auth** | /users/{id} | GET | - | ✓ (own) | ✓ (all) |
| **Book** | /books | GET | ✓ | ✓ | ✓ |
| **Book** | /books/{id} | GET | ✓ | ✓ | ✓ |
| **Book** | /books | POST | - | - | ✓ |
| **Book** | /books/{id} | PUT | - | - | ✓ |
| **Book** | /books/{id} | DELETE | - | - | ✓ |
| **Category** | /category | GET | ✓ | ✓ | ✓ |
| **Category** | /category | POST | - | - | ✓ |
| **Category** | /category/{id} | PUT | - | - | ✓ |
| **Category** | /category/{id} | DELETE | - | - | ✓ |
| **Price** | /price/** | GET | ✓ | ✓ | ✓ |
| **Price** | /price | POST | - | - | ✓ |
| **Price** | /price/{id} | PUT | - | - | ✓ |
| **Price** | /price/{id} | DELETE | - | - | ✓ |
| **Inventory** | /inventory/** | GET | ✓ | ✓ | ✓ |
| **Inventory** | /inventory | POST | - | - | ✓ |
| **Inventory** | /inventory/{id} | PUT | - | - | ✓ |
| **Inventory** | /inventory/{id} | DELETE | - | - | ✓ |

**Legend**:
- ✓ = Allowed
- \- = Not allowed
- (own) = Only for own user ID
- (all) = All resources

---

## Implementation Roadmap

### Phase 1: Foundation (Priority: CRITICAL)
**Timeline**: Days 1-3

**Tasks**:
1. Create Auth Service project structure
2. Copy security components from reference project (`D:\Spring Boot\Spring Security`)
3. Implement User entity, repository, and service
4. Implement JWT utility classes
5. Create authentication controllers (register, login)
6. Configure database (auth_db)
7. Test authentication flow

**Validation**:
- ✓ User can register successfully
- ✓ User can login and receive JWT token
- ✓ Token can be validated
- ✓ Service registered with Eureka

**Critical Files**:
- `auth-service/pom.xml`
- `auth-service/src/main/java/com/authservice/entity/User.java`
- `auth-service/src/main/java/com/authservice/util/JwtUtil.java`
- `auth-service/src/main/java/com/authservice/config/SecurityConfig.java`
- `auth-service/src/main/java/com/authservice/filter/JwtAuthenticationFilter.java`
- `auth-service/src/main/java/com/authservice/controller/AuthController.java`
- `auth-service/src/main/resources/application.yaml`

---

### Phase 2: API Gateway Security (Priority: HIGH)
**Timeline**: Days 4-5

**Tasks**:
1. Add JWT dependencies to API Gateway
2. Create JWT validation filter
3. Implement JWT utility for token parsing
4. Configure public vs. secured routes
5. Add route for Auth Service
6. Update CORS configuration
7. Test gateway security

**Validation**:
- ✓ Public endpoints accessible without token
- ✓ Secured endpoints require valid token
- ✓ Invalid tokens return 401 Unauthorized
- ✓ Valid tokens routed to services
- ✓ Auth service accessible through gateway

**Critical Files**:
- `api-gateway/pom.xml`
- `api-gateway/src/main/java/com/inventory/apigateway/filter/JwtAuthenticationGatewayFilter.java`
- `api-gateway/src/main/java/com/inventory/apigateway/util/JwtUtil.java`
- `api-gateway/src/main/java/com/inventory/apigateway/ApiGatewayConfiguration.java`
- `api-gateway/src/main/resources/application.yaml`

---

### Phase 3: Secure Book Service (Priority: MEDIUM)
**Timeline**: Days 6-7

**Tasks**:
1. Add Spring Security and JWT dependencies
2. Create JWT parsing utility (no validation)
3. Create JWT authentication filter
4. Configure SecurityFilterChain
5. Add @PreAuthorize annotations to controller methods
6. Test with valid admin token
7. Test with regular user token
8. Test inter-service calls

**Validation**:
- ✓ Admin can create/update/delete books
- ✓ Regular users cannot perform admin operations (403 Forbidden)
- ✓ GET requests work without token
- ✓ Feign calls to Category/Price/Inventory work

**Critical Files**:
- `book-service/pom.xml`
- `book-service/src/main/java/com/ecommerce_books/book_service/config/SecurityConfig.java`
- `book-service/src/main/java/com/ecommerce_books/book_service/util/JwtUtil.java`
- `book-service/src/main/java/com/ecommerce_books/book_service/filter/JwtAuthenticationFilter.java`
- `book-service/src/main/java/com/ecommerce_books/book_service/controller/BookController.java`
- `book-service/src/main/resources/application.yaml`

---

### Phase 4: Secure Remaining Services (Priority: MEDIUM)
**Timeline**: Days 8-10

**Tasks**:
1. Apply same security pattern to Category Service
2. Apply same security pattern to Price Service
3. Apply same security pattern to Inventory Service
4. Test each service independently
5. Test complete workflows through Gateway

**Validation** (for each service):
- ✓ Admin operations require ROLE_ADMIN
- ✓ Public GET requests work
- ✓ Token parsing extracts user context
- ✓ @PreAuthorize enforces access control

**Critical Files** (replicate for each service):
- `{service}/pom.xml`
- `{service}/src/main/java/com/{package}/config/SecurityConfig.java`
- `{service}/src/main/java/com/{package}/util/JwtUtil.java`
- `{service}/src/main/java/com/{package}/filter/JwtAuthenticationFilter.java`
- `{service}/src/main/java/com/{package}/controller/*Controller.java`
- `{service}/src/main/resources/application.yaml`

---

### Phase 5: Inter-Service Authentication (Priority: LOW)
**Timeline**: Days 11-12

**Tasks**:
1. Decide: Token propagation vs. trust internal calls
2. If propagating: Create Feign interceptors
3. If propagating: Configure Feign clients
4. Test inter-service calls
5. Verify authentication flows work end-to-end

**Validation**:
- ✓ Book creation triggers Price/Inventory/Category calls
- ✓ All inter-service calls succeed
- ✓ User context preserved through call chain

**Critical Files** (if implementing token propagation):
- `book-service/src/main/java/com/ecommerce_books/book_service/config/FeignConfig.java`
- `book-service/src/main/java/com/ecommerce_books/book_service/interceptor/FeignClientInterceptor.java`
- Similar files for Price and Inventory services

---

### Phase 6: OAuth2 Integration (Priority: OPTIONAL)
**Timeline**: Days 13-14

**Tasks**:
1. Configure Google OAuth2 credentials
2. Update Auth Service security config
3. Create OAuth2 success handler
4. Test OAuth2 login flow
5. Verify JWT generation after OAuth2 login

**Validation**:
- ✓ User can login with Google
- ✓ OAuth2 flow generates JWT token
- ✓ Token works for API access

**Critical Files**:
- `auth-service/src/main/resources/application.yaml` (OAuth2 config)
- `auth-service/src/main/java/com/authservice/handler/OAuth2AuthenticationSuccessHandler.java`
- `auth-service/src/main/java/com/authservice/config/SecurityConfig.java`

---

## Configuration Management

### Environment Variables

**Production Deployment**: Use environment variables for sensitive configuration

**Required Variables**:
```bash
# JWT Secret (shared across Auth Service, Gateway, and all microservices)
JWT_SECRET=your-super-secret-key-minimum-256-bits-long

# Database credentials (if not using default)
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# OAuth2 credentials (if using Google login)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

**Setting Environment Variables**:

Windows:
```cmd
set JWT_SECRET=your-secret-key
```

Linux/Mac:
```bash
export JWT_SECRET=your-secret-key
```

Docker:
```yaml
environment:
  - JWT_SECRET=your-secret-key
```

### Configuration Files Summary

**Auth Service** (`application.yaml`):
```yaml
server.port: 8400
spring.application.name: auth-service
spring.datasource.url: jdbc:mysql://localhost:3306/auth_db
jwt.secret: ${JWT_SECRET}
jwt.expiration: 86400000
```

**API Gateway** (`application.yaml` - additions):
```yaml
jwt.secret: ${JWT_SECRET}
# Add route for auth-service
```

**All Microservices** (`application.yaml` - additions):
```yaml
jwt.secret: ${JWT_SECRET}
```

---

## Testing Strategy

### Unit Testing
**Scope**: Test individual security components

**Test Cases**:
1. **JwtUtil Tests**
   - Test token generation
   - Test token parsing
   - Test token validation
   - Test expiration handling
   - Test invalid token handling

2. **Filter Tests**
   - Test with valid token
   - Test with invalid token
   - Test with missing token
   - Test public endpoint bypass

3. **SecurityConfig Tests**
   - Test public endpoints accessible
   - Test secured endpoints require auth
   - Test role-based access

### Integration Testing
**Scope**: Test complete authentication flows

**Test Scenarios**:

**Scenario 1: User Registration and Login**
1. Register new user
2. Verify user created in database
3. Login with credentials
4. Verify JWT token received
5. Verify token contains correct claims

**Scenario 2: Access Control**
1. Create admin user and regular user
2. Login as admin, get token
3. Access admin endpoint - should succeed
4. Login as regular user, get token
5. Access admin endpoint - should fail with 403
6. Access public endpoint - should succeed

**Scenario 3: Token Expiration**
1. Login and get token
2. Wait for expiration
3. Attempt to access secured endpoint
4. Verify 401 Unauthorized response

**Scenario 4: Inter-Service Communication**
1. Login as admin
2. Create book (triggers calls to Price, Inventory, Category)
3. Verify book created with all related data
4. Verify inter-service calls succeeded

**Scenario 5: Gateway Routing**
1. Access endpoints through Gateway
2. Verify requests routed to correct services
3. Verify JWT validation at Gateway
4. Verify user context passed to services

### Testing Tools
- **Postman/Insomnia**: Manual API testing
- **JUnit 5**: Unit tests
- **MockMvc**: Controller testing
- **Spring Security Test**: Security context testing
- **TestContainers**: Database testing

### Sample Test Requests

**1. Register User**
```
POST http://localhost:9090/auth/api/v1/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "verifyPassword": "SecurePass123!"
}
```

**2. Login**
```
POST http://localhost:9090/auth/api/v1/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123!"
}

Response:
{
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400,
  "username": "john_doe",
  "email": "john@example.com"
}
```

**3. Access Public Endpoint**
```
GET http://localhost:9090/books/api/v1/books

(No Authorization header needed)
```

**4. Access Secured Endpoint**
```
POST http://localhost:9090/books/api/v1/books
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "New Book",
  "description": "Book description",
  ...
}
```

---

## Security Best Practices

### Production Checklist

**Authentication & Authorization**:
- [ ] Use strong JWT secret (minimum 256 bits)
- [ ] Store secrets in environment variables or vault
- [ ] Set appropriate token expiration (24 hours recommended)
- [ ] Implement token refresh mechanism (future enhancement)
- [ ] Enforce password complexity rules
- [ ] Implement account lockout after failed attempts

**Network Security**:
- [ ] Enable HTTPS/TLS in production (disable HTTP)
- [ ] Configure CORS with specific origins (no wildcards)
- [ ] Use security headers (X-Frame-Options, X-Content-Type-Options, etc.)
- [ ] Implement rate limiting on auth endpoints
- [ ] Add request/response logging for auditing

**Database Security**:
- [ ] Use environment variables for database credentials
- [ ] Enable database SSL connections
- [ ] Implement database access auditing
- [ ] Regular backup strategy
- [ ] Principle of least privilege for database users

**Service Security**:
- [ ] Keep dependencies up to date
- [ ] Regular security vulnerability scans
- [ ] Implement API versioning
- [ ] Add health check endpoints
- [ ] Monitor for suspicious activity

**Deployment**:
- [ ] Use container orchestration (Docker, Kubernetes)
- [ ] Implement service mesh for advanced security (optional)
- [ ] Configure proper firewall rules
- [ ] Regular security audits
- [ ] Incident response plan

### JWT Secret Management

**Development**:
- Use default secret in application.yaml
- Share secret across team via secure channel

**Staging/Production**:
- Generate strong random secret: `openssl rand -base64 32`
- Store in secure vault (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
- Rotate secrets periodically
- Use different secrets for each environment

### Password Security

**Requirements** (implement in UserService):
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character
- No common passwords (check against dictionary)

**Storage**:
- Always use BCrypt (already configured with strength 12)
- Never log passwords
- Never expose passwords in API responses
- Implement password reset flow (future enhancement)

### CORS Configuration

**Development**:
```yaml
allowed-origins: "http://localhost:3000"
```

**Production**:
```yaml
allowed-origins: "https://yourdomain.com"
```

**Multiple Origins**:
```yaml
allowed-origins:
  - "https://yourdomain.com"
  - "https://www.yourdomain.com"
  - "https://admin.yourdomain.com"
```

---

## Troubleshooting Guide

### Common Issues and Solutions

**Issue 1: 401 Unauthorized despite valid token**
- **Cause**: JWT secret mismatch between services
- **Solution**: Verify JWT_SECRET environment variable is consistent across Auth Service, Gateway, and all microservices
- **Check**: Log the secret (first 5 chars only) in each service for debugging

**Issue 2: Token parsing fails**
- **Cause**: Token format incorrect or corrupted
- **Solution**: Verify Authorization header format: `Bearer <token>` (note the space)
- **Check**: Token should start with `eyJ` (base64 encoded JSON)

**Issue 3: 403 Forbidden for admin operations**
- **Cause**: Roles not properly extracted from token or @PreAuthorize misconfigured
- **Solution**:
  - Verify token contains roles in claims
  - Check role format: must be `ROLE_ADMIN`, not just `ADMIN`
  - Verify @EnableMethodSecurity is present in SecurityConfig
- **Debug**: Log extracted roles in filter

**Issue 4: Inter-service calls fail with 401**
- **Cause**: Token not propagated to downstream services
- **Solution**:
  - If using Feign interceptor: Verify interceptor is registered
  - If trusting internal calls: Verify SecurityConfig permits internal endpoints
- **Debug**: Log Authorization header in Feign requests

**Issue 5: Gateway doesn't validate tokens**
- **Cause**: Filter not registered or order incorrect
- **Solution**:
  - Verify filter implements GlobalFilter
  - Check getOrder() returns high priority value
  - Verify filter is a Spring @Component
- **Debug**: Add logging in filter's filter() method

**Issue 6: CORS errors in browser**
- **Cause**: CORS configuration incorrect or not applied
- **Solution**:
  - Verify globalcors configuration in Gateway
  - Add OPTIONS to allowed methods
  - Check allowed-origins matches frontend URL exactly
- **Debug**: Check browser console for specific CORS error

**Issue 7: Token expires too quickly**
- **Cause**: Expiration set too short (default 2 minutes in reference)
- **Solution**: Update jwt.expiration in Auth Service to 86400000 (24 hours)
- **Note**: Consider implementing refresh tokens for better UX

**Issue 8: User roles not saved**
- **Cause**: @ElementCollection fetch type or transaction issues
- **Solution**:
  - Verify User entity has @ElementCollection(fetch = FetchType.EAGER)
  - Use userRepository.saveAndFlush() instead of save()
- **Check**: Query database directly to verify roles table

---

## Architecture Trade-offs

### Decision 1: Gateway Validation vs. Service Validation

**Chosen**: Gateway validates, services parse only

**Pros**:
- Single point of validation (DRY principle)
- Better performance (validation once, not N times)
- Simpler service implementation
- Easier to update validation logic

**Cons**:
- Services vulnerable if accessed directly (not through Gateway)
- Requires network trust assumption

**Mitigation**:
- Deploy services in private network
- Use service mesh for additional security layer
- Configure firewalls to block direct service access

### Decision 2: Centralized Auth Service vs. Distributed Auth

**Chosen**: Centralized Auth Service

**Pros**:
- Single source of truth for users
- Consistent user management
- Easier to implement OAuth2
- Simpler token generation logic

**Cons**:
- Single point of failure
- Potential bottleneck

**Mitigation**:
- Scale Auth Service horizontally
- Use database replication
- Implement caching for token validation

### Decision 3: Stateless JWT vs. Stateful Sessions

**Chosen**: Stateless JWT

**Pros**:
- Perfect for microservices (no shared session state)
- Horizontally scalable
- Lower memory footprint
- No session storage needed

**Cons**:
- Cannot revoke tokens before expiration
- Slightly larger payload per request

**Mitigation**:
- Short token expiration
- Implement token blacklist (future enhancement)
- Use refresh tokens for long-lived sessions

### Decision 4: Trust Internal Calls vs. Validate All Calls

**Chosen**: Trust internal calls (recommended for simplicity)

**Pros**:
- Simpler implementation
- Better performance
- No token propagation complexity

**Cons**:
- Less secure if internal network compromised

**Alternative**: Propagate tokens through Feign interceptors for higher security

---

## Future Enhancements

### Phase 2 Features (Post-MVP)

**1. Refresh Token Mechanism**
- Issue long-lived refresh tokens
- Use refresh tokens to get new access tokens
- Store refresh tokens in database
- Implement token revocation

**2. Account Management**
- Password reset flow
- Email verification
- Account lockout after failed attempts
- Two-factor authentication (2FA)

**3. Advanced Authorization**
- Fine-grained permissions beyond roles
- Resource-based authorization
- Dynamic permission management

**4. Monitoring & Logging**
- Security event logging
- Failed login attempt tracking
- Token usage analytics
- Anomaly detection

**5. Service-to-Service Security**
- Mutual TLS (mTLS) for service communication
- Service authentication tokens
- API keys for internal services

**6. Rate Limiting**
- Request throttling per user
- IP-based rate limiting
- DDoS protection

**7. API Documentation Security**
- Secure Swagger UI with authentication
- Hide sensitive endpoints in docs
- API key management for documentation

---

## Critical Files Reference

### Files to Create

**Auth Service** (new service):
```
D:\Spring Boot\Books-Ecommerce Project\auth-service\
├── pom.xml
├── src\main\java\com\authservice\
│   ├── AuthServiceApplication.java
│   ├── config\
│   │   ├── SecurityConfig.java
│   │   └── PasswordEncoderConfig.java
│   ├── controller\
│   │   ├── AuthController.java
│   │   └── UserController.java
│   ├── dto\
│   │   ├── UserRequestDTO.java
│   │   ├── UserResponseDTO.java
│   │   ├── LoginRequestDTO.java
│   │   └── LoginResponseDTO.java
│   ├── entity\
│   │   └── User.java
│   ├── enums\
│   │   └── Role.java
│   ├── filter\
│   │   └── JwtAuthenticationFilter.java
│   ├── repository\
│   │   └── UserRepository.java
│   ├── service\
│   │   └── UserService.java
│   ├── util\
│   │   └── JwtUtil.java
│   └── mapper\
│       └── UserMapper.java
└── src\main\resources\
    └── application.yaml
```

**API Gateway** (new files):
```
D:\Spring Boot\Books-Ecommerce Project\api-gateway\
└── src\main\java\com\inventory\apigateway\
    ├── filter\
    │   └── JwtAuthenticationGatewayFilter.java
    └── util\
        └── JwtUtil.java
```

**Each Microservice** (new files):
```
{service}\src\main\java\com\{package}\
├── config\
│   └── SecurityConfig.java
├── filter\
│   └── JwtAuthenticationFilter.java
└── util\
    └── JwtUtil.java

(Optional for token propagation)
├── config\
│   └── FeignConfig.java
└── interceptor\
    └── FeignClientInterceptor.java
```

### Files to Modify

**API Gateway**:
- `api-gateway\pom.xml` - Add JWT dependencies
- `api-gateway\src\main\java\com\inventory\apigateway\ApiGatewayConfiguration.java` - Add auth-service route
- `api-gateway\src\main\resources\application.yaml` - Add JWT config, update CORS

**Book Service**:
- `book-service\pom.xml` - Add Spring Security and JWT dependencies
- `book-service\src\main\java\com\ecommerce_books\book_service\controller\BookController.java` - Add @PreAuthorize
- `book-service\src\main\resources\application.yaml` - Add JWT config

**Category Service**:
- `Category\pom.xml` - Add Spring Security and JWT dependencies
- `Category\src\main\java\com\categoryservice\category\controller\CategoryController.java` - Add @PreAuthorize
- `Category\src\main\resources\application.yaml` - Add JWT config

**Price Service**:
- `price\pom.xml` - Add Spring Security and JWT dependencies
- `price\src\main\java\com\priceservice\price\controller\PriceController.java` - Add @PreAuthorize
- `price\src\main\resources\application.yaml` - Add JWT config

**Inventory Service**:
- `inventory\pom.xml` - Add Spring Security and JWT dependencies
- `inventory\src\main\java\com\booksecommerce\inventory\controller\InventoryController.java` - Add @PreAuthorize
- `inventory\src\main\resources\application.yaml` - Add JWT config

---

## Summary

This security design provides a comprehensive, production-ready authentication and authorization system for your Books E-commerce microservices. The hybrid approach balances security, performance, and maintainability.

**Key Design Principles**:
1. **Centralized Authentication**: Single Auth Service for user management
2. **Gateway-Level Validation**: Primary defense at API Gateway
3. **Service-Level Authorization**: Fine-grained access control with @PreAuthorize
4. **Stateless JWT**: Perfect for microservices architecture
5. **Role-Based Access**: Admin operations require ROLE_ADMIN, reads are public
6. **Trusted Internal Network**: Services trust calls from other services

**Implementation Timeline**: 2-3 weeks
- Week 1: Auth Service + Gateway Security
- Week 2: Secure all microservices
- Week 3: Testing, refinement, optional OAuth2

**Reference Implementation**: `D:\Spring Boot\Spring Security` contains all security patterns to replicate

**Next Steps**:
1. Review this design document
2. Set up JWT_SECRET environment variable
3. Create Auth Service following Phase 1
4. Secure Gateway following Phase 2
5. Secure individual services following Phase 3-4
6. Test thoroughly using provided test scenarios
7. Deploy to production with security checklist

**Support**:
- Refer to your existing Spring Security project for code examples
- Use this document as architectural reference
- Follow implementation roadmap phase by phase
- Test each phase before proceeding to next

This design document should serve as your complete reference for implementing security across your microservices architecture. Follow the phases sequentially, validate at each step, and refer back to this document for architectural decisions and configurations.
