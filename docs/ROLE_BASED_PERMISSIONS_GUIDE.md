# 🔐 Role-Based Access Control (RBAC) & Extension Guide

> **For Contributors & Open-Source Maintainers**  
> Complete guide to the role-based permission system

---

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Role Definitions](#role-definitions)
3. [Permission Matrix](#permission-matrix)
4. [Adding New Roles](#adding-new-roles)
5. [Protecting Endpoints](#protecting-endpoints)
6. [Audit Logging](#audit-logging)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## System Overview

This application uses **Spring Security** with a **3-tier role system**.

### Architecture
```
┌─────────────────────────────────────────────────────┐
│         Spring Security FilterChain                 │
├─────────────────────────────────────────────────────┤
│  SEC_ROLE_ADMIN       SEC_ROLE_OWNER  SEC_ROLE_SHOP │
│  (Platform Admin)     (Branch Owner)  (Operator)    │
└─────────────────────────────────────────────────────┘
         │                  │                │
         ▼                  ▼                ▼
   ┌──────────┐       ┌──────────┐    ┌─────────┐
   │  /admin  │       │  /owner  │    │  /      │
   └──────────┘       └──────────┘    └─────────┘
```

---

## Role Definitions

### 1. ADMIN - Platform Administrator
```java
Role.ADMIN ("ROLE_ADMIN")
├─ Permission: Overall platform management
├─ Can create/edit/disable any user
├─ Can manage subscriptions globally
├─ Can see all business data (global scope)
└─ Cannot use operational features directly
   (medicines, sales, etc. — admin portal instead)
```

**Database value:** `"ADMIN"` in `user_roles.role`

### 2. OWNER - Branch Owner/Franchise
```java
Role.OWNER ("ROLE_OWNER")
├─ Permission: Own portfolio management
├─ Can manage their branches
├─ Can hire/manage shopkeepers
├─ Can manage subscription plans
├─ Can view analytics for their branches only
└─ Cannot access other owners' data
```

**Database value:** `"OWNER"` in `user_roles.role`

### 3. SHOPKEEPER - Store Operator
```java
Role.SHOPKEEPER ("ROLE_SHOPKEEPER")
├─ Permission: Daily operations
├─ Can process sales, handle returns
├─ Can manage inventory for their branch
├─ Can view reports scoped to their branch
└─ Cannot access multi-branch features
```

**Database value:** `"SHOPKEEPER"` in `user_roles.role`

---

## Permission Matrix

| Feature | ADMIN | OWNER | SHOPKEEPER |
|---------|:-----:|:-----:|:----------:|
| **Management** | | | |
| Manage Users | ✅ | ❌ | ❌ |
| Manage Branches | ✅ | ❌ | ❌ |
| Manage Subscriptions | ✅ | ✅ | ❌ |
| Manage Shopkeepers | ❌ | ✅ | ❌ |
| **Operations** | | | |
| View Dashboard | ✅ | N/A | ✅ |
| Medicines | ✅ | ❌ | ✅ |
| Sales | ✅ | ❌ | ✅ |
| Returns | ✅ | ❌ | ✅ |
| Customers | ✅ | ❌ | ✅ |
| Suppliers | ✅ | ❌ | ✅ |
| Purchases | ✅ | ❌ | ✅ |
| **Analytics** | | | |
| Reports | ✅ (global) | ✅ (scoped) | ✅ (scoped) |
| Analytics | ✅ (global) | ✅ (scoped) | ✅ (scoped) |
| GST/Tax | ✅ (global) | ✅ (scoped) | ✅ (scoped) |
| **Profile** | | | |
| Change Password | ✅ | ✅ | ✅ |

---

## Adding New Roles

### Step 1: Define the Role Enum

```java
// src/main/java/com/medicalstore/model/Role.java

public enum Role {
    ADMIN("ADMIN"),
    OWNER("OWNER"),
    SHOPKEEPER("SHOPKEEPER"),
    REGIONAL_MANAGER("REGIONAL_MANAGER");  // ← NEW ROLE

    private final String value;
    // ... (existing code)
}
```

### Step 2: Create the Controller

```java
// src/main/java/com/medicalstore/controller/RegionalManagerController.java

@Controller
@RequestMapping("/regional-manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('REGIONAL_MANAGER')")
public class RegionalManagerController {

    private final BranchService branchService;
    private final SecurityUtils securityUtils;
    private final RoleAuditService roleAuditService;

    @GetMapping
    public String dashboard(Model model) {
        Long managerId = securityUtils.getCurrentUserId();
        
        // Load branches managed by this regional manager
        List<Branch> managedBranches = branchService.getBranchesByRegionalManager(managerId);
        
        model.addAttribute("title", "Regional Manager Dashboard");
        model.addAttribute("page", "regional-manager");
        model.addAttribute("branches", managedBranches);
        
        // Audit log
        roleAuditService.logAction("DASHBOARD_ACCESS", "Regional Manager accessed dashboard");
        
        return "regional-manager/dashboard";
    }
}
```

### Step 3: Update SecurityConfig

```java
// src/main/java/com/medicalstore/config/SecurityConfig.java

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authz -> authz
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/owner/**").hasRole("OWNER")
            .requestMatchers("/regional-manager/**").hasRole("REGIONAL_MANAGER")  // ← ADD THIS
            .requestMatchers("/medicines/**", "/sales/**")
                .hasAnyRole("ADMIN", "SHOPKEEPER")
            // ... rest of configuration
    );
    // ...
}
```

### Step 4: Create UI Template

```html
<!-- src/main/resources/templates/regional-manager/dashboard.html -->

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
    th:replace="~{layout :: layout(title='Regional Manager', page='regional-manager', content=~{::main})}">
<main>
    <h2>Regional Manager Dashboard</h2>
    <p>Manage your assigned branches</p>
    <!-- Your template here -->
</main>
</html>
```

### Step 5: Update Layout Sidebar

```html
<!-- src/main/resources/templates/layout.html -->

<!-- ──REGIONAL_MANAGER LINKS── -->
<div sec:authorize="hasRole('REGIONAL_MANAGER')">
    <details class="nav-group">
        <summary>Region Management</summary>
        <a class="nav-link" href="/regional-manager">
            <i class="bi bi-map-fill nav-icon"></i><span>Dashboard</span>
        </a>
        <a class="nav-link" href="/regional-manager/branches">
            <i class="bi bi-building nav-icon"></i><span>Branches</span>
        </a>
    </details>
</div>
```

### Step 6: Add Audit Logging

```java
// In your new controller methods, utilize RoleAuditService

@PostMapping("/update-branch/{id}")
public String updateBranch(@PathVariable Long id, ..., RedirectAttributes ra) {
    // Update logic
    
    roleAuditService.logAction(
        "BRANCH_UPDATED",
        String.format("Regional Manager updated branch %d", id)
    );
    
    return "redirect:/regional-manager/branches";
}
```

### Step 7: Add Login Redirect

```java
// src/main/java/com/medicalstore/config/SecurityConfig.java

@Bean
public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return (request, response, authentication) -> {
        userDetailsService.updateLastLogin(auth.getName());

        boolean isRegionalManager = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_REGIONAL_MANAGER"::equals);

        if (isRegionalManager) {
            response.sendRedirect("/regional-manager");  // ← ADD THIS
        } else if (isAdmin) {
            response.sendRedirect("/admin");
        }
        // ... rest of logic
    };
}
```

### Step 8: Database Update (Migration)

```sql
-- Add new role to existing user (if needed)
UPDATE user_roles 
SET role = 'REGIONAL_MANAGER' 
WHERE user_id = ? AND role = 'OWNER';
```

---

## Protecting Endpoints

### Controller-Level Protection

```java
@PreAuthorize("hasRole('ADMIN')")
public class AdminController { ... }

@PreAuthorize("hasRole('OWNER')")
public class OwnerController { ... }

@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
public class MedicineController { ... }
```

### Method-Level Protection

```java
public class SubscriptionController {
    
    @PostMapping("/upgrade/{planType}")
    @PreAuthorize("hasRole('OWNER')")
    public String upgradePlan(@PathVariable String planType) {
        // Only OWNER can upgrade their plan
    }
    
    @PostMapping("/cancel")
    @PreAuthorize("hasRole('OWNER')")
    public String cancelPlan() {
        // Only OWNER can cancel
    }
}
```

### Post-Execution Access Check

```java
// Verify data ownership AFTER loading the data

@GetMapping("/branch/{id}")
public String viewBranch(@PathVariable Long id) {
    Branch branch = branchService.getBranchById(id)
        .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
    
    // Verify current user owns this branch
    Long currentOwnerId = securityUtils.getCurrentOwnerId();
    if (!branch.getOwner().getId().equals(currentOwnerId)) {
        roleAuditService.logEscalationAttempt(
            "/branch/" + id,
            "OWNER",
            "Attempt to access another owner's branch"
        );
        throw new AccessDeniedException("Access denied");
    }
    
    return "branch/detail";
}
```

### Template-Level Protection

```html
<!-- Only show delete button to ADMIN -->
<form th:if="${#authorization.expression('hasRole(\"ADMIN\")')}"
      th:action="@{/users/{id}/delete(id=${user.id})}" method="post">
    <button type="submit" class="btn btn-danger">Delete</button>
</form>

<!-- Using Thymeleaf Spring Security extras -->
<button sec:authorize="hasRole('ADMIN')" class="btn btn-danger">
    Delete User
</button>
```

---

## Audit Logging

### Using RoleAuditService

```java
@Autowired
private RoleAuditService roleAuditService;

// Log user creation
roleAuditService.logUserCreated("john_owner123", "OWNER");

// Log admin actions
roleAuditService.logPasswordReset("shopkeeper_5");

// Log sensitive operations
roleAuditService.logAction("SUBSCRIPTION_CHANGED", 
    "Owner 1: Plan changed from FREE to PRO");

// Log escalation attempts
roleAuditService.logEscalationAttempt(
    "/admin/users",
    "OWNER",
    "ADMIN"
);
```

### Audit Log Output (Logs)

```
2026-03-01 14:23:45.123 INFO  AUDIT_LOG | User: admin | Roles: ADMIN | Action: USER_CREATED | Description: Created user 'john_owner123' with role 'OWNER'

2026-03-01 14:25:12.456 INFO  AUDIT_LOG | User: shopkeeper_1 | Roles: SHOPKEEPER | Action: SALE_COMPLETED | Description: Processed sale #1005...

2026-03-01 14:26:30.789 WARN  AUDIT_DENIED | User: owner_1 | Roles: OWNER | Attempted: /admin/users | Reason: Forbidden - insufficient permissions
```

---

## Best Practices

### ✅ DO

1. **Always use @PreAuthorize on controller classes**
   ```java
   @PreAuthorize("hasRole('ADMIN')")
   public class AdminController { ... }
   ```

2. **Log sensitive operations**
   ```java
   roleAuditService.logAction("USER_DISABLED", "Disabled user: " + username);
   ```

3. **Verify data ownership after retrieval**
   ```java
   Branch branch = branchService.getBranchById(id).orElse(null);
   if (!isOwner(branch)) throw new AccessDeniedException();
   ```

4. **Use SecurityUtils for role checks**
   ```java
   if (securityUtils.isAdmin()) { ... }
   if (securityUtils.getCurrentBranchId() != null) { ... }
   ```

5. **Hide UI elements based on roles**
   ```html
   <button sec:authorize="hasRole('ADMIN')" class="btn btn-danger">Delete</button>
   ```

### ❌ DON'T

1. **Don't rely on frontend-only role checks**
   - Always validate on the backend

2. **Don't skip audit logging for sensitive operations**
   - Always log critical actions

3. **Don't assume users can't access endpoints**
   - Users can craft requests directly
   - Always check @PreAuthorize

4. **Don't hardcode user IDs**
   ```java
   // WRONG
   if (userId == 1) { ... }
   
   // CORRECT
   if (securityUtils.getCurrentUserId().equals(userId)) { ... }
   ```

5. **Don't use `permitAll()` for sensitive endpoints**

---

## Troubleshooting

### Issue: User gets 403 Forbidden unexpectedly

**Diagnosis:**
```bash
grep "AUDIT_DENIED" logs/application.log | grep $USERNAME
```

**Solutions:**
1. Check user has correct role: `SELECT roles FROM user_roles WHERE user_id = ?`
2. Verify SecurityConfig route mapping includes the endpoint
3. Check @PreAuthorize annotation on controller

### Issue: Custom role not working

**Checklist:**
- [ ] Added role to Role enum?
- [ ] Updated SecurityConfig.authorizeHttpRequests()?
- [ ] Created role-specific controller with @PreAuthorize?
- [ ] Updated sidebar layout.html with sec:authorize?
- [ ] Restarted the application?

### Issue: Data leakage between roles

**Check:**
1. Verify queries filter by current user/branch
   ```java
   // WRONG
   Sales.findAll()
   
   // CORRECT
   Sales.findByBranchId(securityUtils.getCurrentBranchId())
   ```

2. Verify @PostAuthorize guards on sensitive reads
3. Check TenantFilter is active

### Issue: Audit logs not appearing

**Troubleshoot:**
```bash
# Check if RoleAuditService is injected
grep "roleAuditService" logs/application.log

# Check logger level
# application.properties should have:
# logging.level.com.medicalstore.service.RoleAuditService=INFO
```

---

## References

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [RBAC Best Practices](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
- [OWASP Authorization Testing](https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/05-Authorization_Testing/README)

---

**Last Updated:** March 1, 2026  
**Contributor Guide Version:** 1.0
