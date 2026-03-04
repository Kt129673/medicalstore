# MediStore — Shopkeeper User Guide

> **Role:** Shopkeeper (SHOPKEEPER)  
> **Audience:** Store operators who handle day-to-day sales, inventory, and customer management for a single branch  
> **Login URL:** `http://localhost:8080/login`

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Dashboard](#2-dashboard)
3. [Inventory Management](#3-inventory-management)
   - [Medicines](#31-medicines)
   - [Purchases](#32-purchases)
   - [Suppliers](#33-suppliers)
4. [Sales & Billing](#4-sales--billing)
   - [Creating a New Sale](#41-creating-a-new-sale)
   - [Sales History](#42-sales-history)
   - [Processing a Return](#43-processing-a-return)
5. [Customers](#5-customers)
6. [Reports & Analytics](#6-reports--analytics)
7. [Notifications](#7-notifications)
8. [Profile & Password](#8-profile--password)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Getting Started

### 1.1 Logging In

1. Open a browser and go to `http://localhost:8080/login`.
2. Enter your **username** and **password** (provided by your Owner or Administrator).
3. Click **Log In**.
4. After a successful login you are automatically redirected to your **Branch Dashboard** at `/`.

### 1.2 Logging Out

Click **Logout** in the top-right corner of the navigation bar. Always log out when leaving the workstation.

### 1.3 Your Branch Scope

As a Shopkeeper you work within a **single branch**. All data you create or view (medicines, sales, customers, returns) belongs exclusively to your assigned branch. You cannot see data from other branches.

---

## 2. Dashboard

**URL:** `/`

The Dashboard gives you a real-time snapshot of your branch.

### 2.1 Summary Cards

| Card | What it shows |
|---|---|
| Total Medicines | Number of medicines currently stocked in your branch |
| Total Customers | Number of registered customers for your branch |
| Today's Sales | Revenue generated today for your branch |
| Low Stock | Number of medicines below their minimum threshold |

### 2.2 Quick Action Links

Use the quick links on the dashboard for the most common tasks:
- **New Sale** → go directly to the POS billing screen
- **Add Medicine** → add a new medicine to inventory
- **Add Customer** → register a new customer
- **View Reports** → open the reports section

### 2.3 Sales Trend Chart

The dashboard displays a sales trend graph for the current week so you can spot busy and slow days at a glance.

---

## 3. Inventory Management

### 3.1 Medicines

**URL:** `/medicines`

#### Viewing Medicines

Navigate to **Inventory → Medicines**. The table shows all medicines in your branch with their category, stock level, expiry date, and price.

- **Red rows** — stock is below the minimum threshold (low stock alert).
- **Orange rows** — medicine expires within the next 30 days.

#### Adding a New Medicine

1. Click **Add Medicine**.
2. Fill in the medicine details:

   | Field | Description |
   |---|---|
   | Name | Medicine / product name |
   | Category | E.g., Tablet, Syrup, Injection, Ointment |
   | Manufacturer | Name of the manufacturer |
   | Batch Number | Batch or lot number from the packaging |
   | Expiry Date | Expiry date printed on the packaging (YYYY-MM-DD) |
   | Purchase Price | Cost per unit you paid the supplier |
   | Selling Price | Price charged to the customer |
   | Quantity | Initial stock quantity to enter |
   | Low Stock Threshold | Minimum quantity before a low-stock alert is raised |
   | HSN / SAC Code | GST classification code (check the packaging or supplier invoice) |
   | GST % | Applicable GST rate (e.g., 5, 12, 18) |

3. Click **Save**.

#### Editing a Medicine

Click the **Edit** (pencil) icon on the medicine row, update the fields, and click **Save**.

#### Deleting Medicines

Select one or more medicine rows using the checkboxes and click **Delete**. Confirm when prompted.

> **Warning:** Deleting a medicine is permanent. Only delete medicines that were added by mistake. Use the **Quantity** field to manage stock instead.

#### Searching for Medicines

Type a medicine name, category, or batch number in the **Search** bar at the top of the list. The table filters live as you type.

---

### 3.2 Purchases

**URL:** `/purchases`

Use Purchases to record stock received from suppliers and to raise purchase orders.

#### Creating a Purchase Order

1. Navigate to **Inventory → Purchases**.
2. Click **New Purchase**.
3. Select the **Supplier** from the dropdown.
4. Add medicines to the order:
   - Search for the medicine by name.
   - Enter the **Quantity Ordered** and **Purchase Price**.
   - Click **Add Item**.
5. Review the order total.
6. Click **Save** to create the order with status **Pending**.

#### Receiving a Purchase

When the stock physically arrives:
1. Find the purchase order in the list (status: **Pending**).
2. Click **Receive**.
3. Confirm the quantities received.
4. Click **Confirm Receipt**. Stock levels are updated automatically.

#### Editing a Pending Purchase

Click **Edit** on a **Pending** order to change quantities or items before stock arrives.

#### Cancelling a Purchase

Click **Cancel** on a **Pending** order if the delivery will not be fulfilled.

---

### 3.3 Suppliers

**URL:** `/suppliers`

#### Viewing Suppliers

Navigate to **Inventory → Suppliers** to see all your branch's suppliers.

#### Adding a Supplier

1. Click **Add Supplier**.
2. Fill in the form:

   | Field | Description |
   |---|---|
   | Company Name | Supplier / distributor name |
   | Contact Person | Name of your main contact at the supplier |
   | Phone | Contact phone number |
   | Email | Contact email address |
   | Address | Supplier's full address |
   | GST Number | Supplier's GSTIN (required for tax records) |

3. Click **Save**.

#### Editing / Deleting a Supplier

- **Edit:** Click the **Edit** icon on the supplier row.
- **Delete:** Click the **Delete** icon. A supplier can only be deleted if they have no pending purchase orders linked to them.

---

## 4. Sales & Billing

### 4.1 Creating a New Sale

**URL:** `/sales/new`

The New Sale screen is your Point-of-Sale (POS) interface.

#### Step-by-Step Billing Process

1. Navigate to **Sales & Billing → New Sale**.
2. **Select or Create Customer:**
   - Type the customer's name or phone in the search box to find an existing customer.
   - If it is a new customer, click **+ New Customer** and fill in their details inline.
3. **Add Medicines:**
   - In the **Add Item** section, type the medicine name or scan the barcode.
   - Select the medicine from the autocomplete suggestions.
   - Enter the **Quantity**.
   - Apply a **Discount (%)** for this line item if applicable.
   - Click **Add to Cart**.
   - Repeat for each medicine.
4. **Review the Cart:**
   - The right panel shows the running total with GST and any discounts applied.
   - To remove an item, click the **✕** on the item row in the cart.
5. **Apply an Overall Discount** (optional): Enter a percentage or flat amount in the **Overall Discount** field.
6. **Select Payment Method:** Choose **Cash**, **Card**, or **UPI**.
7. Click **Complete Sale**.
8. A **printable invoice** is generated. Click **Print Invoice** to print or save as PDF.

> **Tip:** The live calculator on the right updates as you add items — always check the total before confirming the sale.

#### Important Notes

- You cannot sell a medicine with **zero quantity** — stock is validated at the time of sale.
- Selling a medicine reduces its stock count automatically.
- The GST rate is taken from the medicine's configured GST% field.

---

### 4.2 Sales History

**URL:** `/sales`

1. Navigate to **Sales & Billing → Sales History**.
2. Use the **date range filters** to narrow the list.
3. Click on any sale row to view the full **Sale Detail** page.
4. From the detail page, click **Print Invoice** to reprint the invoice.

---

### 4.3 Processing a Return

**URL:** `/returns`

Use this when a customer returns medicines.

1. Navigate to **Sales & Billing → Returns**.
2. Click **New Return**.
3. Enter the original **Sale ID** or search by the customer's name.
4. Select the items being returned.
5. Enter the **Quantity Returned** for each item.
6. Select a **Return Reason** (e.g., wrong medicine, damaged, expired).
7. Click **Submit Return**.

Stock levels are updated automatically when a return is processed.

---

## 5. Customers

**URL:** `/customers`

### 5.1 Viewing Customers

Navigate to **Sales & Billing → Customers**. The directory lists all customers registered to your branch.

### 5.2 Adding a New Customer

1. Click **Add Customer**.
2. Fill in the form:

   | Field | Description |
   |---|---|
   | Full Name | Customer's full name |
   | Phone | Mobile / phone number |
   | Email | Email address (optional) |
   | Address | Delivery or home address (optional) |
   | Date of Birth | Used for birthday reminders (optional) |

3. Click **Save**.

### 5.3 Viewing a Customer's Purchase History

Click on the customer's name to open their profile. The profile shows:
- Contact details
- Complete purchase history with dates and amounts
- Total lifetime spend

### 5.4 Editing a Customer

Click the **Edit** icon on the customer row, update the details, and click **Save**.

---

## 6. Reports & Analytics

All reports are automatically **scoped to your branch** — you will only see data from your own branch.

### 6.1 Standard Reports

**URL:** `/reports`

| Report | URL | Description |
|---|---|---|
| Daily Report | `/reports/daily` | All sales and purchases for today |
| Monthly Report | `/reports/monthly` | Month-to-date summary |
| Custom Date Report | `/reports/custom` | Choose any start and end date |
| Expiry Report | `/reports/expiry` | Medicines expiring soon in your branch |
| Profit & Loss | `/reports/profit-loss` | Revenue vs. cost for your branch |
| GST Report | `/reports/gst` | GST collected from your sales |
| Export to Excel | `/reports/export/excel` | Download the current report as `.xlsx` |

#### How to Generate a Report

1. Navigate to **Reports** in the sidebar.
2. Choose the report from the sub-menu.
3. Set the **date range** if the report requires it.
4. Click **Generate**.
5. Review the on-screen results.
6. Click **Export to Excel** to download a spreadsheet.

### 6.2 Advanced Analytics

**URL:** `/analytics`

| Report | URL | Description |
|---|---|---|
| Analytics Hub | `/analytics` | Visual charts for your branch |
| Profit per Medicine | `/analytics/profit-per-medicine` | Margin analysis by product |
| Dead Stock | `/analytics/dead-stock` | Medicines with no sales in the last N days |
| Fast Moving | `/analytics/fast-moving` | Your best-selling medicines |
| GST Summary | `/analytics/gst-summary` | GST by rate slab |
| Export to Excel | `/analytics/export/excel` | Download analytics data |

---

## 7. Notifications

The **bell icon** in the top navigation bar shows active alerts for your branch:

| Alert | What it means | Action to take |
|---|---|---|
| Low Stock | One or more medicines are below their minimum threshold | Order more stock (create a Purchase Order) |
| Expiring Soon | One or more medicines expire within 30 days | Plan promotions or returns to supplier |

Click the bell icon to see a dropdown list with details and quick links to the affected medicines.

---

## 8. Profile & Password

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

> **Security Tip:** Change your password on your first login and never share it with anyone.

---

## 9. Troubleshooting

| Problem | Solution |
|---|---|
| Cannot log in | Verify your username and password. Contact your Owner or Administrator to reset your password if needed. |
| Redirected to Subscription & Billing | Your Owner's subscription has expired. Ask your Owner to contact the Administrator. |
| Cannot process a sale — medicine shows zero stock | Receive a new purchase order to replenish stock first. |
| Cannot see a customer | The customer may belong to a different branch. Use the **Add Customer** button to create a new record. |
| Invoice does not print | Check that your browser allows pop-ups for this site. Try a different browser if the issue persists. |
| Cannot access `/admin` or `/owner` | These sections require higher-level roles. If you need access, contact your Owner or Administrator. |
| Page shows old data | Click the browser **Refresh** button (F5) or hard-refresh (Ctrl+Shift+R). |
| Reports show no data | Check that the selected date range contains transactions and that sales have been completed (not just started). |
| Cannot delete a supplier | The supplier has linked purchase orders. Cancel or complete those orders first. |

---

*Last Updated: March 2026 | MediStore Shopkeeper User Guide v1.0*
