# Medical Store — 3-Role System Implementation

> **Project:** Medical Store Management System  
> **Roles:** `ADMIN` · `OWNER` · `SHOPKEEPER`  
> **Stack:** Spring Boot 3.x · Spring Security 6 · Thymeleaf · JPA/MySQL · Bootstrap 5.3

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Role Definitions & Permissions](#2-role-definitions--permissions)
3. [File Changes Summary](#3-file-changes-summary)
4. [Authentication & Security](#4-authentication--security)
5. [Multi-Tenancy Model](#5-multi-tenancy-model)
6. [Dashboard Data Flow](#6-dashboard-data-flow)
7. [API Endpoints Reference](#7-api-endpoints-reference)
8. [Database Schema Notes](#8-database-schema-notes)
9. [How to Extend](#9-how-to-extend)

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Spring Security                          │
│  /admin/**  →  ROLE_ADMIN only                                  │
│  /owner/**  →  ROLE_OWNER only                                  │
│  /medicines, /sales, /customers … → ROLE_ADMIN or ROLE_SHOPKEEPER│
│  /profile/**   →  any authenticated user                        │
│  /reports/**   →  all three roles                               │
└─────────────────────────────────────────────────────────────────┘
         │ success → AuthenticationSuccessHandler
         ▼
  ┌──────┬──────────┬────────────────────────────────┐
  │ADMIN │  OWNER   │      SHOPKEEPER                 │
  │/admin│  /owner  │  / (index.html — daily ops)     │
  └──────┴──────────┴────────────────────────────────┘
                │             │
                ▼             ▼
         TenantFilter sets TenantContext (ThreadLocal)
           ownerId           branchId
```

Login → `AuthenticationSuccessHandler` redirects:
- **ADMIN** → `/admin`
- **OWNER** → `/owner`
- **SHOPKEEPER** → `/` (daily operations dashboard)

---

## 2. Role Definitions & Permissions

### ADMIN
| Capability | Detail |
|---|---|
| Dashboard | Platform-wide KPIs, subscription stats, revenue |
| Users | Create / edit / disable / reset password for any user |
| Branches | Create / edit any branch; assign owner |
| Subscriptions | Manage plans for all owners |
| All operations | Full access to medicines, sales, customers, etc. |

### OWNER
| Capability | Detail |
|---|---|
| Dashboard | Aggregated KPIs across all owned branches |
| Branch Detail | Per-branch KPIs, charts, medicines, shopkeepers |
| Branch Comparison | Side-by-side revenue & stock health across branches |
| Shopkeeper Management | Create, activate/deactivate shopkeepers for their branches |
| Reports / Analytics | Scoped to their branches only via TenantContext |

**Auto-provisioning:** When an OWNER user is created (by admin or DataInitializer), a `FREE` subscription plan is automatically generated so they can log in immediately.

### SHOPKEEPER
| Capability | Detail |
|---|---|
| Dashboard | `/` — daily operations with Quick Action buttons |
| Medicines | CRUD scoped to their assigned branch |
| Sales | New sale, view today's sales |
| Customers | Add / list customers for their branch |
| Purchase / Returns | Branch-scoped operations |
| Reports | View reports for their branch |
| Password | Change own password at `/profile/change-password` |

---

## 3. File Changes Summary

### New Files Created

| File | Purpose |
|---|---|
| `model/Role.java` | Enum: `ADMIN`, `OWNER`, `SHOPKEEPER` with `getAuthority()`, `getValue()`, `from(String)` |
| `controller/ProfileController.java` | `/profile/change-password` — all roles can change their password |
| `templates/admin/edit-user.html` | Edit user form + reset password section |
| `templates/admin/edit-branch.html` | Edit branch + reassign owner |
| `templates/owner/compare.html` | Branch comparison page (cards + grouped bar chart + table) |
| `templates/profile/change-password.html` | Password change form with strength meter |

### Modified Files

| File | What Changed |
|---|---|
| `repository/UserRepository.java` | Added `findByRole()`, `countByRole()`, `findShopkeepersByBranchId()`, `findShopkeepersByOwnerId()` |
| `config/DataInitializer.java` | Auto-create FREE subscription plan for `default_owner` on startup |
| `controller/AdminController.java` | Enhanced dashboard; edit user/branch endpoints; create user auto-provisions subscription |
| `controller/OwnerController.java` | Full rewrite: DashboardService injection; compare endpoint with chart arrays; shopkeeper management |
| `service/MedicineService.java` | Added `getMedicinesByBranch(Long branchId)` |
| `config/SecurityConfig.java` | Added remember-me (7-day token); `deleteCookies("remember-me")` on logout |
| `resources/application.properties` | Added `server.servlet.session.timeout=30m` |
| `templates/admin/index.html` | 6 user KPI cards + 4 subscription cards + edit buttons on tables |
| `templates/owner/dashboard.html` | KPI cards + Chart.js bar chart + branch cards + critical stock alert |
| `templates/owner/branch-detail.html` | Per-branch KPIs + line/doughnut charts + last-login column |
| `templates/owner/shopkeepers.html` | Added Last Login column, activity badges, summary cards, branch filter tabs |
| `templates/index.html` (shopkeeper) | Added Quick Action buttons row (New Sale, Medicines, Customers, Purchase, Reports) |
| `templates/layout.html` | Added "Change Password" link in sidebar user profile section |

---

## 4. Authentication & Security

### Login Flow
```
POST /login
  → DaoAuthenticationProvider → CustomUserDetailsService.loadUserByUsername()
  → BCrypt password check
  → AuthenticationSuccessHandler:
       updateLastLogin(username)    // records User.lastLogin timestamp
       ADMIN  → redirect /admin
       OWNER  → redirect /owner
       default → redirect /
```

### Remember-Me
- Token valid **7 days**
- Key: `medicalStoreRememberMeKey2024` (change in production)
- Parameter name: `remember-me` (checkbox in `login.html`)
- Cookie deleted on logout alongside `JSESSIONID`

### Session Timeout
- `server.servlet.session.timeout=30m` — 30 minutes of inactivity

### Access Control Rules (SecurityConfig)
```
/login, /css/**, /js/**, /images/**, /error  → permitAll
/admin/**                                     → ROLE_ADMIN
/owner/**                                     → ROLE_OWNER
/analytics/**                                 → ROLE_ADMIN, ROLE_OWNER, ROLE_SHOPKEEPER
/reports/**                                   → ROLE_ADMIN, ROLE_OWNER, ROLE_SHOPKEEPER
/medicines/**, /sales/**, /customers/**,
  /returns/**, /suppliers/**, /purchases/**   → ROLE_ADMIN, ROLE_SHOPKEEPER
anyRequest                                    → authenticated (covers /profile/**)
```

### SubscriptionInterceptor
- Intercepts all requests for `OWNER` and `SHOPKEEPER`
- If no active subscription plan → redirects to `/subscription/billing`
- Adds `subscriptionWarning` model attribute for near-expiry banners

---

## 5. Multi-Tenancy Model

```java
// TenantContext (ThreadLocal)
TenantContext.setTenantId(branchId)   // SHOPKEEPER: their branch id
TenantContext.setOwnerId(ownerId)     // OWNER: their user id
// cleared in finally block by TenantFilter
```

All service methods check TenantContext:
- `MedicineService` — filters by `TenantContext.getTenantId()` when set
- `SaleService`, `CustomerService` — same pattern
- `DashboardService.buildBranchDashboard(branchId)` — explicit branchId parameter
- `DashboardService.buildOwnerDashboard(ownerId)` — aggregates across all branches of owner

### Branch ↔ Owner Relationship
```
Owner (User)  ──< Branch  ──< User (Shopkeeper)
                         ──< Medicine
                         ──< Sale
                         ──< Customer
```

---

## 6. Dashboard Data Flow

### Admin Dashboard (`/admin`)
```
AdminController.dashboard()
  → userRepository.countByRole("ADMIN/OWNER/SHOPKEEPER")
  → branchRepository counts
  → subscriptionPlanRepository counts (active, expired, free, pro, enterprise)
  → dashboardService.buildAdminDashboard() → todaySales, monthlyRevenue, todayTransactions
Model: totalUsers, totalOwners, totalShopkeepers, totalAdmins,
       totalBranches, activeBranches,
       activeSubscriptions, expiredSubscriptions, freePlans, proPlans, enterprisePlans,
       todaySales, monthlyRevenue, todayTransactions
```

### Owner Dashboard (`/owner`)
```
OwnerController.dashboard()
  → securityUtils.getCurrentUserId() → ownerId
  → dashboardService.buildOwnerDashboard(ownerId)
       → aggregate todaySales, monthlyRevenue, etc. across all owner's branches
  → userManagementService.findShopkeepersByOwnerId(ownerId).size() → owner-scoped shopkeeper count
  → branchService.getBranchesByOwner(ownerId) → branchCount
Model: all KPI keys from dashboardService map + lowStock alias + branchCount + shopkeeperCount
```

### Owner Branch Detail (`/owner/branches/{id}`)
```
OwnerController.branchDetail(id)
  → verifies branch belongs to this owner
  → dashboardService.buildBranchDashboard(id) → per-branch KPIs + salesTrendJson + categoryJson
  → userRepository.findShopkeepersByBranchId(id)
  → medicineService.getMedicinesByBranch(id)
Model: all KPI keys + shopkeepers list + medicines list
```

### Branch Comparison (`/owner/compare`)
```
OwnerController.compareBranches()
  → for each branch:
       dashboardService.buildBranchDashboard(b.getId())
       collect: todaySales, monthlyRevenue, totalMedicines, totalCustomers, lowStockCount, expiringIn30
  → build flat arrays for Chart.js: branchLabels, todaySalesData, monthlyData, medicinesData, lowStockData
Model: branchStats (List<Map>), branchLabels, todaySalesData, monthlyData, medicinesData, lowStockData
```

### Shopkeeper Dashboard (`/`)
```
HomeController.home()
  → if OWNER → redirect /owner
  → else → "index" template
     Dashboard KPIs loaded async via DashboardApiController (/api/dashboard)
     Chart data also loaded async → Chart.js initialized on data arrival
```

---

## 7. API Endpoints Reference

### Admin Endpoints (`/admin/**` — ROLE_ADMIN only)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/admin` | Admin dashboard (platform stats) |
| GET | `/admin/users` | List all users |
| GET | `/admin/users/create` | Show create user form |
| POST | `/admin/users/create` | Create user + auto-provision FREE plan for OWNER |
| GET | `/admin/users/edit/{id}` | Edit user form |
| POST | `/admin/users/edit/{id}` | Update user details |
| POST | `/admin/users/reset-password/{id}` | Reset user password |
| GET | `/admin/branches` | List all branches |
| GET | `/admin/branches/create` | Show create branch form |
| POST | `/admin/branches/create` | Create branch |
| GET | `/admin/branches/edit/{id}` | Edit branch form |
| POST | `/admin/branches/edit/{id}` | Update branch + reassign owner |
| GET | `/admin/subscriptions` | Manage subscriptions |

### Owner Endpoints (`/owner/**` — ROLE_OWNER only)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/owner` | Owner dashboard (aggregated KPIs + charts) |
| GET | `/owner/branches/{id}` | Branch detail (per-branch KPIs + charts) |
| GET | `/owner/compare` | Branch comparison (side-by-side + charts) |
| GET | `/owner/shopkeepers` | List shopkeepers with last-login & activity |
| POST | `/owner/shopkeepers/create` | Create new shopkeeper |
| POST | `/owner/shopkeepers/toggle/{id}` | Enable/disable shopkeeper |

### Profile Endpoints (all authenticated roles)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/profile/change-password` | Change password form |
| POST | `/profile/change-password` | Submit password change |

---

## 8. Database Schema Notes

### `users` table
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| username | VARCHAR | unique |
| password | VARCHAR | BCrypt encoded |
| full_name | VARCHAR | |
| email | VARCHAR | |
| enabled | BOOLEAN | default true |
| account_non_locked | BOOLEAN | default true |
| last_login | DATETIME | updated on every successful login |
| created_date | DATETIME | |
| created_by | VARCHAR | |
| branch_id | BIGINT FK | → branches; set for SHOPKEEPER |

### `user_roles` table (join)
| Column | Notes |
|--------|-------|
| user_id | FK → users |
| roles | `'ADMIN'` / `'OWNER'` / `'SHOPKEEPER'` |

Stored as plain strings, mapped by Spring Security as `ROLE_ADMIN` etc.

### `subscription_plans` table
| Column | Notes |
|--------|-------|
| owner_id | FK → users (owner) |
| plan_type | `FREE` / `PRO` / `ENTERPRISE` |
| start_date | LocalDate |
| end_date | LocalDate |
| max_users | Integer |
| max_branches | Integer |
| active | Boolean |

**Auto-provisioning rule:** When a new OWNER is created, a FREE plan (12-month validity, 5 users, 3 branches) is automatically saved.

---

## 9. How to Extend

### Add a new role (e.g., `PHARMACIST`)
1. Add `PHARMACIST` to `Role.java` enum
2. Add `ROLE_PHARMACIST` to relevant `requestMatchers` in `SecurityConfig`
3. Add `sec:authorize="hasRole('PHARMACIST')"` in layout sidebar for new nav items
4. Create a controller if needed with `@PreAuthorize("hasRole('PHARMACIST')")`

### Add a new dashboard KPI
1. Add the business logic to `DashboardService.buildBranchDashboard()` / `buildOwnerDashboard()`
2. Put the result in the returned `Map<String, Object>` with a new key
3. Access in templates with `${newKpiKey}`

### Add a new admin table column
1. Query the data in `AdminController.dashboard()`
2. Add it as a model attribute: `model.addAttribute("newData", ...)`
3. Use `th:text="${newData}"` in `admin/index.html`

### Change session timeout
```properties
# application.properties
server.servlet.session.timeout=60m   # change to your needs
```

### Change remember-me duration
```java
// SecurityConfig.filterChain()
.rememberMe(rm -> rm
    .tokenValiditySeconds(60 * 60 * 24 * 30)  // 30 days
    ...
```

---

## Running the Application

```bash
# Build (skip tests)
.\mvnw.cmd package -DskipTests

# Run
java -jar target/medicalstore-0.0.1-SNAPSHOT.jar

# Default test users (created by DataInitializer)
# Username      Password    Role
# admin         admin123    ADMIN
# default_owner owner123    OWNER
# shopkeeper1   shop123     SHOPKEEPER (if seeded)
```

---

*Generated: Implementation complete. All 3 roles (ADMIN, OWNER, SHOPKEEPER) are fully operational with role-based dashboards, multi-tenancy, subscription enforcement, and password management.*
