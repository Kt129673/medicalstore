# Backend Patterns — Service, Controller, Security, Caching

Authoritative guide for all Java backend work. Follow these patterns exactly to stay consistent with the existing codebase.

---

## Controller Patterns

### Web Controller (Thymeleaf)

```java
@Controller
@RequestMapping(RoutePaths.MEDICINES)
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")  // class-level default
public class ExampleController {

    private final ExampleService exampleService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        model.addAttribute("itemPage", exampleService.filter(search, pageable));
        model.addAttribute("currentSearch", search);
        return "example/list";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('SHOPKEEPER')")  // tighten at method level when needed
    public String save(@ModelAttribute Entity entity, RedirectAttributes ra) {
        try {
            exampleService.save(entity);
            ra.addFlashAttribute("successMessage", "Saved successfully.");
            return RoutePaths.redirectTo(RoutePaths.MEDICINES);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return RoutePaths.redirectTo(RoutePaths.MEDICINES);
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasPermission(null, 'ENTITY_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            exampleService.delete(id);
            ra.addFlashAttribute("successMessage", "Deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Cannot delete: it may be linked to other records.");
        }
        return RoutePaths.redirectTo(RoutePaths.MEDICINES);
    }
}
```

**Rules:**
- Always use `RoutePaths.redirectTo(RoutePaths.CONSTANT)` — never hardcode redirect strings
- Flash attributes MUST be `successMessage` / `errorMessage` — these are the names the layout template renders
- Catch exceptions in POST handlers and redirect with flash messages — never let them bubble to the error page from a form submit
- Use `@PreAuthorize` at class level for the default, tighten at method level for writes

### REST API Controller

```java
@RestController
@RequestMapping("/api/v1/examples")
@RequiredArgsConstructor
@Tag(name = "Examples", description = "Example resource management")
public class ExampleApiController {

    private final ExampleService exampleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "List all examples")
    public ResponseEntity<List<ExampleDTO>> list() {
        return ResponseEntity.ok(exampleService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Create example")
    public ResponseEntity<ExampleDTO> create(@Valid @RequestBody ExampleDTO dto) {
        ExampleDTO saved = exampleService.save(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete example")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (exampleService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        exampleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Rules:**
- All API routes under `/api/v1/`
- CSRF is disabled for `/api/**` — no CSRF token needed in API requests
- Always add `@Operation` and `@Tag` for Swagger docs
- Return `ResponseEntity` with correct HTTP status codes
- Validation errors, business exceptions, and 500s are handled automatically by `ApiExceptionHandler`

---

## Service Patterns

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // default: read-only; override on writes
public class ExampleService {

    private final ExampleRepository exampleRepository;

    // ── Reads — always scope to tenant context ─────────────────────────────

    public List<Example> getAll() {
        Long tenantId = TenantContext.getTenantId();
        Long ownerId  = TenantContext.getOwnerId();

        if (tenantId != null) return exampleRepository.findByBranchId(tenantId);
        if (ownerId  != null) return exampleRepository.findByOwnerId(ownerId);
        return exampleRepository.findAll();   // ADMIN global view
    }

    public Optional<Example> findById(Long id) {
        Optional<Example> result = exampleRepository.findById(id);
        result.ifPresent(e -> {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null && !tenantId.equals(e.getBranch().getId())) {
                throw new TenantAccessException("Example", tenantId);
            }
        });
        return result;
    }

    // ── Writes ─────────────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "medicines_search", allEntries = true)  // evict if this affects medicine data
    public Example save(Example entity) {
        // Validate before persisting
        if (entity.getName() == null || entity.getName().isBlank())
            throw new IllegalArgumentException("Name is required");
        return exampleRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        exampleRepository.deleteById(id);
    }
}
```

**Rules:**
- Class-level `@Transactional(readOnly = true)` — override with `@Transactional` on write methods
- Every read method MUST check `TenantContext` and scope the query accordingly
- Throw `IllegalArgumentException` for input validation failures (maps to 400)
- Throw `ResourceNotFoundException` when an entity is not found (maps to 404)
- Throw `TenantAccessException` for cross-tenant access attempts (maps to 403)
- Throw `StockConflictException` for concurrent stock race conditions (maps to 409)
- Never throw raw `RuntimeException` — use the typed exceptions in `com.medicalstore.exception`

---

## Exception Usage

| Situation | Exception to throw |
|---|---|
| Input validation failure | `throw new IllegalArgumentException("Field X is required")` |
| Entity not found | `throw new ResourceNotFoundException("Medicine", id)` |
| Business rule violation | `throw new BusinessException("RULE_CODE", "Human-readable message")` |
| Cross-tenant access | `throw new TenantAccessException("Medicine", attemptedTenantId)` |
| Concurrent stock conflict | `throw new StockConflictException(medicineName)` |

Both `GlobalExceptionHandler` (HTML) and `ApiExceptionHandler` (JSON) handle all of these automatically — never catch and swallow them in services.

---

## Multi-Tenancy — TenantContext

`TenantFilter` sets `TenantContext` at the start of every request. Services must always read it:

```java
Long tenantId = TenantContext.getTenantId();  // non-null for SHOPKEEPER
Long ownerId  = TenantContext.getOwnerId();   // non-null for OWNER

// Pattern for all service reads:
if (tenantId != null) return repo.findByBranchId(tenantId);
if (ownerId  != null) return repo.findByOwnerId(ownerId);
return repo.findAll();  // ADMIN sees everything
```

**Never** query without checking tenant context — this is the primary data isolation mechanism.

---

## Security — @PreAuthorize

Use `@PreAuthorize` at the controller level. Common patterns:

```java
// Role checks
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")

// Custom permission evaluator (MediStorePermissionEvaluator → PermissionService)
@PreAuthorize("hasPermission(null, 'MEDICINE_DELETE')")
@PreAuthorize("hasPermission(null, 'MEDICINE_BULK_DELETE')")
@PreAuthorize("hasPermission(null, 'REPORT_EXPORT_EXCEL')")
@PreAuthorize("hasPermission(null, 'REPORT_EXPORT_PDF')")
```

Permission codes are stored in the `permissions` table (seeded by `DataInitializer`) and checked via `PermissionService` → cached in `role_permissions` cache. To add a new permission: insert a record in `DataInitializer.seedPermissions()` and add the `@PreAuthorize` annotation.

**SecurityUtils** — inject and use in controllers when you need the current user:

```java
securityUtils.isAdmin()            // true if ADMIN role
securityUtils.isOwner()            // true if OWNER role
securityUtils.isShopkeeper()       // true if SHOPKEEPER role
securityUtils.getCurrentUser()     // full User entity (hits DB — use sparingly)
securityUtils.getCurrentBranchId() // branch ID (SHOPKEEPER only, null otherwise)
securityUtils.getCurrentOwnerId()  // owner ID (OWNER or SHOPKEEPER's branch owner)
```

---

## Caching

```java
// Cache a read result — always include tenantId in key for tenant-scoped data
@Cacheable(value = "medicines_search",
           key = "#query + '-' + T(com.medicalstore.common.TenantContext).getTenantId()")
public List<MedicineDTO> searchMedicinesForPos(String query) { ... }

// Evict on write — allEntries=true is the safe default for tenant-scoped caches
@CacheEvict(value = "medicines_search", allEntries = true)
@Transactional
public Medicine save(Medicine medicine) { ... }

// Evict multiple caches on a single write
@Caching(evict = {
    @CacheEvict(value = "dashboard_kpis", allEntries = true),
    @CacheEvict(value = "dashboard_charts", allEntries = true)
})
@Transactional
public Sale createSale(Sale sale) { ... }
```

**All cache names** (defined in `CacheConfig.java`):

| Cache | TTL | Purpose |
|---|---|---|
| `dashboard_kpis` | 30s | Dashboard KPI counts |
| `dashboard_charts` | 30s | Dashboard chart data |
| `branch_comparison` | 30s | Owner branch comparison |
| `medicines_search` | 60s | POS medicine search |
| `subscription_plan` | 60s | Subscription plan lookups |
| `analytics_profit` | 60s | Profit per medicine |
| `analytics_deadstock` | 60s | Dead stock analysis |
| `analytics_fastmoving` | 60s | Fast moving items |
| `analytics_gst` | 60s | GST analytics |
| `role_permissions` | 60s | Permission code lookups |
| `plan_features` | 60s | Subscription feature flags |

To add a new cache: add `build("cache_name", ttlSeconds)` to `CacheConfig.java` and document it in `tech.md`.

---

## Entity Patterns

All entities follow this structure — copy it exactly:

```java
@Entity
@Table(name = "examples", indexes = {
    @Index(name = "idx_example_branch", columnList = "branch_id"),
    @Index(name = "idx_example_name",   columnList = "name")
})
@SQLDelete(sql = "UPDATE examples SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Data @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Example {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Add @Version to entities with concurrent write risk (stock, balances)
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private String name;

    // Branch association — always LAZY, always @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @JsonIgnore
    private Branch branch;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
    }
}
```

**Key rules:**
- Always add `@SQLDelete` + `@SQLRestriction` — no hard deletes
- Always add `branch_id` index — every tenant-scoped entity needs it
- `@ManyToOne` associations: always `FetchType.LAZY` + `@JsonIgnore` to prevent serialization loops
- Add `@Version` to any entity where concurrent writes could cause lost updates (stock, credit balances)

---

1. **Model** — Add entity in `model/`, annotate with `@Entity`, `@Table`, Lombok `@Data`/`@Builder`
2. **Repository** — Add interface in `repository/` extending `JpaRepository`. Add tenant-scoped query methods
3. **Service** — Add in `service/`, follow tenant-context pattern, use typed exceptions
4. **Controller** — Add in `controller/`, use `RoutePaths`, `@PreAuthorize`, flash messages
5. **API Controller** (if needed) — Add in `controller/api/`, follow REST patterns above
6. **Templates** — Follow ui-productivity.md patterns, extend `layout.html`
7. **RoutePaths** — Add new URL constants to `RoutePaths.java`
8. **SecurityConfig** — Add URL access rules if the new route needs them
9. **FeatureFlags** — Add a flag if the feature should be role-gated at runtime

---

## Lombok Usage

```java
@Data           // getters, setters, equals, hashCode, toString
@Builder        // builder pattern
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor  // constructor for final fields (preferred for DI)
@Slf4j          // injects `log` field
```

Prefer `@RequiredArgsConstructor` on services and controllers — Spring injects via constructor automatically.

---

## PDF Generation

Two paths — choose based on complexity:

| Use case | Approach |
|---|---|
| Invoices (structured, precise layout) | iText 8 direct API via `PdfService` |
| Reports (HTML-based, data-heavy) | Velocity template → HTML → html2pdf via `ReportService` |

Velocity templates live in `src/main/resources/velocity/`. Use `VelocityConfig` to load them.
