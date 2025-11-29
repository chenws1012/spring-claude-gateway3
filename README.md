# Hive Gateway

[![Maven Central](https://img.shields.io/maven-central/v/com.woody/hive-gateway.svg)](https://mvnrepository.com/artifact/com.woody/hive-gateway)

Microservice gateway based on **Spring Cloud Gateway**, providing JWT token authentication, service discovery, rate limiting, and more.

## 🚀 Features

- **JWT Token Authentication**: `CheckTokenFilter` (global filter, order: -100) supports token extraction from header/cookie
- **Bloom Filter Caching**: `CircleBloomFilter` circular Bloom filter to avoid repeated JWT parsing
- **Service Discovery**: Integrated Nacos dynamic route discovery
- **Rate Limiting**: Redis token bucket algorithm (15 req/s, burst 30)
- **Path Whitelisting**: Dynamic configuration with Ant path matching
- **CORS Support**: Global cross-origin configuration
- **Performance Optimizations**: Fast token parsing (no signature verification), thread-safe caching

## 🏗️ Architecture Overview

```
Client Request → CheckTokenFilter (Auth + Bloom Cache) → Rate Limiter → Route Discovery → Target Service
```

![Architecture Diagram](doc/gateway.jpg)

## 📦 Quick Start

### Prerequisites
- Java 17
- Nacos (`127.0.0.1:8848`)
- Redis (`127.0.0.1:6379`)
- Configure `publicKeyPem` (ECC public key in PEM format, Nacos/env)

### Build & Run
```bash
# Build
./mvnw clean package

# Run (dev profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Test
./mvnw test
```

### Docker
```bash
# Build image
docker build -t hive-gateway:latest .

# Run
docker run -p 9000:9000 -e SPRING_PROFILES_ACTIVE=dev hive-gateway:latest
```

## ⚙️ Configuration

### Key application.yaml Settings
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
    gateway:
      routes:
        # Static route example
        - id: test_route
          uri: https://httpbin.org
          predicates:
            - Path=/test/**
  redis:
    host: 127.0.0.1
    port: 6379

# JWT Public Key
publicKeyPem: |
  -----BEGIN PUBLIC KEY-----
  ...
  -----END PUBLIC KEY-----

# Whitelist Paths
my-filter:
  config:
    whiteList: /public/**,/health,/actuator/**
```

Full configuration supports dynamic Nacos refresh with `@RefreshScope`.

## 🔧 Core Components

| Component | Path | Description |
|-----------|------|-------------|
| `CheckTokenFilter` | `src/main/java/com/woody/gateway/filter/CheckTokenFilter.java` | Global auth filter handling whitelists, cache, token validation |
| `CircleBloomFilter` | `src/main/java/com/woody/gateway/util/CircleBloomFilter.java` | 5 rotating filters caching `passed/expired/stopped` states |
| `CheckTokenUtil` | `src/main/java/com/woody/gateway/util/CheckTokenUtil.java` | ECC public key JWT validation (jjwt + BouncyCastle) |
| `TokenParse` | `src/main/java/com/woody/gateway/util/TokenParse.java` | Fast payload parsing (no signature) |
| `MyFilterConfiguration` | `src/main/java/com/woody/gateway/config/MyFilterConfiguration.java` | Whitelist configuration |

## 🧪 Testing & Health Checks

- **Unit Tests**: `./mvnw test`
- **Health Check**: `GET /actuator/health`
- **Error Responses**:
  - 401: No token/invalid
  - 403: Token expired

## 🚀 Deployment

### CI/CD (GitLab CI)
- Auto deploy: test/dev/sit/stage
- Manual deploy: release/prod (K8s)

## 📚 Dependencies

- Spring Boot 3.1.5
- Spring Cloud 2022.0.5 / Alibaba 2022.0.0.0
- Nacos Discovery/Config
- Redis Reactive
- jjwt 0.12.3 + BouncyCastle 1.60
- Guava 32.1.1-jre

## 🤝 Contributing

1. Fork the project
2. Create a feature branch
3. Submit PR to `main` branch

## 📄 License

MIT