# MediStore — Roles & Access Reference

## Overview

The system has **three roles**. Each role sees a different sidebar and can access different URLs.

| Role        | Login redirects to | Badge colour |
|-------------|--------------------|--------------|
| `ADMIN`     | `/admin`           | Yellow       |
| `OWNER`     | `/owner`           | Blue         |
| `SHOPKEEPER`| `/` (Dashboard)    | Green        |

---

## ADMIN

> Full platform access. Manages the entire SaaS setup — users, branches, subscriptions, and all operational data.

### Sidebar sections visible to ADMIN
| Section         | Links                                              |
|-----------------|----------------------------------------------------|
| Platform Admin  | Admin Panel, Manage Users, Manage Branches, Subscriptions |
| Dashboard       | `/` — global KPI dashboard                        |
| Inventory       | Medicines, Purchases, Suppliers                    |
| Sales & Billing | New Sale, Sales History, Returns, Customers        |
| Reports         | Standard Reports, Advanced Analytics, GST / Tax   |

### What ADMIN can do

#### Platform Admin (`/admin/**`)
| Page | URL | What it does |
|------|-----|--------------|
| Admin Dashboard | `/admin` | Totals for all users, branches, owners; quick-links |
| Manage Users | `/admin/users` | List every user in the system |
| Create User | `/admin/users/create` | Create OWNER or SHOPKEEPER accounts |
| Toggle User | `/admin/users/toggle/{id}` | Enable / disable any user account |
| Delete User | `/admin/users/delete/{id}` | Permanently delete a user (cannot delete self) |
| Manage Branches | `/admin/branches` | List all branches across all owners |
| Create Branch | `/admin/branches/create` (POST) | Create a new branch and assign it to an owner |
| Toggle Branch | `/admin/branches/toggle/{id}` | Activate / deactivate a branch |
| Subscriptions | `/admin/subscriptions` | View subscription status for every owner |
| Update Subscription | `/admin/subscriptions/update` (POST) | Set plan type (FREE / PRO / ENTERPRISE), expiry date, max users, max branches |

#### Operational (same access as SHOPKEEPER, but sees **all** data, not branch-scoped)
| Area | URLs |
|------|------|
| Medicines | `/medicines/**` |
| Sales | `/sales/**` |
| Customers | `/customers/**` |
| Returns | `/returns/**` |
| Suppliers | `/suppliers/**` |
| Purchases | `/purchases/**` |

#### Reports & Analytics
| Area | URLs |
|------|------|
| Standard Reports | `/reports`, `/reports/monthly`, `/reports/daily`, `/reports/custom` |
| Expiry Report | `/reports/expiry` |
| Profit & Loss | `/reports/profit-loss` |
| GST Report | `/reports/gst` |
| Excel Export | `/reports/export/excel` |
| Analytics Hub | `/analytics` |
| Profit per Medicine | `/analytics/profit-per-medicine` |
| Dead Stock | `/analytics/dead-stock` |
| Fast Moving | `/analytics/fast-moving` |
| GST Summary | `/analytics/gst-summary` |
| Analytics Excel Export | `/analytics/export/excel` |

#### Notifications
- Bell icon shows **low-stock count** and **expiring-soon count** (all branches).

---

## OWNER

> Business owner. Sees only their own branches and the people who work in them. Cannot access the storefront operations (medicines, sales, etc.) directly — that is done per-branch by shopkeepers.

### Sidebar sections visible to OWNER
| Section     | Links                              |
|-------------|------------------------------------|
| My Portfolio| Owner Dashboard, Shopkeepers       |
| Reports     | Standard Reports, Advanced Analytics, GST / Tax |

### What OWNER can do

#### Owner Portal (`/owner/**`)
| Page | URL | What it does |
|------|-----|--------------|
| Owner Dashboard | `/owner` | Aggregated stats (medicines, customers, today's sales, low-stock) across **all owned branches** |
| Branch Detail | `/owner/branches/{id}` | Drill into one branch — medicines, recent sales, assigned shopkeepers |
| Shopkeepers | `/owner/shopkeepers` | List every shopkeeper assigned to the owner's branches |
| Create Shopkeeper | `/owner/shopkeepers/create` (POST) | Add a shopkeeper and assign to one of their branches |
| Toggle Shopkeeper | `/owner/shopkeepers/toggle/{id}` | Enable / disable a shopkeeper account |

#### Reports & Analytics (data scoped to owner's branches)
Same report/analytics URLs as ADMIN, but all queries are **filtered to the owner's branches only**.

#### What OWNER **cannot** do
- Access `/admin/**` — returns 403 → redirected to `/owner?denied=true`
- Access `/medicines/**`, `/sales/**`, `/customers/**`, `/returns/**`, `/suppliers/**`, `/purchases/**` directly
- No low-stock / expiry bell notification (not shown in header)

#### Subscription wall
If the owner's subscription plan is **expired**, every request is intercepted and redirected to `/subscription/billing` until the admin renews the plan.

---

## SHOPKEEPER

> Store operator. Works inside a single branch. Can sell, manage medicines, handle customers and returns for their assigned branch only.

### Sidebar sections visible to SHOPKEEPER
| Section         | Links                                              |
|-----------------|----------------------------------------------------|
| Dashboard       | `/` — branch-scoped KPI dashboard                 |
| Inventory       | Medicines, Purchases, Suppliers                    |
| Sales & Billing | New Sale, Sales History, Returns, Customers        |
| Reports         | Standard Reports, Advanced Analytics, GST / Tax   |

### What SHOPKEEPER can do

#### Dashboard
- `/` — shows KPIs **only for their assigned branch**: medicines count, customers, low-stock, expiry windows, today's sales trend.

#### Inventory (branch-scoped)
| Area | Capabilities |
|------|-------------|
| Medicines `/medicines/**` | View, add, edit medicines in their branch; low-stock alerts; expiry alerts; bulk delete (SHOPKEEPER sees only their branch's medicines) |
| Purchases `/purchases/**` | Create/edit/receive/cancel purchase orders for their branch |
| Suppliers `/suppliers/**` | View, add, edit, delete suppliers |

#### Sales & Billing (branch-scoped)
| Area | Capabilities |
|------|-------------|
| New Sale `/sales/new` | POS-style billing with barcode/search, discount, GST |
| Sales History `/sales` | View all sales for their branch |
| Sale Detail `/sales/{id}` | View, print invoice for a sale |
| Returns `/returns/**` | Create and manage product returns |
| Customers `/customers/**` | View, add, edit customers; purchase history |

#### Reports & Analytics (branch-scoped)
Same report URLs as ADMIN/OWNER, but all queries are **filtered to the shopkeeper's branch**.

#### Notifications
- Bell icon shows **low-stock count** and **expiring-soon count** for their branch only.

#### What SHOPKEEPER **cannot** do
- Access `/admin/**` — 403
- Access `/owner/**` — 403
- See other branches' data

#### Subscription wall
If the **owner's** subscription is expired, the shopkeeper is also blocked and redirected to `/subscription/billing`.

---

## URL Access Matrix

| URL Pattern             | ADMIN | OWNER | SHOPKEEPER |
|-------------------------|:-----:|:-----:|:----------:|
| `/admin/**`             | ✅    | ❌    | ❌         |
| `/owner/**`             | ❌    | ✅    | ❌         |
| `/`  (Dashboard)        | ✅    | ❌    | ✅         |
| `/medicines/**`         | ✅    | ❌    | ✅         |
| `/sales/**`             | ✅    | ❌    | ✅         |
| `/customers/**`         | ✅    | ❌    | ✅         |
| `/returns/**`           | ✅    | ❌    | ✅         |
| `/suppliers/**`         | ✅    | ❌    | ✅         |
| `/purchases/**`         | ✅    | ❌    | ✅         |
| `/reports/**`           | ✅    | ✅    | ✅         |
| `/analytics/**`         | ✅    | ✅    | ✅         |
| `/subscription/billing` | ✅    | ✅    | ✅ (public)|

---

## Data Scoping

Even when two roles share the same URL, **what they see differs**:

| Role        | Data scope enforced by |
|-------------|------------------------|
| ADMIN       | No filter — sees all data |
| OWNER       | `TenantContext.ownerId` — all branches belonging to this owner |
| SHOPKEEPER  | `TenantContext.tenantId` (branch ID) — their single branch only |

The `TenantFilter` sets these context values on every request from the authenticated user's profile. All service queries (`MedicineService`, `SaleService`, etc.) check the context and add the appropriate WHERE clause automatically.
