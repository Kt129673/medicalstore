# MediStore — Admin User Guide

> **Role:** Administrator (ADMIN)  
> **Audience:** Platform administrators responsible for managing the entire MediStore system  
> **Login URL:** `http://localhost:8080/login`

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Admin Dashboard](#2-admin-dashboard)
3. [Managing Users](#3-managing-users)
4. [Managing Branches](#4-managing-branches)
5. [Managing Subscriptions](#5-managing-subscriptions)
6. [Inventory Management](#6-inventory-management)
7. [Sales & Billing](#7-sales--billing)
8. [Reports & Analytics](#8-reports--analytics)
9. [Audit Logs](#9-audit-logs)
10. [Notifications](#10-notifications)
11. [Profile & Password](#11-profile--password)
12. [Troubleshooting](#12-troubleshooting)

---

## 1. Getting Started

### 1.1 Logging In

1. Open a browser and navigate to `http://localhost:8080/login`.
2. Enter your **username** and **password**.
3. Click **Log In**.
4. After a successful login you are automatically redirected to the **Admin Panel** at `/admin`.

### 1.2 Logging Out

Click **Logout** in the top-right corner of the navigation bar at any time.

### 1.3 What You Can Do as Admin

As the platform Administrator you have the highest level of access:

| Capability | Description |
|---|---|
| Manage all users | Create, enable/disable, and delete Owner and Shopkeeper accounts |
| Manage all branches | Create branches, assign them to Owners, activate/deactivate |
| Manage subscriptions | Set plans (FREE / PRO / ENTERPRISE), expiry dates and limits |
| View all operational data | Medicines, sales, customers, returns, suppliers, purchases — across every branch |
| Generate global reports | Reports and analytics are not filtered by branch or owner |
| Low-stock & expiry notifications | Notifications cover every branch in the system |

---

## 2. Admin Dashboard

**URL:** `/admin`

The Admin Dashboard gives a platform-wide summary.

### 2.1 Summary Cards

| Card | What it shows |
|---|---|
| Total Users | Number of active user accounts across all roles |
| Total Branches | Number of branches registered in the system |
| Total Owners | Number of Owner accounts |
| Recent Activity | Quick overview of the latest system events |

### 2.2 Quick Links

The dashboard contains shortcut buttons to the most frequently used admin functions:
- **Manage Users** → `/admin/users`
- **Manage Branches** → `/admin/branches`
- **Subscriptions** → `/admin/subscriptions`

---

## 3. Managing Users

**URL:** `/admin/users`

### 3.1 Viewing All Users

Navigate to **Admin Panel → Manage Users**. A table lists every registered user with their username, role, status, and last login time.

### 3.2 Creating a New User

1. Click **Create User** (top-right of the users table).
2. Fill in the form:

   | Field | Description |
   |---|---|
   | Username | Unique login name for the user |
   | Password | Initial password (the user should change it after first login) |
   | Full Name | Display name |
   | Email | Contact email address |
   | Role | `OWNER` or `SHOPKEEPER` |
   | Branch (Shopkeeper only) | The branch to assign the shopkeeper to |

3. Click **Save**.

> **Note:** Admin accounts can only be created directly in the database for security reasons.

### 3.3 Enabling / Disabling a User

- In the user list, click the **Toggle** button next to a user.
- A disabled user cannot log in until re-enabled.
- You **cannot** disable your own account.

### 3.4 Deleting a User

1. Click **Delete** next to the user you want to remove.
2. Confirm the deletion in the dialog box.
3. The user account is **soft-deleted** — it is hidden from normal views but not permanently removed from the database.

> **Warning:** You cannot delete your own account. You also cannot delete the last remaining Administrator account — create another admin first if you need to remove the current one.

### 3.5 Restoring a Deleted User

Soft-deleted users can be recovered at any time:

1. Navigate to **Admin Panel → Manage Users → Deleted Users** (or visit `/admin/users/deleted`).
2. Find the user you want to restore.
3. Click **Restore** next to the user.
4. The account is immediately re-activated and the user can log in again.

---

## 4. Managing Branches

**URL:** `/admin/branches`

### 4.1 Viewing All Branches

Navigate to **Admin Panel → Manage Branches**. The table shows every branch along with the assigned owner, status, and creation date.

### 4.2 Creating a Branch

1. Click **Create Branch**.
2. Fill in the form:

   | Field | Description |
   |---|---|
   | Branch Name | A recognizable name for the branch |
   | Location / Address | Physical address of the branch |
   | Owner | Select the Owner this branch belongs to |

3. Click **Save**. The new branch is immediately active.

### 4.3 Activating / Deactivating a Branch

Click **Toggle** next to a branch to switch it between active and inactive states.

- An **inactive** branch cannot be accessed by its assigned shopkeepers.
- The branch data is preserved and can be reactivated at any time.

---

## 5. Managing Subscriptions

**URL:** `/admin/subscriptions`

Every Owner has a subscription plan that governs how many users and branches they may have.

### 5.1 Viewing Subscription Status

The subscriptions page lists every Owner with their current plan, expiry date, user limit, and branch limit.

### 5.2 Updating a Subscription

1. Click **Update** next to an Owner.
2. Edit the subscription details:

   | Field | Options / Description |
   |---|---|
   | Plan Type | `FREE`, `PRO`, or `ENTERPRISE` |
   | Expiry Date | Date the subscription expires (YYYY-MM-DD) |
   | Max Users | Maximum number of user accounts for this owner |
   | Max Branches | Maximum number of branches for this owner |

3. Click **Save**.

> **Tip:** When an owner's subscription expires, their shopkeepers are also blocked until you renew the plan.

---

## 6. Inventory Management

As Admin you can view and manage inventory data across **all** branches. The data is not filtered by branch.

### 6.1 Medicines

**URL:** `/medicines`

| Action | How |
|---|---|
| View all medicines | Navigate to **Inventory → Medicines** |
| Add a medicine | Click **Add Medicine**, fill in the form and click **Save** |
| Edit a medicine | Click the **Edit** (pencil) icon next to the medicine |
| Delete medicines | Select one or more rows and click **Delete** |
| Search | Type in the search bar at the top of the list |
| Low-stock alert | Medicines with stock below the threshold are highlighted in red |
| Expiry alert | Medicines expiring within 30 days are highlighted in orange |

#### Medicine Form Fields

| Field | Description |
|---|---|
| Name | Medicine / product name |
| Category | E.g., Tablet, Syrup, Injection |
| Manufacturer | Manufacturer name |
| Batch Number | Batch or lot number |
| Expiry Date | Expiry date (YYYY-MM-DD) |
| Purchase Price | Cost per unit |
| Selling Price | Retail price per unit |
| Quantity | Current stock level |
| Low Stock Threshold | Minimum quantity before a low-stock alert is raised |
| HSN / SAC Code | GST classification code |
| GST % | Applicable GST rate |

### 6.2 Purchases

**URL:** `/purchases`

| Action | How |
|---|---|
| View all purchases | Navigate to **Inventory → Purchases** |
| Create a purchase order | Click **New Purchase** and select a supplier and medicines |
| Edit a purchase | Click **Edit** on a pending purchase order |
| Mark as received | Click **Receive** to update stock automatically |
| Cancel a purchase | Click **Cancel** on an open order |

### 6.3 Suppliers

**URL:** `/suppliers`

| Action | How |
|---|---|
| View all suppliers | Navigate to **Inventory → Suppliers** |
| Add a supplier | Click **Add Supplier**, fill in the form and save |
| Edit a supplier | Click the **Edit** icon |
| Delete a supplier | Click the **Delete** icon (only allowed if no pending purchases) |

---

## 7. Sales & Billing

**URL:** `/sales`

As Admin you can view all sales from every branch, create new sales on behalf of any branch, and handle returns.

### 7.1 Creating a New Sale

1. Navigate to **Sales & Billing → New Sale**.
2. Select or search for the **Customer** (or create a new one inline).
3. Add line items by searching for medicines using the search box.
4. For each item:
   - Verify the quantity.
   - Apply a line-item discount if needed.
5. The **Total** panel on the right updates live.
6. Select the **Payment Method** (Cash / Card / UPI).
7. Click **Complete Sale**. An invoice is generated automatically.

### 7.2 Viewing Sales History

- Navigate to **Sales & Billing → Sales History**.
- Use the date filters to narrow down the list.
- Click a sale to view its full detail / invoice.
- Click **Print Invoice** to open a printable PDF.

### 7.3 Processing a Return

1. Navigate to **Sales & Billing → Returns → New Return**.
2. Enter the original **Sale ID** or search by customer.
3. Select the items being returned and enter the reason.
4. Click **Submit Return**. Stock is added back automatically.

### 7.4 Managing Customers

**URL:** `/customers`

| Action | How |
|---|---|
| View all customers | Navigate to **Sales & Billing → Customers** |
| Add a customer | Click **Add Customer**, fill in the form and save |
| Edit a customer | Click the **Edit** icon |
| View purchase history | Click on a customer's name to view their full history |

---

## 8. Reports & Analytics

### 8.1 Standard Reports

**URL:** `/reports`

| Report | URL | Description |
|---|---|---|
| Daily Report | `/reports/daily` | Sales and purchases for today |
| Monthly Report | `/reports/monthly` | Month-to-date summary |
| Custom Date Report | `/reports/custom` | Select any date range |
| Expiry Report | `/reports/expiry` | Medicines expiring within a configurable window |
| Profit & Loss | `/reports/profit-loss` | Revenue vs. cost analysis |
| GST Report | `/reports/gst` | GST collected and input tax credit |
| Export to Excel | `/reports/export/excel` | Download the current report as `.xlsx` |

### 8.2 Advanced Analytics

**URL:** `/analytics`

| Report | URL | Description |
|---|---|---|
| Analytics Hub | `/analytics` | Overview dashboard with charts |
| Profit per Medicine | `/analytics/profit-per-medicine` | Margin analysis by product |
| Dead Stock | `/analytics/dead-stock` | Medicines with no sales in the last N days |
| Fast Moving | `/analytics/fast-moving` | Top-selling medicines |
| GST Summary | `/analytics/gst-summary` | GST breakdown by rate slab |
| Export to Excel | `/analytics/export/excel` | Download analytics data as `.xlsx` |

> **Tip (Admin):** All reports and analytics show **global data** — no branch or owner filter is applied.

---

## 9. Audit Logs

**URL:** `/admin/audit-logs`

The Audit Logs page records every significant action performed by any user in the system — giving administrators a full tamper-evident trail of who did what and when.

### 9.1 Viewing Audit Logs

Navigate to **Admin Panel → Audit Logs**. The log table shows:

| Column | Description |
|---|---|
| Timestamp | Date and time the action occurred |
| User | Username of the person who performed the action |
| Action | Action code (e.g., `USER_CREATED`, `SALE_COMPLETED`, `PASSWORD_RESET`) |
| Description | Human-readable description of what happened |

Logs are displayed in reverse-chronological order (newest first), 50 per page.

### 9.2 Filtering Audit Logs

Use the filter bar at the top to narrow the results:

| Filter | Description |
|---|---|
| Username | Filter by the user who performed the action |
| Action | Filter by action code keyword |
| From Date | Start of date range (YYYY-MM-DD) |
| To Date | End of date range (YYYY-MM-DD) |

Click **Apply Filters** to refresh the results.

### 9.3 What Gets Logged

| Action Code | When it is recorded |
|---|---|
| `USER_CREATED` | A new user is created |
| `USER_DELETED` | A user account is soft-deleted |
| `USER_RESTORED` | A soft-deleted user is restored |
| `PASSWORD_RESET` | An admin resets a user's password |
| `ADMIN_VIEW_AS_OWNER` | An admin uses the "View as Owner" feature |
| `SALE_COMPLETED` | A sale transaction is completed |
| `ESCALATION_ATTEMPT` | A user tries to access a resource beyond their role |

> **Tip:** Use audit logs to investigate suspicious activity or to verify that an operation was performed correctly. All actions are immutable — they cannot be edited or deleted through the UI.

---

## 10. Notifications

The **bell icon** in the top navigation bar shows real-time alert counts:

| Alert | Description |
|---|---|
| Low Stock | Number of medicines across all branches currently below their threshold |
| Expiring Soon | Number of medicines expiring within the next 30 days across all branches |

Click the bell icon to see a dropdown list of the alerts with quick links to the affected medicines.

---

## 11. Profile & Password

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

## 12. Troubleshooting

| Problem | Solution |
|---|---|
| Cannot log in | Verify username and password. Ask another admin to reset your password if needed. |
| Page shows 403 Forbidden | You may have navigated to a URL you don't have access to. Return to `/admin`. |
| User account not appearing | Refresh the page. The user may have been soft-deleted — check `/admin/users/deleted`. |
| Cannot delete a user | You cannot delete your own account or the last Administrator account. |
| Subscription changes not applying | Verify the expiry date is in the future. Check the owner's username. |
| Excel export is empty | Make sure there is data in the selected date range before exporting. |
| Low-stock alerts missing | Verify the medicine's **Low Stock Threshold** is set correctly. |
| Build or startup errors | Run `mvn clean install` and restart with `mvn spring-boot:run`. |

---

*Last Updated: March 2026 | MediStore Admin User Guide v1.0*
