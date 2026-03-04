# MediStore — Role Hierarchy & Functionality Guide

> **Complete reference** for the 3-tier role system: who can do what, at what scope, and how it is enforced technically.

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Role Hierarchy Diagram](#2-role-hierarchy-diagram)
3. [Role Definitions](#3-role-definitions)
   - 3.1 [ADMIN — Platform Administrator](#31-admin--platform-administrator)
   - 3.2 [OWNER — Branch / Franchise Owner](#32-owner--branch--franchise-owner)
   - 3.3 [SHOPKEEPER — Store Operator](#33-shopkeeper--store-operator)
4. [Complete Permissions Matrix](#4-complete-permissions-matrix)
5. [URL Access Matrix](#5-url-access-matrix)
6. [API Endpoint Permissions](#6-api-endpoint-permissions)
7. [Data Scoping Rules](#7-data-scoping-rules)
8. [Subscription Wall](#8-subscription-wall)
9. [Login & Redirect Flow](#9-login--redirect-flow)
10. [Sidebar Navigation by Role](#10-sidebar-navigation-by-role)
11. [Notifications by Role](#11-notifications-by-role)
12. [Audit Logging](#12-audit-logging)
13. [Security Enforcement Layers](#13-security-enforcement-layers)
14. [Troubleshooting](#14-troubleshooting)

---

## 1. System Overview

MediStore uses a **3-tier Role-Based Access Control (RBAC)** system built on **Spring Security**.

```
┌─────────────────────────────────────────────────────────┐
│                  MediStore Platform                     │
├─────────────────────────────────────────────────────────┤
│  ROLE_ADMIN          ROLE_OWNER         ROLE_SHOPKEEPER │
│  (Platform Admin)   (Branch Owner)      (Operator)      │
└─────────────────────────────────────────────────────────┘
         │                  │                    │
         ▼                  ▼                    ▼
   ┌──────────┐       ┌──────────┐        ┌──────────┐
   │  /admin  │       │  /owner  │        │   /      │
   └──────────┘       └──────────┘        └──────────┘
   Sees ALL data    Sees own branches    Sees own branch
```

**Authentication:** Session-based Form Login (`JSESSIONID` cookie).  
**Authorization:** `@PreAuthorize` annotations on every controller + route guards in `SecurityConfig`.  
**Data isolation:** `TenantFilter` reads the logged-in user's profile and populates `TenantContext` on every request; all service queries automatically filter data based on that context.

---

## 2. Role Hierarchy Diagram

```
                        ┌─────────────────────────┐
                        │     MediStore Platform   │
                        └────────────┬────────────┘
                                     │
                                     ▼
                        ┌─────────────────────────┐
                        │        👑 ADMIN          │
                        │   Platform Administrator │
                        │   Badge: Yellow          │
                        │   Scope: ALL data        │
                        └────────────┬────────────┘
                                     │ creates & manages
                                     ▼
                        ┌─────────────────────────┐
                        │        🏢 OWNER          │
                        │   Branch/Franchise Owner │
                        │   Badge: Blue            │
                        │   Scope: Own branches    │
                        └────────────┬────────────┘
                                     │ hires & manages
                                     ▼
                        ┌─────────────────────────┐
                        │      🛒 SHOPKEEPER       │
                        │     Store Operator       │
                        │     Badge: Green         │
                        │ Scope: Single branch     │
                        └─────────────────────────┘
```

**Upward authority:**
- ADMIN can create, edit, disable, and delete any OWNER or SHOPKEEPER.
- OWNER can create, enable/disable Shopkeepers assigned to their own branches only.
- SHOPKEEPER has no user-management authority.

---

## 3. Role Definitions

---

### 3.1 ADMIN — Platform Administrator

> Full platform access. Manages the entire SaaS setup — users, branches, subscriptions, and all operational data.

**Database value:** `"ADMIN"` stored in `user_roles.role`  
**Spring Security name:** `ROLE_ADMIN`  
**Login redirects to:** `/admin`  
**Badge colour:** Yellow

#### What ADMIN Can Do

##### A. Platform Administration (`/admin/**`)

| Action | Endpoint | Description |
|--------|----------|-------------|
| View Admin Dashboard | `GET /admin` | Platform-wide KPIs — total users, branches, owners, platform health |
| List All Users | `GET /admin/users` | Every user in the system regardless of role or branch |
| Create User | `POST /admin/users/create` | Create OWNER or SHOPKEEPER accounts |
| Edit User | `POST /admin/users/edit/{id}` | Update name, email, enabled status |
| Toggle User | `POST /admin/users/toggle/{id}` | Enable or disable any user account |
| Delete User | `POST /admin/users/delete/{id}` | Permanently remove a user (cannot delete own account) |
| Reset Password | `POST /admin/users/reset-password/{id}` | Force-reset any user's password |
| List All Branches | `GET /admin/branches` | All branches across all owners |
| Create Branch | `POST /admin/branches/create` | Create a new branch and assign it to an owner |
| Toggle Branch | `POST /admin/branches/toggle/{id}` | Activate or deactivate a branch |
| View Subscriptions | `GET /admin/subscriptions` | Subscription status for every owner |
| Update Subscription | `POST /admin/subscriptions/update` | Set plan (FREE / PRO / ENTERPRISE), expiry date, max users, max branches |

##### B. Full Operational Access (Global Scope — all data, no branch filter)

| Area | Endpoints |
|------|-----------|
| Medicines & Inventory | `/medicines/**` |
| Sales & Billing | `/sales/**` |
| Returns | `/returns/**` |
| Customers | `/customers/**` |
| Suppliers | `/suppliers/**` |
| Purchase Orders | `/purchases/**` |

##### C. Reports & Analytics (Global)

| Report | Endpoint |
|--------|---------|
| Sales Report (daily/monthly/custom) | `/reports`, `/reports/daily`, `/reports/monthly`, `/reports/custom` |
| Expiry Report | `/reports/expiry` |
| Profit & Loss | `/reports/profit-loss` |
| GST / Tax Report | `/reports/gst` |
| Excel Export | `/reports/export/excel` |
| Analytics Hub | `/analytics` |
| Profit per Medicine | `/analytics/profit-per-medicine` |
| Dead Stock | `/analytics/dead-stock` |
| Fast Moving Items | `/analytics/fast-moving` |
| GST Summary | `/analytics/gst-summary` |
| Analytics Excel Export | `/analytics/export/excel` |

##### D. Rate Limits & Quotas

| Feature | ADMIN |
|---------|-------|
| API calls / hour | Unlimited |
| Bulk export | Allowed |
| User creation | Allowed |
| Report generation | Unlimited |

##### E. What ADMIN Cannot Do

- Cannot delete their own account (self-delete is blocked).
- Cannot bypass audit logging — all sensitive actions are logged.

---

### 3.2 OWNER — Branch / Franchise Owner

> Business owner. Sees only their own branches and the shopkeepers working in them. Cannot access storefront operations (medicines, sales, etc.) directly — that is handled per-branch by shopkeepers.

**Database value:** `"OWNER"` stored in `user_roles.role`  
**Spring Security name:** `ROLE_OWNER`  
**Login redirects to:** `/owner`  
**Badge colour:** Blue

#### What OWNER Can Do

##### A. Owner Portal (`/owner/**`)

| Action | Endpoint | Description |
|--------|----------|-------------|
| Owner Dashboard | `GET /owner` | Aggregated stats across **all owned branches**: medicines count, customers, today's sales, low-stock alerts |
| Branch Detail | `GET /owner/branches/{id}` | Drill into one branch — medicines, recent sales, assigned shopkeepers |
| Compare Branches | `GET /owner/compare` | Side-by-side revenue, medicine count, low-stock comparison across branches |
| List Shopkeepers | `GET /owner/shopkeepers` | Every shopkeeper assigned to the owner's branches |
| Create Shopkeeper | `POST /owner/shopkeepers/create` | Add a shopkeeper and assign them to one of the owner's branches |
| Toggle Shopkeeper | `POST /owner/shopkeepers/toggle/{id}` | Enable or disable a shopkeeper account |
| View Subscription | `GET /owner/subscription` | Current plan, expiry date, usage statistics, upgrade options |

##### B. Reports & Analytics (Scoped to Own Branches)

Same report and analytics URLs as ADMIN, but all queries are automatically filtered to the owner's branches only through `TenantContext.ownerId`.

| Report | Endpoint | Scope |
|--------|----------|-------|
| All standard reports | `/reports/**` | Own branches |
| All analytics | `/analytics/**` | Own branches |
| GST / Tax | `/reports/gst` | Own branches |
| Excel export | `/reports/export/excel` | Own branches |

##### C. Rate Limits & Quotas

| Feature | OWNER |
|---------|-------|
| API calls / hour | 1,000 |
| Bulk export | Allowed |
| Report generation | Per-branch |

##### D. What OWNER Cannot Do

| Blocked Action | Result |
|---------------|--------|
| Access `/admin/**` | HTTP 403 → redirected to `/owner?denied=true` |
| Access `/medicines/**` directly | HTTP 403 |
| Access `/sales/**` directly | HTTP 403 |
| Access `/customers/**` directly | HTTP 403 |
| Access `/returns/**` directly | HTTP 403 |
| Access `/suppliers/**` directly | HTTP 403 |
| Access `/purchases/**` directly | HTTP 403 |
| Access another owner's branches | HTTP 403 + audit log `ESCALATION_ATTEMPT` |
| View bell notifications | Not shown in header |

---

### 3.3 SHOPKEEPER — Store Operator

> Store operator. Works inside a single assigned branch. Can sell, manage medicines, handle customers and returns — all scoped to their one branch only.

**Database value:** `"SHOPKEEPER"` stored in `user_roles.role`  
**Spring Security name:** `ROLE_SHOPKEEPER`  
**Login redirects to:** `/` (Dashboard)  
**Badge colour:** Green

#### What SHOPKEEPER Can Do

##### A. Branch Dashboard (`/`)

| Feature | Detail |
|---------|--------|
| KPIs | Medicines count, customers, today's sales, this month's revenue |
| Low-stock panel | Medicines below threshold in their branch |
| Expiry panel | Medicines expiring within 7 / 30 days |
| Sales trend | Today's hourly sales trend chart |

##### B. Inventory Management (Branch-Scoped)

| Action | Endpoint | Detail |
|--------|----------|--------|
| View medicines | `GET /medicines` | own branch only |
| Add medicine | `POST /medicines/save` | Added to own branch |
| Edit medicine | `POST /medicines/edit/{id}` | Own branch medicines only |
| Bulk delete | `POST /medicines/delete` | Own branch only |
| Low-stock alert | `/medicines?filter=low-stock` | Own branch |
| Expiry alert | `/medicines?filter=expiring` | Own branch |
| Create purchase order | `POST /purchases/save` | For own branch |
| Edit / receive / cancel PO | `/purchases/**` | Own branch |
| Manage suppliers | `/suppliers/**` | View, add, edit, delete |

##### C. Sales & Billing (Branch-Scoped)

| Action | Endpoint | Detail |
|--------|----------|--------|
| New sale (POS) | `GET /sales/new` | Barcode / name search, discount, GST auto-calc |
| Save sale | `POST /sales/save` | Deducts stock automatically |
| View sales history | `GET /sales` | Own branch only, paginated |
| Sale detail / invoice | `GET /sales/{id}` | View & print invoice PDF |
| Delete sale | `POST /sales/delete/{id}` | Restores stock |

**POS Sale payload includes:**
- Customer info (name, phone, address)
- Line items (medicineId, quantity, price)
- Payment method: CASH / CARD / CHECK
- Discount percentage
- GST percentage

##### D. Returns (`/returns/**`, Branch-Scoped)

| Action | Endpoint |
|--------|----------|
| Create return | `POST /returns/save` |
| View returns | `GET /returns` |
| Return detail | `GET /returns/{id}` |

##### E. Customer Management (Branch-Scoped)

| Action | Endpoint |
|--------|----------|
| View customers | `GET /customers` |
| Add customer | `POST /customers/save` |
| Edit customer | `POST /customers/edit/{id}` |
| Purchase history | `GET /customers/{id}/history` |

**Customer fields:** name, phone, email, address, city, GSTIN.

##### F. Reports & Analytics (Branch-Scoped)

Same URLs as ADMIN/OWNER — but all queries filtered to the shopkeeper's single branch via `TenantContext.tenantId`.

##### G. Rate Limits & Quotas

| Feature | SHOPKEEPER |
|---------|-----------|
| API calls / hour | 500 |
| Bulk export | Not allowed |
| Report PDF export | Not allowed |
| Report CSV export | Allowed |

##### H. What SHOPKEEPER Cannot Do

| Blocked Action | Result |
|---------------|--------|
| Access `/admin/**` | HTTP 403 |
| Access `/owner/**` | HTTP 403 |
| See any other branch's data | Blocked by TenantFilter |
| Create / manage other users | No access |

---

## 4. Complete Permissions Matrix

| Feature | ADMIN | OWNER | SHOPKEEPER |
|---------|:-----:|:-----:|:----------:|
| **Platform Management** | | | |
| Manage all users | ✅ | ❌ | ❌ |
| Manage all branches | ✅ | ❌ | ❌ |
| Manage subscriptions (global) | ✅ | ❌ | ❌ |
| View own subscription | ✅ | ✅ | ❌ |
| **Owner/Shopkeeper Management** | | | |
| Create Owner accounts | ✅ | ❌ | ❌ |
| Create Shopkeeper accounts | ✅ | ✅ (own branches) | ❌ |
| Enable / disable any user | ✅ | ❌ | ❌ |
| Enable / disable own shopkeepers | ✅ | ✅ | ❌ |
| **Daily Operations** | | | |
| Dashboard | ✅ (global) | ✅ (portfolio) | ✅ (branch) |
| Medicines | ✅ (all) | ❌ | ✅ (branch) |
| Sales | ✅ (all) | ❌ | ✅ (branch) |
| Returns | ✅ (all) | ❌ | ✅ (branch) |
| Customers | ✅ (all) | ❌ | ✅ (branch) |
| Suppliers | ✅ (all) | ❌ | ✅ (branch) |
| Purchases | ✅ (all) | ❌ | ✅ (branch) |
| **Reports & Analytics** | | | |
| Standard reports | ✅ (global) | ✅ (scoped) | ✅ (scoped) |
| Advanced analytics | ✅ (global) | ✅ (scoped) | ✅ (scoped) |
| GST / Tax report | ✅ (global) | ✅ (scoped) | ✅ (scoped) |
| Profit & Loss | ✅ (global) | ✅ (scoped) | ✅ (scoped) |
| Excel / PDF export | ✅ | ✅ | CSV only |
| **Notifications (Bell icon)** | | | |
| Low-stock alerts | ✅ (all branches) | ❌ | ✅ (own branch) |
| Expiry alerts | ✅ (all branches) | ❌ | ✅ (own branch) |
| **Account** | | | |
| Change own password | ✅ | ✅ | ✅ |
| Reset any user's password | ✅ | ❌ | ❌ |

---

## 5. URL Access Matrix

| URL Pattern | ADMIN | OWNER | SHOPKEEPER |
|-------------|:-----:|:-----:|:----------:|
| `/admin/**` | ✅ | ❌ (403) | ❌ (403) |
| `/owner/**` | ❌ (403) | ✅ | ❌ (403) |
| `/` (Dashboard) | ✅ | ❌ | ✅ |
| `/medicines/**` | ✅ | ❌ | ✅ |
| `/sales/**` | ✅ | ❌ | ✅ |
| `/customers/**` | ✅ | ❌ | ✅ |
| `/returns/**` | ✅ | ❌ | ✅ |
| `/suppliers/**` | ✅ | ❌ | ✅ |
| `/purchases/**` | ✅ | ❌ | ✅ |
| `/reports/**` | ✅ | ✅ | ✅ |
| `/analytics/**` | ✅ | ✅ | ✅ |
| `/profile/**` | ✅ | ✅ | ✅ |
| `/subscription/billing` | ✅ | ✅ | ✅ (public) |
| `/login` | Public | Public | Public |

---

## 6. API Endpoint Permissions

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
- **Type:** Session-based Form Login
- **Cookie:** `JSESSIONID` (set automatically on login)
- **CSRF:** Required for POST / PUT / DELETE (submitted automatically with forms)

### Dashboard KPIs

```http
GET /api/v1/dashboard/kpis
```

| Role | Scope | Allowed |
|------|-------|---------|
| ADMIN | Platform-wide | ✅ |
| OWNER | Own branches | ✅ |
| SHOPKEEPER | Own branch | ✅ |

**Response:**
```json
{
  "todaySales": 15000.50,
  "monthlyRevenue": 450000.00,
  "totalCustomers": 234,
  "totalMedicines": 567,
  "lowStockCount": 12,
  "expiringIn30": 5
}
```

### Medicines API

| Endpoint | Method | ADMIN | OWNER | SHOPKEEPER |
|----------|--------|:-----:|:-----:|:----------:|
| `/medicines` | GET | ✅ | ❌ | ✅ |
| `/medicines/save` | POST | ✅ | ❌ | ✅ |
| `/medicines/edit/{id}` | POST | ✅ | ❌ | ✅ |
| `/medicines/delete` | POST | ✅ | ❌ | ✅ |
| `/api/v1/medicines/search?q=` | GET | ✅ | ❌ | ✅ |

### Sales API

| Endpoint | Method | ADMIN | OWNER | SHOPKEEPER |
|----------|--------|:-----:|:-----:|:----------:|
| `/sales` | GET | ✅ | ❌ | ✅ |
| `/sales/new` | GET | ✅ | ❌ | ✅ |
| `/sales/save` | POST | ✅ | ❌ | ✅ |
| `/sales/{id}` | GET | ✅ | ❌ | ✅ |
| `/sales/delete/{id}` | POST | ✅ | ❌ | ✅ |

### Admin API

| Endpoint | Method | ADMIN | Notes |
|----------|--------|:-----:|-------|
| `/admin` | GET | ✅ | Platform dashboard |
| `/admin/users` | GET | ✅ | List all users |
| `/admin/users/create` | POST | ✅ | Audited |
| `/admin/users/edit/{id}` | POST | ✅ | Audited |
| `/admin/users/toggle/{id}` | POST | ✅ | Audited |
| `/admin/users/delete/{id}` | POST | ✅ | Audited; cannot delete self |
| `/admin/users/reset-password/{id}` | POST | ✅ | Audited |
| `/admin/branches` | GET | ✅ | All branches |
| `/admin/branches/create` | POST | ✅ | Audited |
| `/admin/branches/toggle/{id}` | POST | ✅ | Audited |
| `/admin/subscriptions` | GET | ✅ | All owners |
| `/admin/subscriptions/update` | POST | ✅ | Audited |

### Owner API

| Endpoint | Method | OWNER | Notes |
|----------|--------|:-----:|-------|
| `/owner` | GET | ✅ | Portfolio dashboard |
| `/owner/branches/{id}` | GET | ✅ | Own branches only |
| `/owner/compare` | GET | ✅ | Cross-branch comparison |
| `/owner/shopkeepers` | GET | ✅ | Own shopkeepers only |
| `/owner/shopkeepers/create` | POST | ✅ | Audited |
| `/owner/shopkeepers/toggle/{id}` | POST | ✅ | Audited |
| `/owner/subscription` | GET | ✅ | Own subscription |

### Reports API (All Roles — Scoped)

| Endpoint | Method | Role Scope |
|----------|--------|-----------|
| `/reports` | GET | All (auto-scoped) |
| `/reports/daily` | GET | All (auto-scoped) |
| `/reports/monthly` | GET | All (auto-scoped) |
| `/reports/custom` | GET | All (auto-scoped) |
| `/reports/expiry` | GET | All (auto-scoped) |
| `/reports/profit-loss` | GET | All (auto-scoped) |
| `/reports/gst` | GET | All (auto-scoped) |
| `/reports/export/excel` | GET | ADMIN & OWNER only |
| `/analytics` | GET | All (auto-scoped) |
| `/analytics/export/excel` | GET | ADMIN & OWNER only |

### Error Responses

| Status | Meaning |
|--------|--------|
| `401 Unauthorized` | Not logged in → redirect to `/login` |
| `403 Forbidden` | Logged in but wrong role → redirect to role home with `?denied=true` |
| `404 Not Found` | Resource does not exist |

---

## 7. Data Scoping Rules

Even when two roles share the same URL, **what they see is different**. Scoping is enforced automatically by `TenantFilter` and the service layer.

| Role | Context Variable | SQL Effect |
|------|-----------------|------------|
| ADMIN | None | No WHERE clause — sees all rows |
| OWNER | `TenantContext.ownerId` | `WHERE owner_id = {ownerId}` applied across all queries |
| SHOPKEEPER | `TenantContext.tenantId` (branch ID) | `WHERE branch_id = {branchId}` applied across all queries |

### How It Works

1. User authenticates → Spring Security loads `UserDetails`.
2. `TenantFilter` runs on every request — reads the user's `ownerId` / `branchId` from their profile and stores it in `TenantContext`.
3. All service methods (`MedicineService`, `SaleService`, `CustomerService`, etc.) read `TenantContext` and append the correct WHERE clause before executing the query.
4. Even if a user manually crafts a URL with another branch's ID, the WHERE clause prevents data leakage.

---

## 8. Subscription Wall

Subscriptions are attached to **OWNER** accounts. SHOPKEEPER access is cascaded from their associated OWNER.

### Plans

| Plan | Features |
|------|---------|
| FREE | Limited users, limited branches, basic reports |
| PRO | More users, more branches, advanced analytics |
| ENTERPRISE | Unlimited users, unlimited branches, all features |

### What Happens When a Subscription Expires

```
Owner subscription expires
         │
         ├──▶ OWNER: every request → redirect to /subscription/billing
         │
         └──▶ All SHOPKEEPERS under that owner: every request → redirect to /subscription/billing
```

Only ADMIN can renew the subscription via `/admin/subscriptions/update`.

### Who Can Access `/subscription/billing`

| Role | Access |
|------|--------|
| ADMIN | ✅ Always |
| OWNER | ✅ Always (to pay / view plan) |
| SHOPKEEPER | ✅ Read-only (informed, cannot pay) |

---

## 9. Login & Redirect Flow

```
User submits /login (POST)
         │
         ▼
Spring Security authenticates credentials
         │
         ├── ADMIN   ──▶  /admin
         ├── OWNER   ──▶  /owner
         └── SHOPKEEPER ──▶  /  (Dashboard)
```

### Post-Login Behaviour

| Role | First Page | If account disabled |
|------|-----------|-------------------|
| ADMIN | `/admin` — Platform KPI dashboard | Login rejected |
| OWNER | `/owner` — Portfolio dashboard | Login rejected |
| SHOPKEEPER | `/` — Branch dashboard | Login rejected |

`updateLastLogin()` is called on every successful login to track activity.

---

## 10. Sidebar Navigation by Role

### ADMIN Sidebar

| Section | Links |
|---------|-------|
| Platform Admin | Admin Panel `/admin`, Manage Users `/admin/users`, Manage Branches `/admin/branches`, Subscriptions `/admin/subscriptions` |
| Dashboard | Global Dashboard `/` |
| Inventory | Medicines `/medicines`, Purchases `/purchases`, Suppliers `/suppliers` |
| Sales & Billing | New Sale `/sales/new`, Sales History `/sales`, Returns `/returns`, Customers `/customers` |
| Reports | Reports `/reports`, Analytics `/analytics`, GST/Tax `/reports/gst` |

### OWNER Sidebar

| Section | Links |
|---------|-------|
| My Portfolio | Owner Dashboard `/owner`, Shopkeepers `/owner/shopkeepers` |
| Reports | Standard Reports `/reports`, Advanced Analytics `/analytics`, GST/Tax `/reports/gst` |

### SHOPKEEPER Sidebar

| Section | Links |
|---------|-------|
| Dashboard | Branch Dashboard `/` |
| Inventory | Medicines `/medicines`, Purchases `/purchases`, Suppliers `/suppliers` |
| Sales & Billing | New Sale `/sales/new`, Sales History `/sales`, Returns `/returns`, Customers `/customers` |
| Reports | Standard Reports `/reports`, Advanced Analytics `/analytics`, GST/Tax `/reports/gst` |

---

## 11. Notifications by Role

The bell icon in the header shows real-time alerts. Visibility and scope differ by role.

| Notification | ADMIN | OWNER | SHOPKEEPER |
|-------------|:-----:|:-----:|:----------:|
| Low-stock alert | ✅ All branches | ❌ Hidden | ✅ Own branch |
| Expiry alert | ✅ All branches | ❌ Hidden | ✅ Own branch |
| Badge count | Global total | — | Branch total |

---

## 12. Audit Logging

Every sensitive action is written to the application log by `RoleAuditService`.

### Log Format

```
AUDIT_LOG   | User: <username> | Roles: <ROLE> | Action: <ACTION_CODE> | Description: <detail>
AUDIT_DENIED| User: <username> | Roles: <ROLE> | Attempted: <URL>      | Reason: Forbidden
```

### Actions Logged Per Role

#### ADMIN Actions

| Action Code | Trigger |
|-------------|---------|
| `USER_CREATED` | Creating any user |
| `USER_DISABLED` | Disabling a user account |
| `USER_DELETED` | Deleting a user |
| `PASSWORD_RESET_BY_ADMIN` | Resetting any user's password |
| `BRANCH_CREATED` | Creating a new branch |
| `BRANCH_TOGGLED` | Enabling/disabling a branch |
| `SUBSCRIPTION_CHANGED` | Updating an owner's subscription plan |

#### OWNER Actions

| Action Code | Trigger |
|-------------|---------|
| `SHOPKEEPER_CREATED` | Hiring a new shopkeeper |
| `SHOPKEEPER_TOGGLED` | Enabling/disabling a shopkeeper |
| `DASHBOARD_ACCESS` | Accessing the owner dashboard |

#### SHOPKEEPER Actions

| Action Code | Trigger |
|-------------|---------|
| `SALE_COMPLETED` | Processing a sale |
| `RETURN_PROCESSED` | Processing a return |
| `MEDICINE_ADDED` | Adding a new medicine |

#### Access Denial Events

| Action Code | Trigger |
|-------------|---------|
| `AUDIT_DENIED` | Access to forbidden URL |
| `ESCALATION_ATTEMPT` | Attempting to access another entity's data (e.g., another owner's branch) |

### Sample Log Lines

```
2026-03-02 10:15:22  INFO  AUDIT_LOG | User: admin | Roles: ADMIN | Action: USER_CREATED | Description: Created user 'owner_john' with role 'OWNER'
2026-03-02 10:18:45  INFO  AUDIT_LOG | User: owner_john | Roles: OWNER | Action: SHOPKEEPER_CREATED | Description: Created shopkeeper 'shop_ravi' for branch ID 3
2026-03-02 10:45:12  INFO  AUDIT_LOG | User: shop_ravi | Roles: SHOPKEEPER | Action: SALE_COMPLETED | Description: Processed sale #1034, total ₹1,250
2026-03-02 11:02:30  WARN  AUDIT_DENIED | User: owner_john | Roles: OWNER | Attempted: /admin/users | Reason: Forbidden - insufficient permissions
2026-03-02 11:05:44  WARN  AUDIT_DENIED | User: shop_ravi | Roles: SHOPKEEPER | Attempted: /owner/branches/3 | Reason: Forbidden - insufficient permissions
```

### Searching Audit Logs

```bash
# All access denials
grep "AUDIT_DENIED" logs/application.log

# Actions by a specific user
grep "User: owner_john" logs/application.log

# All escalation attempts
grep "ESCALATION_ATTEMPT" logs/application.log
```

---

## 13. Security Enforcement Layers

Security is applied at **four levels** — a request must pass all four.

### Layer 1: Spring Security Route Guards (`SecurityConfig`)

```java
http.authorizeHttpRequests(authz -> authz
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/owner/**").hasRole("OWNER")
    .requestMatchers("/medicines/**", "/sales/**", "/customers/**",
                     "/returns/**", "/suppliers/**", "/purchases/**")
        .hasAnyRole("ADMIN", "SHOPKEEPER")
    .requestMatchers("/reports/**", "/analytics/**")
        .hasAnyRole("ADMIN", "OWNER", "SHOPKEEPER")
    .anyRequest().authenticated()
);
```

### Layer 2: Controller-Level `@PreAuthorize`

```java
@PreAuthorize("hasRole('ADMIN')")
public class AdminController { ... }

@PreAuthorize("hasRole('OWNER')")
public class OwnerController { ... }

@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
public class MedicineController { ... }
```

### Layer 3: Data Ownership Check (Post-Load)

```java
Branch branch = branchService.getBranchById(id).orElseThrow(...);

// Verify the current owner actually owns this branch
if (!branch.getOwner().getId().equals(securityUtils.getCurrentOwnerId())) {
    roleAuditService.logEscalationAttempt("/branch/" + id, "OWNER", "...");
    throw new AccessDeniedException("Access denied");
}
```

### Layer 4: Thymeleaf UI Guards (Template Level)

```html
<!-- Only ADMIN sees the Delete button -->
<button sec:authorize="hasRole('ADMIN')" class="btn btn-danger">
    Delete User
</button>

<!-- Only SHOPKEEPER sees the New Sale button -->
<a sec:authorize="hasAnyRole('ADMIN','SHOPKEEPER')"
   href="/sales/new" class="btn btn-primary">New Sale</a>
```

---

## 14. Troubleshooting

### User Gets Unexpected 403

1. Check the user's role in the database:
   ```sql
   SELECT * FROM user_roles WHERE user_id = ?;
   ```
2. Check audit log for the denial:
   ```bash
   grep "AUDIT_DENIED" logs/application.log | grep <username>
   ```
3. Verify `SecurityConfig` route mapping includes the endpoint.
4. Verify `@PreAuthorize` annotation on the controller class / method.

### Data From Wrong Branch Appearing

1. Verify `TenantFilter` is registered and active.
2. Check service method is reading from `TenantContext`, not querying `.findAll()`.
3. Confirm user's `branchId` / `ownerId` is correctly set in the user profile.

### Subscription Wall Not Lifting After Renewal

1. Ask the user to log out and log back in (session cache refresh).
2. Verify ADMIN updated the expiry date via `/admin/subscriptions/update`.
3. Check `SubscriptionInterceptor` — it re-reads subscription status on every request.

### Audit Logs Not Appearing

1. Confirm logging level in `application.properties`:
   ```properties
   logging.level.com.medicalstore.service.RoleAuditService=INFO
   ```
2. Verify `RoleAuditService` is `@Autowired` in the controller.
3. Ensure the log file path is writable.

---

## Quick Reference Card

```
┌───────────────┬─────────────────────────┬────────────────────────────┬─────────────────────────┐
│ Role          │ Scope                   │ Portal                     │ Key Capability          │
├───────────────┼─────────────────────────┼────────────────────────────┼─────────────────────────┤
│ ADMIN         │ ALL data, no filter     │ /admin                     │ Platform management     │
│               │                         │                            │ User/branch/subscription│
├───────────────┼─────────────────────────┼────────────────────────────┼─────────────────────────┤
│ OWNER         │ Own branches only       │ /owner                     │ Portfolio overview      │
│               │ (WHERE owner_id = X)    │                            │ Shopkeeper management   │
├───────────────┼─────────────────────────┼────────────────────────────┼─────────────────────────┤
│ SHOPKEEPER    │ Own branch only         │ / (Dashboard)              │ Daily POS operations    │
│               │ (WHERE branch_id = Y)   │                            │ Inventory, Sales, CRM   │
└───────────────┴─────────────────────────┴────────────────────────────┴─────────────────────────┘
```

---

*Last Updated: March 2, 2026 | Version: 1.0*
