# Tech Stack

## Core

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.7 |
| Build | Maven 3.8+ (Maven Wrapper: `./mvnw`) |
| Packaging | WAR (embedded Tomcat or external) |
| UI | Spring MVC + Thymeleaf (server-rendered) |
| Security | Spring Security 6 (role hierarchy, CSRF, Remember-Me) |
| Persistence | Spring Data JPA / Hibernate 6 + MySQL 8.x (prod), H2 (test) |
| Connection Pool | HikariCP (min-idle=5, max-pool=20) |
| Caching | Spring Cache + Caffeine (`maximumSize=1000, expireAfterWrite=60s`) |
| Code Generation | Lombok |

## Libraries

| Purpose | Library |
|---|---|
| PDF generation | iText 8 + html2pdf 5 + Apache Velocity templates |
| Excel export | Apache POI 5.2 |
| Rate limiting | Bucket4j 8.1 |
| WhatsApp notifications | Twilio SDK 10.0 |
| API docs | SpringDoc OpenAPI 2.8 + Swagger UI |
| Monitoring | Spring Boot Actuator |
| JSON | Jackson 2.15 |
| MCP testing server | Node.js + `@modelcontextprotocol/sdk` |

## Key Configuration (`application.properties`)

- Server port: **8081**
- `spring.jpa.hibernate.ddl-auto=update` — schema auto-managed by Hibernate
- `spring.thymeleaf.cache=true` — set to `false` for local dev hot-reload
- `server.compression.enabled=false` — gzip handled externally by nginx
- Twilio credentials default to placeholders; use env vars in production
- Actuator endpoints: `health, info, metrics, caches, env`
- Swagger UI: `/swagger-ui.html` | OpenAPI spec: `/v3/api-docs`
- Remember-me key: `remember.me.key` env var (default: `medicalStoreRememberMeKey2024`)

## Cache Names (Caffeine)

All caches are defined in `CacheConfig.java`. Use the exact names below — never invent new cache name strings inline.

| Cache name | TTL | Used by |
|---|---|---|
| `dashboard_kpis` | 30s | `DashboardService` — KPI counts |
| `dashboard_charts` | 30s | `DashboardService` — chart data |
| `branch_comparison` | 30s | `AnalyticsService` — branch comparison |
| `medicines_search` | 60s | `MedicineService.searchMedicinesForPos()` |
| `subscription_plan` | 60s | `SubscriptionService` |
| `analytics_profit` | 60s | `AnalyticsService` — profit per medicine |
| `analytics_deadstock` | 60s | `AnalyticsService` — dead stock |
| `analytics_fastmoving` | 60s | `AnalyticsService` — fast moving items |
| `analytics_gst` | 60s | `AnalyticsService` — GST analytics |
| `role_permissions` | 60s | `PermissionService` |
| `plan_features` | 60s | `SubscriptionService` — plan feature flags |

To add a new cache: define it in `CacheConfig.java` (use the `build(name, ttlSeconds)` helper), then document it here.

## Common Commands

```bash
# Run locally (embedded Tomcat, port 8081)
./mvnw spring-boot:run

# Compile only
./mvnw compile

# Package as WAR (skip tests)
./mvnw package -DskipTests

# Full build with tests
./mvnw clean install
```

## CI/CD

GitHub Actions (`.github/workflows/deploy.yml`) triggers on push to `main`:
build WAR → SCP to EC2 → stop old process → run via `nohup java -jar app.war`.

## Testing

**IMPORTANT: Skip all test development for this project.**

- DO NOT create new test files or test classes
- DO NOT write unit tests, integration tests, or any automated tests
- DO NOT run tests unless explicitly requested by the user
- Focus on implementation and functionality only

Existing test infrastructure (reference only):
- Test DB: H2 in-memory
- Test classes: `src/test/java/com/medicalstore/controller/api/`
- Key files: `MedicineApiControllerTest`, `ApiEndpointsIntegrationTest`, `ActuatorSwaggerTest`
- Uses `spring-security-test` for RBAC assertions
