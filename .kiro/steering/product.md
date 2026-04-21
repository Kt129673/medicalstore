# MedicalStore — Product Overview

A multi-branch pharmacy management system for end-to-end store operations. Targets three user roles: **Admin** (platform governance), **Owner** (multi-branch oversight), and **Shopkeeper** (daily store operations).

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
ADMIN  →  full platform control (users, branches, subscriptions, audit logs)
OWNER  →  multi-branch analytics, reports, subscription management
SHOPKEEPER  →  medicines, sales, purchases, customers, returns
```

Access is enforced at two levels: URL security (`SecurityConfig`) and runtime feature flags (`FeatureFlags`).
