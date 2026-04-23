# Project Structure

## Root Layout

```
medicalstore/
├── src/main/java/com/medicalstore/   # Application source
├── src/main/resources/               # Config, templates, static assets
├── src/test/java/com/medicalstore/   # Tests (do not modify — see tech.md)
├── mcp-server/                       # Node.js MCP testing server
├── docs/                             # Developer documentation
├── .github/workflows/deploy.yml      # CI/CD (GitHub Actions → EC2)
├── .kiro/                            # Kiro specs and steering
├── pom.xml                           # Maven build
└── src/main/resources/application.properties  # All app config
```

## Java Package Structure (`com.medicalstore`)

### `common/`
| File | Purpose |
|---|---|
| `RoutePaths.java` | Centralized URL path constants — **always use these, never hardcode URLs** |
| `SecurityUtils.java` | Auth helpers: `getCurrentUser()`, `isAdmin()`, `isOwner()`, `isShopkeeper()`, `getCurrentBranchId()`, `getCurrentOwnerId()` |
| `TenantContext.java` | Thread-local branch/owner context — set by `TenantFilter`, cleared after request |

### `config/`
| File | Purpose |
|---|---|
| `SecurityConfig.java` | Spring Security filter chain, URL rules, role hierarchy, login/logout, remember-me |
| `FeatureFlags.java` | Runtime role-based feature toggles — check before adding role-conditional logic |
| `DataInitializer.java` | Seed data on first run |
| `RateLimitFilter.java` | Bucket4j per-IP rate limiting |
| `TenantFilter.java` | Resolves branch from session → sets `TenantContext` on every request |
| `CacheConfig.java` | Caffeine cache bean definitions |
| `WebMvcConfig.java` | MVC interceptors and static resource config |
| `OpenApiConfig.java` | Swagger/OpenAPI grouping and metadata |
| `VelocityConfig.java` | Velocity template engine for PDF reports |
| `TwilioConfig.java` | Twilio client bean |

### `controller/`
Web controllers return Thymeleaf views. API controllers return JSON. Both share the same service layer.

| Controller | Route |
|---|---|
| `DashboardController` | `/dashboard` |
| `MedicineController` | `/medicines/**` |
| `SaleController` | `/sales/**` |
| `PurchaseController` | `/purchases/**` |
| `CustomerController` | `/customers/**` |
| `ReturnController` | `/returns/**` |
| `SupplierController` | `/suppliers/**` |
| `SupplierCreditController` | `/suppliers/credit/**` |
| `ReportController` | `/reports/**` |
| `AnalyticsController` | `/analytics/**` |
| `OwnerController` | `/owner/**` |
| `AdminController` | `/admin/**` |
| `SubscriptionController` | `/subscription/**` |
| `ProfileController` | `/profile/**` |
| `LoginController` | `/login` |
| `GlobalExceptionHandler` | `@ControllerAdvice` — HTML error pages |

**`controller/api/`** — REST JSON controllers (prefix: `/api/v1/`)

| Controller | Route |
|---|---|
| `MedicineApiController` | `/api/v1/medicines/**` |
| `SaleApiController` | `/api/v1/sales/**` |
| `CustomerApiController` | `/api/v1/customers/**` |
| `SupplierApiController` | `/api/v1/suppliers/**` |
| `AnalyticsApiController` | `/api/v1/analytics/**` |
| `DashboardApiController` | `/api/v1/dashboard/**` |
| `AppInfoController` | `/api/v1/info` |
| `ApiExceptionHandler` | `@RestControllerAdvice` — JSON error responses |

### `model/` — JPA Entities (16)

All entities use soft delete via `@SQLDelete` + `@SQLRestriction("is_deleted = false")`. Hard deletes never happen — Hibernate rewrites DELETE as `UPDATE ... SET is_deleted = true`.

| Entity | Table | Notes |
|---|---|---|
| `User` | `users` | Roles stored in `user_roles` join table. `branch` only set for SHOPKEEPER |
| `Branch` | `branches` | Owned by a `User` with OWNER role |
| `Medicine` | `medicines` | Has `@Version` for optimistic locking on stock updates |
| `Sale` | `sales` | Header record; line items in `SaleItem` |
| `SaleItem` | `sale_items` | Line items for a `Sale` |
| `PurchaseOrder` | `purchase_orders` | Header; items in `PurchaseOrderItem` |
| `PurchaseOrderItem` | `purchase_order_items` | Line items for a `PurchaseOrder` |
| `Customer` | `customers` | Has loyalty points and credit balance |
| `Supplier` | `suppliers` | Branch-scoped |
| `SupplierCredit` | `supplier_credits` | Credit/debit ledger per supplier |
| `Return` | `returns` | Sale return records |
| `SubscriptionPlan` | `subscription_plans` | One per owner; plan type: FREE/PRO/ENTERPRISE |
| `SubscriptionFeature` | `subscription_features` | Feature flags per plan tier |
| `Role` | `roles` | Role definitions |
| `Permission` | `permissions` | Fine-grained permission codes |
| `AuditLog` | `audit_logs` | Immutable audit trail |

**Entity patterns to follow:**
```java
@Entity
@Table(name = "examples", indexes = {
    @Index(name = "idx_example_branch", columnList = "branch_id")
})
@SQLDelete(sql = "UPDATE examples SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Data @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Example {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version  // add to entities with concurrent write risk (stock, balances)
    private Long version = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @JsonIgnore
    private Branch branch;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() { /* set createdDate etc. */ }
}
```

### `repository/` — Spring Data JPA (14)

`AuditLogRepository`, `BranchRepository`, `CustomerRepository`, `MedicineRepository`, `PermissionRepository`, `PurchaseOrderRepository`, `ReturnRepository`, `SaleItemRepository`, `SaleRepository`, `SubscriptionFeatureRepository`, `SubscriptionPlanRepository`, `SupplierCreditRepository`, `SupplierRepository`, `UserRepository`

All repositories must provide tenant-scoped query methods:
```java
List<Example> findByBranchId(Long branchId);
List<Example> findByOwnerId(Long ownerId);   // via branch.owner.id join
long countByBranchId(Long branchId);
```

### `service/` — Business Logic (21)
`AnalyticsService`, `AuditLogService`, `BranchService`, `BusinessLimitService`, `CustomerService`, `CustomUserDetailsService`, `DashboardService`, `MedicineService`, `PdfService`, `PermissionService`, `PurchaseService`, `ReportService`, `ReturnService`, `RoleAuditService`, `SaleService`, `ScheduledJobService`, `SubscriptionService`, `SupplierCreditService`, `SupplierService`, `UserManagementService`, `WhatsAppService`

### `dto/`
`BranchComparisonDTO`, `DailyReportData`, `ErrorResponse`, `GstReportData`, `MedicineDTO`, `MonthlyReportData`, `SaleRequestDTO`

### `exception/`
| Class | HTTP | When to throw |
|---|---|---|
| `BusinessException(code, message)` | 400 | Domain rule violations |
| `ResourceNotFoundException(entity, id)` | 404 | Entity not found |
| `StockConflictException(medicineName)` | 409 | Concurrent stock race condition |
| `TenantAccessException(resource, tenantId)` | 403 | Cross-tenant access attempt |

### `security/`
`UserDetails` implementation, `MediStorePermissionEvaluator` (custom `@PreAuthorize` permission checks)

## Resources Layout

```
resources/
  application.properties        # All app config (port 8081)
  templates/
    layout.html                 # Base layout — ALL pages extend this
    fragments.html              # Reusable UI fragments
    fragments-role-guards.html  # Role-conditional UI fragments
    index.html                  # Shopkeeper dashboard
    login.html
    admin/    analytics/   customers/   medicines/   owner/
    pdf/      purchase/    reports/     returns/     sales/
    suppliers/ subscription/ profile/  error/
  static/
    css/
      layout.css                # Entry point — imports all layout/ files
      layout/                   # base.css, components.css, sidebar.css, header.css,
                                # responsive.css, pos.css, medicines.css,
                                # enhancements.css, fixes.css
    js/
      layout.js                 # Global JS (sidebar, alerts, form helpers)
  velocity/                     # Velocity templates for PDF report generation
  db/                           # DB migration/seed scripts
```

## Architectural Patterns

- **Layered**: Controller → Service → Repository → Entity
- **Multi-tenancy**: `TenantFilter` sets `TenantContext` (thread-local) on every request; services read `TenantContext.getTenantId()` / `getOwnerId()` to scope all queries
- **Dual controller**: Web controllers → Thymeleaf views; `api/` controllers → JSON. Same service layer
- **Feature flags**: Check `FeatureFlags` before adding role-conditional UI or service logic
- **URL constants**: Always use `RoutePaths` constants — never hardcode URL strings
- **Soft deletes**: Entities support soft deletion; scheduled job purges after retention period
- **Caching**: `@Cacheable` / `@CacheEvict` in services — always evict on writes
- **PDF generation**: iText direct API (invoices) OR Velocity → HTML → html2pdf (reports)
- **Security layers**: URL rules in `SecurityConfig` + `@PreAuthorize` on methods + `sec:authorize` in templates
