# Tech Stack

## Core

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.7 |
| Build | Maven 3.8+ (Maven Wrapper included: `mvnw`) |
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

- Server runs on **port 8081**
- `spring.jpa.hibernate.ddl-auto=update` — schema auto-managed
- `spring.thymeleaf.cache=true` — disable for local dev if needed
- `server.compression.enabled=false` — gzip handled externally (nginx)
- Twilio credentials default to placeholders; use env vars in production
- Actuator exposes: `health, info, metrics, caches, env`
- Swagger UI: `/swagger-ui.html` | OpenAPI spec: `/v3/api-docs`

## Common Commands

```bash
# Run locally (embedded Tomcat)
./mvnw spring-boot:run

# Compile only
./mvnw compile

# Run tests
./mvnw test

# Package as WAR (skip tests)
./mvnw package -DskipTests

# Full build with tests
./mvnw clean install
```

## CI/CD

GitHub Actions workflow (`.github/workflows/deploy.yml`) triggers on push to `main`:
build WAR → SCP to EC2 → stop old process → run via `nohup java -jar app.war`.

## Testing

- Test DB: H2 in-memory
- Test classes in `src/test/java/com/medicalstore/controller/api/`
- Key test files: `MedicineApiControllerTest`, `ApiEndpointsIntegrationTest`, `ActuatorSwaggerTest`
- Uses `spring-security-test` for RBAC assertions
