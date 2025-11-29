# Hive Gateway

[![Maven Central](https://img.shields.io/maven-central/v/com.shun/hive-gateway.svg)](https://mvnrepository.com/artifact/com.shun/hive-gateway)

基于 **Spring Cloud Gateway** 的微服务网关，提供 JWT 令牌鉴权、服务发现、限流等功能。

## 🚀 特性

- **JWT 令牌鉴权**：使用 `CheckTokenFilter`（全局过滤器，order: -100）支持 header/cookie 令牌提取
- **Bloom Filter 缓存**：`CircleBloomFilter` 循环布隆过滤器，避免重复 JWT 解析
- **服务发现**：集成 Nacos 动态路由发现
- **速率限制**：Redis 令牌桶算法（15 req/s，burst 30）
- **路径白名单**：动态配置，支持 Ant 路径匹配
- **CORS 支持**：全域跨域配置
- **性能优化**：快速令牌解析（无签名验证）、线程安全缓存

## 🏗️ 架构概述

```
客户端请求 → CheckTokenFilter (鉴权 + Bloom Cache) → 限流器 → 路由发现 → 目标服务
```

![架构图](doc/gateway.jpg)

## 📦 快速开始

### 前置要求
- Java 17
- Nacos (`127.0.0.1:8848`)
- Redis (`127.0.0.1:6379`)
- 配置 `publicKeyPem`（ECC 公钥 PEM 格式，Nacos/env）

### 构建 & 运行
```bash
# 构建
./mvnw clean package

# 运行（dev 环境）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 测试
./mvnw test
```

### Docker
```bash
# 构建镜像
docker build -t hive-gateway:latest .

# 运行
docker run -p 9000:9000 -e SPRING_PROFILES_ACTIVE=dev hive-gateway:latest
```

## ⚙️ 配置

### application.yaml 关键配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
    gateway:
      routes:
        # 静态路由示例
        - id: test_route
          uri: https://httpbin.org
          predicates:
            - Path=/test/**
  redis:
    host: 127.0.0.1
    port: 6379

# JWT 公钥
publicKeyPem: |
  -----BEGIN PUBLIC KEY-----
  ...
  -----END PUBLIC KEY-----

# 白名单路径
my-filter:
  config:
    whiteList: /public/**,/health,/actuator/**
```

完整配置支持 Nacos 动态刷新 `@RefreshScope`。

## 🔧 核心组件

| 组件 | 路径 | 描述 |
|------|------|------|
| `CheckTokenFilter` | `src/main/java/com/woody/gateway/filter/CheckTokenFilter.java` | 全局鉴权过滤器，处理白名单、缓存、令牌验证 |
| `CircleBloomFilter` | `src/main/java/com/woody/gateway/util/CircleBloomFilter.java` | 5 个旋转过滤器，缓存 `passed/expired/stopped` 状态 |
| `CheckTokenUtil` | `src/main/java/com/woody/gateway/util/CheckTokenUtil.java` | ECC 公钥 JWT 验证（jjwt + BouncyCastle） |
| `TokenParse` | `src/main/java/com/woody/gateway/util/TokenParse.java` | 快速 payload 解析（无签名） |
| `MyFilterConfiguration` | `src/main/java/com/woody/gateway/config/MyFilterConfiguration.java` | 白名单配置 |

## 🧪 测试 & 健康检查

- **单元测试**：`./mvnw test`
- **健康检查**：`GET /actuator/health`
- **错误响应**：
  - 401: 无令牌/无效
  - 403: 令牌过期

## 🚀 部署

### CI/CD (GitLab CI)
- 自动部署：test/dev/sit/stage
- 手动部署：release/prod (K8s)

## 📚 依赖

- Spring Boot 3.1.5
- Spring Cloud 2022.0.5 / Alibaba 2022.0.0.0
- Nacos Discovery/Config
- Redis Reactive
- jjwt 0.12.3 + BouncyCastle 1.60
- Guava 32.1.1-jre

## 🤝 贡献

1. Fork 项目
2. 创建 feature 分支
3. 提交 PR 到 `main` 分支

## 📄 许可证

MIT