# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Spring Cloud Gateway** microservice gateway with:
- JWT authentication via `CheckTokenFilter` (global filter, order -100)
- Nacos service discovery and config
- Redis rate limiting
- Path whitelisting
- CORS support
- CircleBloomFilter caching for JWT validation

## Common Development Commands

### Build & Run
```bash
./mvnw clean package

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

./mvnw test

./mvnw test -Dtest=GatewayApplicationTests
```

### Docker
```bash
docker build -t hive-gateway:latest .

docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev hive-gateway:latest
```

**Tools**: Maven wrapper (`./mvnw`), Spring Boot 3.1.5, Java 17.

## High-Level Architecture

### Core Flow
```
Request → CheckTokenFilter (whitelist/Bloom cache/JWT verify) → Rate Limit → Nacos Route Discovery → Upstream
```

Key packages:
- `com.shun.gateway.filter`: Global auth filter
- `com.shun.gateway.util`: BloomFilter, token utils
- `com.shun.gateway.config`: Whitelists, routes

### Config Requirements
- Nacos: `127.0.0.1:8848`
- Redis: `127.0.0.1:6379`
- `publicKeyPem`: ECC PEM public key (Nacos/env)
- `my-filter.config.whiteList`: Ant patterns (e.g., `/public/**,/health`)

### Token Flow
1. Extract token (header/cookie)
2. Whitelist check → pass
3. Bloom cache: `stopped/expired/passed` → decide
4. Full verify (jjwt/BouncyCastle) → cache result, add headers (`userId`, etc.)

Rate limit: 15rps/burst 30 (Redis).

Health: `/actuator/health`

## Deployment
GitLab CI: build → deploy (auto test/dev/stage, manual release/prod to K8s).