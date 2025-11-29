# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Spring Cloud Gateway** microservice gateway that provides:
- **JWT token authentication** via `CheckTokenFilter` (global filter with order -100)
- **Service discovery** using Nacos
- **Rate limiting** with Redis
- **Path whitelisting** for public endpoints
- **CORS support** for cross-origin requests
- **Bloom filter caching** for performance optimization

## Common Development Commands

### Build & Run
```bash
# Build the project
./mvnw clean package

# Run with specific Spring profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Run a single test
./mvnw test -Dtest=GatewayApplicationTests
```

### Docker
```bash
# Build Docker image (requires built jar)
docker build -t gateway:latest .

# Run with docker-compose (if available)
docker-compose up -d
```

### Key Development Tools
- **Maven wrapper**: `./mvnw` (no need to install Maven)
- **Spring Boot 2.3.12.RELEASE**
- **Java**: Compatible with OpenJDK 8+

## High-Level Architecture

### Core Flow
```
Client Request → CheckTokenFilter (Auth) → Rate Limiter → Route Discovery → Target Service
                     ↓
                 Bloom Filter Cache → JWT Validation
```

### Key Components

1. **CheckTokenFilter** (`src/main/java/com/woody/gateway/filter/CheckTokenFilter.java`)
   - Global filter with order -100 (runs early)
   - Handles JWT token extraction from Authorization header or `jwt` cookie
   - Uses CircleBloomFilter for caching validation results
   - Implements path whitelisting via MyFilterConfiguration
   - Adds user context headers: `userId`, `userName`, `audience`, `traceId`

2. **CircleBloomFilter** (`src/main/java/com/woody/gateway/util/CircleBloomFilter.java`)
   - Circular Bloom Filter pattern with 5 rotating filters
   - Stores validation results: `passed`, `stopped`, `expired` prefixes
   - Prevents repeated JWT parsing for same token
   - Automatically rotates filters every minute
   - Prototype scope bean (new instance per request)

3. **CheckTokenUtil** (`src/main/java/com/woody/gateway/util/CheckTokenUtil.java`)
   - Verifies JWT signatures using ECC public key
   - Public key loaded from `publicKeyPem` configuration
   - Uses BouncyCastle for PEM parsing
   - Returns parsed Claims on success

4. **TokenParse** (`src/main/java/com/woody/gateway/util/TokenParse.java`)
   - Fast token parsing without signature verification
   - Only decodes base64url payload
   - Used for performance when token already validated via Bloom filter

5. **MyFilterConfiguration** (`src/main/java/com/woody/gateway/config/MyFilterConfiguration.java`)
   - Configures path whitelists for public endpoints
   - Supports Nacos dynamic configuration with `@RefreshScope`
   - Uses AntPathMatcher for pattern matching

### Configuration Structure

**application.yaml** (main config):
- **Nacos**: Service discovery at `127.0.0.1:8848`
- **Gateway routes**: Static routes + discovery-based routing
- **Rate limiting**: 15 req/s, burst 30, using Redis
- **CORS**: All origins allowed, all methods
- **Redis**: `127.0.0.1:6379`

**Required External Config** (Nacos or env):
- `publicKeyPem`: RSA public key in PEM format
- `my-filter.config.whiteList`: List of path patterns for public access

### Route Configuration Examples
```yaml
routes:
  - id: test_route
    uri: https://httpbin.org
    predicates:
      - Path=/test/{segment}
  - id: host_route
    uri: http://prd-api:8112
    predicates:
      - Host=api.shwoody.com
```

## Key Implementation Details

### Token Validation Strategy
1. Extract token from header or cookie
2. Check if path is whitelisted → allow through
3. Check CircleBloomFilter cache:
   - `stopped{token}` → return 401
   - `expired{token}` → return 403
   - `passed{token}` → parse with TokenParse, add headers, continue
4. If not cached, validate with CheckTokenUtil:
   - Success → add to Bloom filter, parse claims, set headers
   - Expired → add to expired cache, return 403
   - Invalid → add to stopped cache, return 401

### Performance Optimizations
- **Bloom Filter**: Avoids repeated JWT parsing (expensive crypto operations)
- **Rotating filters**: Prevents memory bloat (5 filters, 1M entries each)
- **Thread-safe**: Uses CopyOnWriteArrayList
- **Rate limiting**: Protects downstream services

### Error Responses
- **401 Unauthorized**: No token or invalid token
- **403 Forbidden**: Token expired
- **200 + JSON**: For OPTIONS requests (CORS preflight)

## CI/CD Pipeline

**GitLab CI** stages:
1. `check` - Deployment validation
2. `maven_build` - Build and test
3. `deploy` - K8s deployment (test/dev/sit/stage/release branches, manual for prod)
4. `notify` - Feishu notifications

**Deployment Strategy**:
- Automatic deploy to test environments (test/dev/sit/stage)
- Manual deploy to pre-prod (release branch)
- Manual deploy to prod (release-v* tags)

## Important Notes

### Dependencies
- **Spring Cloud Gateway** - Core gateway functionality
- **Nacos Discovery** - Service registration/discovery
- **Nacos Config** - Dynamic configuration
- **Redis Reactive** - Rate limiting backend
- **JWT (jjwt)** - Token creation/validation
- **BouncyCastle** - Cryptography (PEM parsing)
- **Guava** - Bloom filter implementation

### Configuration Requirements
- Nacos server must be running (`127.0.0.1:8848` by default)
- Redis server must be running (`127.0.0.1:6379` by default)
- Public key must be configured for JWT validation
- Whitelist paths should be minimal (security best practice)

### Testing
- Only one test class: `GatewayApplicationTests`
- Use Maven test goals for running tests
- Integration tests can be added in future

### Security Considerations
- Token validation is critical - Bloom filter should not bypass security
- Public key rotation requires service restart (or use Nacos config)
- Whitelist paths should be reviewed regularly
- CORS allows all origins in dev - restrict in production

### Health Checks
- Spring Boot Actuator enabled
- Health endpoint: `GET /actuator/health`
- Shows details when `management.endpoint.health.show-details=always`
