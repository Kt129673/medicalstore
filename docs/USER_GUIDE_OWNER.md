# MediStore — Owner User Guide

> **Role:** Owner (OWNER)  
> **Audience:** Business owners who manage one or more medical store branches  
> **Login URL:** `http://localhost:8080/login`

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Owner Dashboard](#2-owner-dashboard)
3. [Managing Your Branches](#3-managing-your-branches)
4. [Managing Shopkeepers](#4-managing-shopkeepers)
5. [Reports & Analytics](#5-reports--analytics)
6. [Subscription & Billing](#6-subscription--billing)
7. [Profile & Password](#7-profile--password)
8. [Troubleshooting](#8-troubleshooting)

---

## 1. Getting Started

### 1.1 Logging In

1. Open a browser and go to `http://localhost:8080/login`.
2. Enter your **username** and **password** (provided by your Administrator).
3. Click **Log In**.
4. After a successful login you are automatically redirected to the **Owner Dashboard** at `/owner`.

### 1.2 Logging Out

Click **Logout** in the top-right corner of the navigation bar.

### 1.3 What You Can Do as Owner

As a business Owner you manage your **own portfolio** of branches and the shopkeepers who work in them.

| Capability | Description |
|---|---|
| View portfolio dashboard | Aggregated KPIs for all your branches in one place |
| Drill into branch details | Medicines, recent sales, and assigned shopkeepers for each branch |
| Manage shopkeepers | Add, enable/disable shopkeeper accounts for your branches |
| View reports & analytics | Reports scoped to your branches only |
| Manage your subscription | Upgrade or review your subscription plan |

> **Note:** Day-to-day operations (selling, stock updates, customer management) are handled by your Shopkeepers. As an Owner you have a read-only, aggregated view of operational data.

---

## 2. Owner Dashboard

**URL:** `/owner`

The Owner Dashboard gives you a bird's-eye view of your entire business portfolio.

### 2.1 Summary Cards

| Card | What it shows |
|---|---|
| Total Medicines | Combined medicine count across all your branches |
| Total Customers | Total unique customers across all your branches |
| Today's Sales | Revenue generated today across all your branches |
| Low Stock Alerts | Medicines that have dropped below their threshold |

### 2.2 Branch Overview List

Below the summary cards is a list of all your branches. Each row shows:
- Branch name and location
- Number of medicines and customers
- Status (Active / Inactive)
- A **View Details** button

Click **View Details** to drill into a specific branch.

---

## 3. Managing Your Branches

### 3.1 Viewing Branch Details

**URL:** `/owner/branches/{id}`

Click **View Details** on any branch from the Owner Dashboard to open the branch detail page. You will see:

| Section | Information |
|---|---|
| Branch Info | Name, location, creation date, status |
| Medicines | List of medicines stocked in this branch with quantities and expiry dates |
| Recent Sales | The latest sales transactions for this branch |
| Assigned Shopkeepers | Users currently assigned to operate this branch |

> **Note:** You cannot create or delete branches yourself. Contact your Administrator to add new branches or change branch assignments.

### 3.2 Understanding Branch Status

| Status | Meaning |
|---|---|
| Active | The branch is operational; shopkeepers can log in and process sales |
| Inactive | The branch is disabled; shopkeepers are blocked from accessing it |

---

## 4. Managing Shopkeepers

**URL:** `/owner/shopkeepers`

### 4.1 Viewing Your Shopkeepers

Navigate to **My Portfolio → Shopkeepers**. The table shows every shopkeeper assigned to one of your branches, along with their assigned branch, status, and last login time.

### 4.2 Adding a New Shopkeeper

1. Click **Create Shopkeeper** (top-right of the shopkeepers table).
2. Fill in the form:

   | Field | Description |
   |---|---|
   | Username | Unique login name for the shopkeeper |
   | Password | Initial password (ask the shopkeeper to change it on first login) |
   | Full Name | Display name |
   | Email | Contact email address |
   | Branch | Select one of your active branches to assign the shopkeeper to |

3. Click **Save**. The shopkeeper can now log in immediately.

### 4.3 Enabling / Disabling a Shopkeeper

- In the shopkeeper list, click the **Toggle** button next to the shopkeeper.
- A **disabled** shopkeeper cannot log in until re-enabled.
- Use this when a staff member is on leave or has left the business.

> **Note:** You can only manage shopkeepers assigned to your own branches. If you need to reassign a shopkeeper to a different owner's branch, contact the Administrator.

---

## 5. Reports & Analytics

All reports and analytics are automatically **scoped to your branches only** — you will never see data from another owner's branches.

### 5.1 Standard Reports

**URL:** `/reports`

| Report | URL | Description |
|---|---|---|
| Daily Report | `/reports/daily` | Combined sales and purchases across all your branches for today |
| Monthly Report | `/reports/monthly` | Month-to-date summary across your branches |
| Custom Date Report | `/reports/custom` | Choose any start and end date |
| Expiry Report | `/reports/expiry` | Medicines expiring within a configurable window across your branches |
| Profit & Loss | `/reports/profit-loss` | Revenue vs. cost analysis across your branches |
| GST Report | `/reports/gst` | GST collected and input tax credit summary |
| Export to Excel | `/reports/export/excel` | Download the current report as `.xlsx` |

#### How to Generate a Report

1. Navigate to **Reports** in the sidebar.
2. Select the report type from the sub-menu.
3. Set the **date range** if required.
4. Click **Generate** (or **Apply Filters**).
5. Review the on-screen results.
6. Click **Export to Excel** to download a spreadsheet copy.

### 5.2 Advanced Analytics

**URL:** `/analytics`

| Report | URL | Description |
|---|---|---|
| Analytics Hub | `/analytics` | Visual charts and KPI overview |
| Profit per Medicine | `/analytics/profit-per-medicine` | Margin per product across your branches |
| Dead Stock | `/analytics/dead-stock` | Medicines with no sales in the last N days |
| Fast Moving | `/analytics/fast-moving` | Top-selling medicines across your branches |
| GST Summary | `/analytics/gst-summary` | GST breakdown by rate slab |
| Export to Excel | `/analytics/export/excel` | Download analytics data as `.xlsx` |

---

## 6. Subscription & Billing

**URL:** `/subscription/billing`

Your subscription plan determines the number of users and branches you are allowed to have.

### 6.1 Viewing Your Current Plan

Navigate to **Subscription & Billing** in the sidebar (or visit `/subscription/billing`). You can see:
- Current plan name (FREE / PRO / ENTERPRISE)
- Plan expiry date
- Maximum users allowed
- Maximum branches allowed

### 6.2 Plan Limits

| Plan | Users | Branches | Analytics |
|---|---|---|---|
| FREE | Limited | Limited | Basic |
| PRO | More users | More branches | Full |
| ENTERPRISE | Unlimited | Unlimited | Full + Priority Support |

### 6.3 What Happens When Your Subscription Expires

- Every request (for you **and** your shopkeepers) is automatically redirected to the `/subscription/billing` page.
- Operations are suspended until the Administrator renews your plan.
- Contact your Administrator and ask them to update your subscription.

> **Tip:** Set a reminder a week before your plan expiry date so there is no disruption to your operations.

---

## 7. Profile & Password

**URL:** `/profile`

1. Click your **username** in the top-right corner and select **Profile**.
2. You can update:
   - Full Name
   - Email address
3. To change your password:
   1. Click **Change Password**.
   2. Enter your **Current Password**.
   3. Enter and confirm your **New Password**.
   4. Click **Update Password**.

---

## 8. Troubleshooting

| Problem | Solution |
|---|---|
| Cannot log in | Verify your username and password. Ask the Administrator to reset your password if needed. |
| Redirected to Subscription & Billing | Your subscription has expired. Contact your Administrator to renew the plan. |
| Cannot see a branch | The branch may be inactive or assigned to a different owner. Contact the Administrator. |
| Shopkeeper cannot log in | Check that their account is **enabled** in the Shopkeepers list. Also verify the branch is active and the subscription is current. |
| Reports show no data | Verify the selected date range has transactions. Check that your branches have active shopkeepers entering data. |
| Cannot access `/admin` | Admin access requires the ADMIN role. You do not have permission to access that section. |
| Excel export is empty | Make sure there is data in the selected date range. |

---

*Last Updated: March 2026 | MediStore Owner User Guide v1.0*
