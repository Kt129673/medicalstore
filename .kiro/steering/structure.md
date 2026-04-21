# Project Structure

## Root Layout

```
medicalstore/
├── src/main/java/com/medicalstore/   # Application source
├── src/main/resources/               # Config, templates, static assets
├── src/test/java/com/medicalstore/   # Tests
├── mcp-server/                       # Node.js MCP testing server
├── docs/                             # Developer documentation (30 files)
├── .github/workflows/                # CI/CD (GitHub Actions)
├── .kiro/                            # Kiro specs and steering
├── pom.xml                           # Maven build
└── application.properties            # App config (in resources/)
```

## Java Package Structure (`com.medicalstore`)

```
common/
  RoutePaths.java          # Centralized URL path constants — use these, don't hardcode URLs
  SecurityUtils.java       # Auth helper methods (current user, role checks)
  TenantContext.java       # Thread-local branch/tenant context for multi-tenancy

config/
  SecurityConfig.java      # Spring Security — URL access rules, filter chain
  FeatureFlags.java        # Runtime role-based feature toggles
  DataInitializer.java     # Seed data on first run
  RateLimitFilter.java     # Bucket4j per-IP rate limiting
  TenantFilter.java        # Resolves branch from session → sets TenantContext
  CacheConfig.java         # Caffeine cache bean definitions
  WebMvcConfig.java        # MVC interceptors and static resource config
  OpenApiConfig.java       # Swagger/OpenAPI grouping and metadata
  VelocityConfig.java      # Velocity template engine for PDF reports
  TwilioConfig.java        # Twilio client bean

controller/
  api/                     # REST JSON controllers (prefix: /api/v1/)
    MedicineApiController
    SaleApiController
    CustomerApiController
    SupplierApiController
    AnalyticsApiController
    DashboardApiController
    AppInfoController
    ApiExceptionHandler    # @RestControllerAdvice for JSON error responses
  DashboardController      # / (home)
  MedicineController       # /medicines/**
  SaleController           # /sales/**
  PurchaseController       # /purchases/**
  CustomerController       # /customers/**
  ReturnController         # /returns/**
  SupplierController       # /suppliers/**
  SupplierCreditController
  ReportController         # /reports/**
  AnalyticsController      # /analytics/**
  OwnerController          # /owner/**
  AdminController          # /admin/**
  SubscriptionController   # /subscription/**
  ProfileController        # /profile/**
  LoginController          # /login
  GlobalExceptionHandler   # @ControllerAdvice for HTML error pages

dto/                       # Data Transfer Objects (7 DTOs)
exception/                 # BusinessException, ResourceNotFoundException
model/                     # JPA entities (16 models)
repository/                # Spring Data JPA repositories (14)
security/                  # UserDetails implementation, auth components
service/                   # Business logic (21 services)
```

## Resources Layout

```
resources/
  application.properties   # All app config
  templates/               # Thymeleaf HTML templates
    layout.html            # Base layout (all pages extend this)
    fragments.html         # Reusable UI fragments
    fragments-role-guards.html  # Role-conditional UI fragments
    index.html             # Dashboard
    login.html
    admin/   analytics/   customers/   medicines/   owner/
    pdf/     purchase/    reports/     returns/     sales/
    suppliers/  subscription/  profile/  error/
  static/
    css/
      layout.css           # Main stylesheet entry point
      layout/              # Modular CSS (components.css, sidebar.css, header.css, responsive.css)
    js/
      layout.js            # Main JS file
  velocity/                # Velocity templates for PDF report generation
  db/                      # DB migration/seed scripts
```

## Architectural Patterns

- **Layered architecture**: Controller → Service → Repository → Entity
- **Multi-tenancy**: `TenantFilter` sets `TenantContext` (thread-local) on every request; services use it to scope queries to the current branch
- **Dual controller pattern**: Web controllers return Thymeleaf views; API controllers under `api/` return JSON. Both share the same service layer
- **Feature flags**: `FeatureFlags.java` gates features at runtime by role — check here before adding role-conditional UI
- **URL constants**: Always use `RoutePaths` constants instead of hardcoding URL strings
- **Soft deletes**: Entities support soft deletion; a scheduled job purges records after the retention period
- **Caching**: `@Cacheable` / `@CacheEvict` used in services — evict relevant caches when mutating data
- **PDF generation**: Two paths — iText direct API (invoices) and Velocity templates rendered to HTML then converted via html2pdf (reports)
- **Security layers**: URL-level rules in `SecurityConfig` + method/template-level checks via `FeatureFlags` and Thymeleaf `sec:authorize`
