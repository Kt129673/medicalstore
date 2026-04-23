# MedicalStore — Product Overview

A multi-branch pharmacy management SaaS for end-to-end store operations. Targets three user roles: **Admin** (platform governance), **Owner** (multi-branch oversight), and **Shopkeeper** (daily store operations).

## Core Capabilities

- **Inventory** — Medicine CRUD, batch/expiry tracking, low-stock and dead-stock detection
- **Sales & Billing** — POS interface, GST-compliant invoicing, PDF invoice generation, sale returns
- **Purchasing** — Supplier management, purchase orders, supplier credit tracking
- **Customers** — Customer database, purchase history, credit balances
- **Analytics & Reports** — Fast-moving items, profit-per-medicine, daily/monthly/GST/P&L reports, PDF & Excel export
- **Multi-Branch** — Branch-level data isolation via tenant context; owner-level cross-branch dashboards
- **Subscriptions** — Tiered plan-based feature gating with auto-expiry enforcement
- **Notifications** — Twilio WhatsApp alerts for expiry and low-stock events
- **Scheduled Jobs** — Daily expiry alerts (06:00), daily subscription enforcement (01:00), weekly soft-delete purge (Sunday 03:00)

## Access Model

```
ADMIN       →  full platform control (users, branches, subscriptions, audit logs)
OWNER       →  multi-branch analytics, reports, subscription management
SHOPKEEPER  →  medicines, sales, purchases, customers, returns
```

Role hierarchy: `ADMIN > OWNER` (ADMIN inherits OWNER permissions for impersonation drill-down). OWNER does **not** inherit SHOPKEEPER — this enforces strict SaaS separation.

Access is enforced at two levels:
1. URL security rules in `SecurityConfig.java`
2. Runtime feature flags in `FeatureFlags.java` + `@PreAuthorize` on methods

## Feature Flags (FeatureFlags.java)

| Flag | ADMIN | OWNER | SHOPKEEPER |
|---|---|---|---|
| `ADVANCED_ANALYTICS` | ✅ | ✅ | ❌ |
| `BULK_OPERATIONS` | ✅ | ❌ | ❌ |
| `EXPORT_REPORTS` | ✅ | ✅ | ❌ |
| `API_ACCESS` | ✅ | ❌ | ❌ |
| `MULTI_BRANCH_COMPARISON` | ✅ | ✅ | ❌ |
| `SUBSCRIPTION_MANAGEMENT` | ✅ | ✅ | ❌ |
| `USER_MANAGEMENT` | ✅ | ❌ | ❌ |
| `CUSTOM_REPORTS` | ✅ | ✅ | ❌ |

Check `FeatureFlags.isFeatureEnabled(flag, role)` before adding role-conditional UI or service logic.

## Subscription Plan Tiers

| Plan | Max Users | Max Branches | Duration |
|---|---|---|---|
| `FREE` | 50 | 10 | 10 years (default) |
| `PRO` | 10 | 5 | 12 months |
| `ENTERPRISE` | 30 | 20 | 24 months |

Plan enforcement runs daily at 01:00 via `ScheduledJobService`. Feature access per plan is defined in `SubscriptionFeature` records seeded by `DataInitializer`.

## Default Seed Credentials (Dev/Demo)

Seeded by `DataInitializer` on first run — idempotent, never duplicated:

| Username | Password | Role | Branch |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | — |
| `default_owner` | `Owner@123` | OWNER | Default Branch |
| `shop1` | `shop123` | SHOPKEEPER | Default Branch |
| `owner_pro` | `OwnerPro@123` | OWNER | Sharma Medicals (2 branches) |
| `owner_enterprise` | `OwnerEnt@123` | OWNER | Patel HealthCare (3 branches) |
| `shop_pro_1/2` | `ShopPro@123` | SHOPKEEPER | Sharma Medicals branches |
| `shop_ent_1/2/3` | `ShopEnt@123` | SHOPKEEPER | Patel HealthCare branches |

## Post-Login Redirects

| Role | Redirect |
|---|---|
| ADMIN | `/admin/dashboard` |
| OWNER | `/owner/dashboard` |
| SHOPKEEPER | `/dashboard` |

## Design Principles

**CRITICAL: Every new feature MUST be designed for scalability.**

- **Performance at scale** — Must work with 1000+ branches, 100K+ medicines, millions of transactions
- **Database efficiency** — Proper indexing, avoid N+1 queries, leverage caching
- **Async processing** — Long-running operations (reports, notifications) must be async or scheduled
- **Multi-tenancy isolation** — Always scope queries via `TenantContext`; never leak cross-tenant data
- **Horizontal scalability** — Stateless services only; no in-memory state between requests
- **Connection pooling** — Respect HikariCP limits (max-pool=20)
- **Cache strategy** — Use Caffeine caching, always evict stale data on writes
- **Rate limiting** — Protect expensive endpoints with Bucket4j
- **Batch operations** — Prefer bulk inserts/updates over row-by-row operations
