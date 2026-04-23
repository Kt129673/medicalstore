# Requirements Document

## Introduction

The Purchase Order Management feature provides a complete procurement workflow for the MedicalStore pharmacy management system. It enables SHOPKEEPER and ADMIN users to create purchase orders against registered suppliers, manage line items (medicines, quantities, prices), track orders through a defined status lifecycle, receive stock (partially or fully) with automatic medicine quantity updates, record supplier payments, and view PO history and reports. OWNER users have read-only access to cross-branch PO reports. The feature builds on the existing partial `PurchaseOrder`, `PurchaseOrderItem`, `PurchaseService`, and `SupplierCredit` infrastructure, completing and hardening it into a production-ready workflow.

## Glossary

- **PO / Purchase_Order**: A formal document issued to a supplier requesting delivery of medicines at agreed quantities and prices.
- **PO_Line_Item**: A single medicine entry within a Purchase Order, carrying ordered quantity, unit price, and received quantity.
- **PO_Status**: The lifecycle state of a Purchase Order — one of `DRAFT`, `SENT`, `PARTIALLY_RECEIVED`, `RECEIVED`, or `CANCELLED`.
- **Stock_Receipt**: The act of recording that goods from a PO have physically arrived, triggering medicine stock quantity updates.
- **Partial_Receipt**: A Stock_Receipt where the received quantity for one or more PO_Line_Items is less than the ordered quantity.
- **Supplier_Payment**: A monetary payment recorded against a supplier's outstanding credit balance for a specific PO.
- **Supplier_Credit**: An outstanding payable balance owed to a supplier, created when a PO is received and settled via Supplier_Payments.
- **PO_Report**: An aggregated view of Purchase_Orders filtered by date range, supplier, status, or branch.
- **TenantContext**: The thread-local branch context that scopes all data queries to the current branch.
- **Branch**: A physical pharmacy location; the unit of multi-tenancy in MedicalStore.
- **SHOPKEEPER**: A role with full operational access to PO creation, editing, receiving, and payment recording within their branch.
- **ADMIN**: A platform-level role with full access to all POs across all branches, including deletion.
- **OWNER**: A role with read-only access to cross-branch PO reports and analytics for their owned branches.
- **Order_Number**: A system-generated unique identifier for a Purchase_Order in the format `PO-YYYYMMDD-XXXXXX`.

---

## Requirements

### Requirement 1: Purchase Order Creation

**User Story:** As a SHOPKEEPER, I want to create a purchase order against a registered supplier with one or more medicine line items, so that I can formally track what I am ordering and at what price.

#### Acceptance Criteria

1. WHEN a SHOPKEEPER submits a new PO form with a valid supplier, at least one PO_Line_Item, and an order date, THE Purchase_Order_Service SHALL persist the Purchase_Order with status `DRAFT` and a system-generated Order_Number.
2. THE Purchase_Order_Service SHALL generate Order_Numbers in the format `PO-YYYYMMDD-XXXXXX` where `XXXXXX` is a 6-character alphanumeric suffix that is unique within the system.
3. WHEN a PO is created, THE Purchase_Order_Service SHALL automatically assign the Purchase_Order to the branch resolved from TenantContext.
4. THE Purchase_Order_Service SHALL calculate each PO_Line_Item's `totalPrice` as `quantity × unitPrice` and set the Purchase_Order's `totalAmount` as the sum of all PO_Line_Item `totalPrice` values.
5. IF a SHOPKEEPER submits a PO form with no PO_Line_Items, THEN THE PurchaseController SHALL return the form view with an error message stating "At least one line item is required."
6. IF a SHOPKEEPER submits a PO form with a `quantity` less than or equal to zero for any PO_Line_Item, THEN THE PurchaseController SHALL return the form view with an error message stating "Quantity must be greater than zero."
7. IF a SHOPKEEPER submits a PO form with a `unitPrice` less than or equal to zero for any PO_Line_Item, THEN THE PurchaseController SHALL return the form view with an error message stating "Unit price must be greater than zero."
8. WHEN a PO is saved, THE PurchaseController SHALL redirect to the PO detail view and display a success flash message containing the Order_Number.

---

### Requirement 2: Purchase Order Editing

**User Story:** As a SHOPKEEPER, I want to edit a purchase order that is still in DRAFT status, so that I can correct mistakes before sending it to the supplier.

#### Acceptance Criteria

1. WHEN a SHOPKEEPER requests the edit form for a Purchase_Order with status `DRAFT`, THE PurchaseController SHALL render the edit form pre-populated with the existing Purchase_Order data.
2. IF a SHOPKEEPER attempts to access the edit form for a Purchase_Order whose status is not `DRAFT`, THEN THE PurchaseController SHALL redirect to the PO detail view with an error message stating "Only DRAFT orders can be edited."
3. WHEN a SHOPKEEPER submits a valid edit form for a `DRAFT` Purchase_Order, THE Purchase_Order_Service SHALL update the Purchase_Order and recalculate `totalAmount`.
4. IF a SHOPKEEPER attempts to edit a Purchase_Order belonging to a different branch, THEN THE Purchase_Order_Service SHALL throw an `AccessDeniedException`.

---

### Requirement 3: Purchase Order Status Lifecycle

**User Story:** As a SHOPKEEPER, I want to advance a purchase order through its lifecycle (Draft → Sent → Received), so that I can track where each order stands in the procurement process.

#### Acceptance Criteria

1. THE Purchase_Order_Service SHALL enforce the following valid status transitions: `DRAFT` → `SENT`, `SENT` → `PARTIALLY_RECEIVED`, `SENT` → `RECEIVED`, `PARTIALLY_RECEIVED` → `RECEIVED`, `DRAFT` → `CANCELLED`, `SENT` → `CANCELLED`.
2. IF a SHOPKEEPER attempts a status transition not listed in criterion 1, THEN THE Purchase_Order_Service SHALL throw a `BusinessException` with a message describing the invalid transition.
3. WHEN a SHOPKEEPER marks a `DRAFT` Purchase_Order as `SENT`, THE Purchase_Order_Service SHALL persist the updated status without modifying line items or totals.
4. WHEN a Purchase_Order transitions to `CANCELLED`, THE Purchase_Order_Service SHALL set the status to `CANCELLED` and SHALL NOT reverse any stock that was previously received.
5. WHEN a Purchase_Order transitions to `RECEIVED`, THE Purchase_Order_Service SHALL set `receivedDate` to the current date.
6. WHILE a Purchase_Order has status `RECEIVED` or `CANCELLED`, THE PurchaseController SHALL render the detail view with all edit and status-change actions disabled.

---

### Requirement 4: Stock Receipt — Full and Partial

**User Story:** As a SHOPKEEPER, I want to record the receipt of goods against a purchase order (fully or partially), so that medicine stock quantities are automatically updated when stock arrives.

#### Acceptance Criteria

1. WHEN a SHOPKEEPER submits a receipt form for a `SENT` or `PARTIALLY_RECEIVED` Purchase_Order with received quantities for each PO_Line_Item, THE Purchase_Order_Service SHALL update each PO_Line_Item's `receivedQuantity` and atomically increment the corresponding Medicine's `quantity` by the newly received amount.
2. THE Purchase_Order_Service SHALL use `MedicineRepository.addStock(medicineId, delta)` to update Medicine stock quantities to prevent lost-update races under concurrent access.
3. IF all PO_Line_Items have `receivedQuantity` equal to their `quantity` after a receipt, THEN THE Purchase_Order_Service SHALL set the Purchase_Order status to `RECEIVED`.
4. IF at least one PO_Line_Item has `receivedQuantity` less than its `quantity` after a receipt, THEN THE Purchase_Order_Service SHALL set the Purchase_Order status to `PARTIALLY_RECEIVED`.
5. IF a SHOPKEEPER submits a received quantity greater than the ordered quantity for any PO_Line_Item, THEN THE Purchase_Order_Service SHALL throw a `BusinessException` stating "Received quantity cannot exceed ordered quantity."
6. IF a SHOPKEEPER submits a received quantity less than zero for any PO_Line_Item, THEN THE Purchase_Order_Service SHALL throw a `BusinessException` stating "Received quantity cannot be negative."
7. WHEN stock is received, THE Purchase_Order_Service SHALL evict the `medicines_search` and `dashboard_kpis` caches.
8. IF a SHOPKEEPER attempts to receive stock against a `DRAFT`, `RECEIVED`, or `CANCELLED` Purchase_Order, THEN THE Purchase_Order_Service SHALL throw a `BusinessException` stating "Stock can only be received against SENT or PARTIALLY_RECEIVED orders."

---

### Requirement 5: Supplier Credit and Payment Tracking

**User Story:** As a SHOPKEEPER, I want supplier credit records to be automatically created when stock is received and to record payments against them, so that I can track what the pharmacy owes each supplier.

#### Acceptance Criteria

1. WHEN a Purchase_Order transitions to `RECEIVED` or `PARTIALLY_RECEIVED` for the first time, THE Purchase_Order_Service SHALL create a Supplier_Credit record with `totalDue` equal to the total value of received goods, linked to the Purchase_Order's supplier and branch.
2. WHEN additional stock is received against a `PARTIALLY_RECEIVED` Purchase_Order, THE Purchase_Order_Service SHALL update the existing Supplier_Credit's `totalDue` by adding the value of the newly received goods.
3. WHEN a SHOPKEEPER records a Supplier_Payment with a positive amount against a Supplier_Credit, THE SupplierCreditService SHALL add the payment amount to `paidAmount` and call `updateStatus()` to recalculate the credit status.
4. IF a SHOPKEEPER records a Supplier_Payment where the payment amount exceeds the remaining balance (`totalDue - paidAmount`), THEN THE SupplierCreditService SHALL throw an `IllegalArgumentException` stating "Payment exceeds total due amount."
5. IF a SHOPKEEPER records a Supplier_Payment with an amount less than or equal to zero, THEN THE SupplierCreditService SHALL throw an `IllegalArgumentException` stating "Payment amount must be greater than zero."
6. THE SupplierCreditService SHALL set Supplier_Credit status to `PAID` when `paidAmount >= totalDue`, `PARTIAL` when `paidAmount > 0` and `paidAmount < totalDue`, `OVERDUE` when `paidAmount == 0` and the current date is after `dueDate`, and `PENDING` otherwise.

---

### Requirement 6: Purchase Order List View

**User Story:** As a SHOPKEEPER or ADMIN, I want to view a paginated, searchable list of purchase orders for my branch, so that I can quickly find and manage orders.

#### Acceptance Criteria

1. THE PurchaseController SHALL render a paginated list of Purchase_Orders scoped to the current TenantContext branch, with a default page size of 20 and sorted by `orderDate` descending.
2. WHEN a SHOPKEEPER or ADMIN applies a status filter on the list page, THE PurchaseController SHALL return only Purchase_Orders matching the selected PO_Status.
3. WHEN a SHOPKEEPER or ADMIN applies a supplier filter on the list page, THE PurchaseController SHALL return only Purchase_Orders linked to the selected supplier.
4. WHEN a SHOPKEEPER or ADMIN applies a date range filter on the list page, THE PurchaseController SHALL return only Purchase_Orders whose `orderDate` falls within the specified range (inclusive).
5. THE PurchaseController SHALL display each Purchase_Order in the list with: Order_Number, supplier name, order date, status badge (colour-coded), total amount, and action buttons.
6. WHILE the list contains no Purchase_Orders matching the current filters, THE PurchaseController SHALL render an empty-state message: "No purchase orders found."

---

### Requirement 7: Purchase Order Detail View

**User Story:** As a SHOPKEEPER or ADMIN, I want to view the full details of a purchase order including all line items and receipt history, so that I can understand the complete state of an order.

#### Acceptance Criteria

1. WHEN a SHOPKEEPER or ADMIN requests the detail view for a Purchase_Order, THE PurchaseController SHALL render the Order_Number, supplier details, order date, status, notes, all PO_Line_Items (medicine name, ordered quantity, received quantity, unit price, line total), the order total, and the received date (if applicable).
2. WHEN the Purchase_Order has an associated Supplier_Credit, THE PurchaseController SHALL display the credit summary (total due, paid amount, remaining balance, status) on the detail view.
3. WHILE a Purchase_Order has status `SENT` or `PARTIALLY_RECEIVED`, THE PurchaseController SHALL render a "Receive Stock" action button on the detail view.
4. WHILE a Purchase_Order has status `DRAFT`, THE PurchaseController SHALL render "Edit" and "Mark as Sent" action buttons on the detail view.
5. WHILE a Purchase_Order has status `DRAFT` or `SENT`, THE PurchaseController SHALL render a "Cancel Order" action button on the detail view.
6. IF a SHOPKEEPER or ADMIN requests the detail view for a Purchase_Order belonging to a different branch, THEN THE Purchase_Order_Service SHALL throw an `AccessDeniedException`.

---

### Requirement 8: Purchase Order Deletion

**User Story:** As an ADMIN, I want to delete a purchase order, so that I can remove erroneous records from the system.

#### Acceptance Criteria

1. WHEN an ADMIN requests deletion of a Purchase_Order, THE PurchaseController SHALL require confirmation before proceeding.
2. WHEN an ADMIN confirms deletion of a Purchase_Order, THE Purchase_Order_Service SHALL permanently delete the Purchase_Order and all associated PO_Line_Items.
3. IF an ADMIN attempts to delete a Purchase_Order with status `RECEIVED`, THEN THE Purchase_Order_Service SHALL throw a `BusinessException` stating "Received orders cannot be deleted as they have updated stock."
4. WHEN a Purchase_Order is deleted, THE PurchaseController SHALL redirect to the PO list view with a success flash message.

---

### Requirement 9: PO History and Reporting for OWNER

**User Story:** As an OWNER, I want to view purchase order history and summary reports across all my branches, so that I can monitor procurement spend and supplier relationships.

#### Acceptance Criteria

1. WHEN an OWNER accesses the PO report view, THE PurchaseController SHALL render a list of Purchase_Orders scoped to all branches owned by the current OWNER, with pagination (default page size 20).
2. WHEN an OWNER applies a branch filter on the PO report view, THE PurchaseController SHALL return only Purchase_Orders belonging to the selected branch.
3. WHEN an OWNER applies a date range filter on the PO report view, THE PurchaseController SHALL return only Purchase_Orders whose `orderDate` falls within the specified range (inclusive).
4. THE PurchaseController SHALL display a summary row on the PO report view showing: total number of orders, total spend (sum of `totalAmount` for `RECEIVED` orders), and total outstanding supplier credit across the filtered result set.
5. WHILE an OWNER views the PO report, THE PurchaseController SHALL render the view in read-only mode with no create, edit, receive, or delete actions available.

---

### Requirement 10: Role-Based Access Control

**User Story:** As a system administrator, I want purchase order operations to be strictly gated by role, so that only authorised users can perform sensitive actions.

#### Acceptance Criteria

1. THE SecurityConfig SHALL restrict all `/purchases/**` URL patterns to users with role `ADMIN` or `SHOPKEEPER`.
2. THE PurchaseController SHALL restrict PO creation, editing, receiving, cancellation, and payment recording to users with role `SHOPKEEPER` via `@PreAuthorize("hasRole('SHOPKEEPER')")`.
3. THE PurchaseController SHALL restrict PO deletion to users with role `ADMIN` via `@PreAuthorize("hasRole('ADMIN')")`.
4. WHEN an OWNER accesses `/purchases/**`, THE SecurityConfig SHALL deny access and redirect to the OWNER dashboard with a `denied=true` parameter.
5. WHERE the OWNER role requires PO visibility, THE OwnerController SHALL expose a dedicated read-only PO report endpoint under `/owner/purchases` accessible to `OWNER` and `ADMIN` roles.
6. THE Purchase_Order_Service SHALL enforce branch-level tenant isolation on every read and write operation by comparing the Purchase_Order's `branch.id` against the value from TenantContext.

---

### Requirement 11: Data Integrity and Concurrency

**User Story:** As a system architect, I want purchase order and stock updates to be safe under concurrent access, so that stock quantities are never corrupted by race conditions.

#### Acceptance Criteria

1. THE PurchaseOrder entity SHALL carry a `@Version` field for optimistic locking; concurrent status transitions on the same order SHALL result in an `OptimisticLockException` for the losing writer.
2. THE Medicine entity's `quantity` field SHALL be updated exclusively via `MedicineRepository.addStock(id, delta)` (an atomic SQL `UPDATE ... SET quantity = quantity + ?`) during stock receipt to prevent lost-update races.
3. THE Purchase_Order_Service SHALL wrap all stock receipt operations in a single `@Transactional` boundary so that partial failures roll back both the PO status update and all Medicine stock increments atomically.
4. THE Purchase_Order_Service SHALL wrap all PO save operations in a single `@Transactional` boundary so that PO header and PO_Line_Item persistence are atomic.
5. IF an `OptimisticLockException` is thrown during a status transition, THEN THE PurchaseController SHALL catch it and return an error message stating "The order was modified by another user. Please refresh and try again."

---

### Requirement 12: UI Consistency and Navigation

**User Story:** As a SHOPKEEPER, I want the purchase order UI to follow the same patterns as the rest of the application, so that I can navigate and use it without learning new interaction patterns.

#### Acceptance Criteria

1. THE purchase order list, form, and detail Thymeleaf templates SHALL extend `layout.html` via `th:replace="~{layout :: layout(...)}"`.
2. THE purchase order templates SHALL display success and error flash messages using the `successMessage` and `errorMessage` model attributes rendered by the standard alert pattern.
3. THE purchase order list template SHALL use the standard table pattern with striped rows, hover states, a `table-dark` header, and action buttons in the last column.
4. THE purchase order form template SHALL include a CSRF token hidden input in all POST forms.
5. THE PurchaseController SHALL use `RoutePaths` constants for all redirect targets; THE purchase order templates SHALL use `@{/purchases/...}` Thymeleaf URL syntax for all links.
6. THE purchase order list template SHALL display PO_Status values as colour-coded Bootstrap badges: `DRAFT` → secondary, `SENT` → info, `PARTIALLY_RECEIVED` → warning, `RECEIVED` → success, `CANCELLED` → danger.
7. THE purchase order detail template SHALL display currency values formatted as `₹#,##0.00`.
