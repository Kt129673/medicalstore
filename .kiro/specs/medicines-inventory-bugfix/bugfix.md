# Bugfix Requirements Document

## Introduction

The Medicines Inventory module has five bugs causing broken UI rendering, silent functional failures, and potential runtime errors. The most visible symptom — confirmed by screenshot — is the inventory table rendering without any styling, causing column headers to appear merged (e.g., "PriceStockBatch") and the layout to collapse. A separate critical bug causes the delete button on the Expiry Alerts page to always produce a 404. Two additional bugs affect the dashboard expired-medicines card (never shown) and the low-stock page (potential NullPointerException when a medicine has no supplier).

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN a user visits the Medicines Inventory page (`/medicines`) THEN the system renders the table without any visual styling because the CSS classes used by `list.html` (`.inv-table`, `.inv-filter-bar`, `.inv-table-card`, `.inv-table-header`, `.inv-table-wrapper`, `.inv-pagination`, `.inv-empty`, `.inv-row`, `.cat-tag`, `.stock-pill`, `.expiry-badge`) are not defined in any CSS file, causing columns to appear merged and the layout to break.

1.2 WHEN a user with ADMIN role clicks the "Remove" button for an expired medicine on the Expiry Alerts page (`/medicines/expiry-alerts`) THEN the system submits a POST to `/medicines/delete/${m.id}` where `${m.id}` is unresolved (the loop variable is `medicine`, not `m`), resulting in a 404 error and the medicine not being deleted.

1.3 WHEN the Medicines Inventory page (`/medicines`) finishes loading THEN the system calls `entInitTableSort('inventoryTable')` which is defined in `layout.js` but the function call occurs before `layout.js` is loaded by the page layout, causing a `ReferenceError` or silent failure and disabling column sort functionality.

1.4 WHEN the dashboard KPI API (`/api/v1/dashboard/kpis`) is called for a Shopkeeper user THEN the system returns a map that does not include the `expiredCount` key (only the Admin dashboard path sets this field), so the expired medicines card on the dashboard (`#cardExpired`) is never shown for Shopkeeper users.

1.5 WHEN a user visits the Low Stock page (`/medicines/low-stock`) and a medicine in the list has a null `supplier` field THEN the system accesses `${medicine.supplier.name}` without null-guarding, causing a Thymeleaf evaluation error or NullPointerException that breaks the page render.

### Expected Behavior (Correct)

2.1 WHEN a user visits the Medicines Inventory page (`/medicines`) THEN the system SHALL render the inventory table with correct column separation, styled filter bar, stock-level pills, category tags, and expiry badges as intended by the template design.

2.2 WHEN a user with ADMIN role clicks the "Remove" button for an expired medicine on the Expiry Alerts page THEN the system SHALL submit the delete form to `/medicines/delete/{medicine.id}` using the correct loop variable `medicine.id`, successfully deleting the record and redirecting back to the medicines list.

2.3 WHEN the Medicines Inventory page finishes loading THEN the system SHALL successfully invoke `entInitTableSort('inventoryTable')` after `layout.js` is available, enabling column sorting without errors.

2.4 WHEN the dashboard KPI API is called for any authenticated user role THEN the system SHALL include the `expiredCount` field in the response map for all dashboard scopes (Admin, Owner, and Shopkeeper), so the expired medicines card is shown whenever the count is greater than zero.

2.5 WHEN a user visits the Low Stock page and a medicine has a null `supplier` field THEN the system SHALL safely skip rendering the supplier name without throwing an error, keeping the page fully functional.

### Unchanged Behavior (Regression Prevention)

3.1 WHEN a user with ADMIN role deletes a medicine from the main Medicines Inventory list (`/medicines`) THEN the system SHALL CONTINUE TO submit the delete form correctly using `medicine.id` and redirect to the medicines list on success.

3.2 WHEN a user visits the Expiry Alerts page and clicks "Update" on a medicine in the "Expiring Within 30 Days" section THEN the system SHALL CONTINUE TO navigate to the correct edit page using `medicine.id`.

3.3 WHEN a user visits the Low Stock page and a medicine has a non-null `supplier` THEN the system SHALL CONTINUE TO display the supplier name beneath the medicine name.

3.4 WHEN the dashboard KPI API is called and there are zero expired medicines THEN the system SHALL CONTINUE TO hide the expired medicines card (`#cardExpired`) on the dashboard.

3.5 WHEN a user visits the Medicines Inventory page and the medicine list is empty THEN the system SHALL CONTINUE TO display the empty-state prompt to add the first medicine.

3.6 WHEN a user applies filters (search, category, stock level, expiry range) on the Medicines Inventory page THEN the system SHALL CONTINUE TO filter and paginate results correctly regardless of CSS or JS changes.
