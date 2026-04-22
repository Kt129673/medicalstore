# Medicines Inventory Bugfix Design

## Overview

Five targeted bugs in the Medicines Inventory module are fixed with minimal, surgical changes.
The bugs span three layers: CSS (missing class definitions), Thymeleaf template (wrong loop variable),
JavaScript load order (defer timing), Java service (missing map key), and a Thymeleaf null-safety
confirmation. Each fix is isolated to a single file and does not alter surrounding logic.

---

## Glossary

- **Bug_Condition (C)**: The specific input or state that triggers each defect.
- **Property (P)**: The correct observable behavior that must hold after the fix.
- **Preservation**: All behaviors not covered by C(X) that must remain identical before and after the fix.
- **`inv-*` classes**: CSS class names used exclusively in `list.html` for the inventory table layout.
- **`buildBranchDashboard` / `buildOwnerDashboard`**: Methods in `DashboardService` that build the KPI map for Shopkeeper and Owner roles respectively.
- **`defer` attribute**: HTML script attribute that delays execution until after the document is parsed; scripts with `defer` execute in source order after parsing, but inline `<script>` blocks inside the page content fragment execute before deferred external scripts.
- **`${m.id}` vs `${medicine.id}`**: Thymeleaf expression referencing the loop variable; the loop uses `th:each="medicine : ${expiredMedicines}"`, so `m` is undefined.

---

## Bug Details

### Bug 1 — Missing CSS Classes for Inventory Table

The bug manifests when any user visits `/medicines`. The template `list.html` references CSS classes
(`.inv-table`, `.inv-filter-bar`, `.inv-table-card`, `.inv-table-header`, `.inv-table-wrapper`,
`.inv-pagination`, `.inv-empty`, `.inv-row`, `.cat-tag`, `.stock-pill`, `.expiry-badge`, `.med-icon`,
`.inv-count-badge`, `.inv-toolbar`) that are not defined in any loaded stylesheet.

**Formal Specification:**
```
FUNCTION isBugCondition_1(request)
  INPUT: HTTP GET /medicines
  OUTPUT: boolean

  RETURN cssClassesUsedByListHtml NOT IN definedClassesAcrossAllLoadedStylesheets
END FUNCTION
```

**Examples:**
- User visits `/medicines` → columns "Price", "Stock", "Batch" appear merged with no spacing.
- Filter bar renders as unstyled `<div>` with no visual separation.
- Stock-level pills (`.stock-pill.critical`, `.stock-pill.low`, `.stock-pill.ok`) render as plain text.

---

### Bug 2 — Wrong Variable `${m.id}` in expiry-alerts.html Delete Form

The bug manifests when an ADMIN clicks "Remove" on an expired medicine. The form action uses
`${m.id}` but the Thymeleaf loop variable is `medicine` (declared as `th:each="medicine : ${expiredMedicines}"`),
so `m` resolves to null/empty, producing a URL like `/medicines/delete/` which returns 404.

**Formal Specification:**
```
FUNCTION isBugCondition_2(request)
  INPUT: POST /medicines/delete/{id} from expiry-alerts.html
  OUTPUT: boolean

  loopVar := "medicine"   -- from th:each="medicine : ${expiredMedicines}"
  formVar := "m"          -- from th:action="@{/medicines/delete/{id}(id=${m.id})}"
  RETURN loopVar != formVar   -- true → bug is present
END FUNCTION
```

**Examples:**
- ADMIN clicks "Remove" on expired medicine ID 42 → POST to `/medicines/delete/` → 404.
- Medicine is never deleted; user sees an error page.

---

### Bug 3 — `entInitTableSort` Called Before `layout.js` Is Available

The bug manifests when `list.html` finishes parsing. The inline `<script>` block inside the page
content fragment calls `entInitTableSort('inventoryTable')` inside a `DOMContentLoaded` listener.
`layout.js` is loaded with the `defer` attribute in `layout.html`, which means it executes after
HTML parsing completes — but the inline script's `DOMContentLoaded` callback also fires at that
same moment, before deferred scripts have run. The result is `ReferenceError: entInitTableSort is not defined`.

**Formal Specification:**
```
FUNCTION isBugCondition_3(pageLoad)
  INPUT: DOMContentLoaded event on /medicines
  OUTPUT: boolean

  layoutJsDeferred := true   -- layout.js has defer attribute
  inlineCallInDOMContentLoaded := true
  RETURN layoutJsDeferred AND inlineCallInDOMContentLoaded
         AND entInitTableSort NOT IN window AT TIME OF DOMContentLoaded
END FUNCTION
```

**Examples:**
- Page loads → browser console shows `ReferenceError: entInitTableSort is not defined`.
- Clicking column headers does nothing; sort is silently broken.

---

### Bug 4 — `expiredCount` Missing for Non-Admin Roles

The bug manifests when the dashboard KPI API is called for a Shopkeeper or Owner user.
`buildAdminDashboard()` puts `expiredCount` into the map; `buildBranchDashboard()` and
`buildOwnerDashboard()` do not. The dashboard JS checks `if (data.expiredCount)` and only
shows `#cardExpired` when the key is present and non-zero.

**Formal Specification:**
```
FUNCTION isBugCondition_4(role)
  INPUT: authenticated user role
  OUTPUT: boolean

  RETURN role IN ['SHOPKEEPER', 'OWNER']
         AND 'expiredCount' NOT IN dashboardKpiMap(role)
END FUNCTION
```

**Examples:**
- Shopkeeper logs in → dashboard loads → expired medicines card never appears even when 5 medicines are expired.
- Owner logs in → same result; `data.expiredCount` is `undefined`, falsy check hides the card.

---

### Bug 5 — Supplier Null Safety in low-stock.html

The bug manifests when a medicine in the low-stock list has a `null` supplier. The template
already guards with `th:if="${medicine.supplier != null}"` before accessing `medicine.supplier.name`,
so the null-safety is already in place. This bug is confirmed mitigated.

**Formal Specification:**
```
FUNCTION isBugCondition_5(medicine)
  INPUT: Medicine entity
  OUTPUT: boolean

  RETURN medicine.supplier == null
         AND template accesses medicine.supplier.name WITHOUT null guard
END FUNCTION
```

**Current state:** `low-stock.html` line already reads:
```html
<small class="text-muted d-block" th:if="${medicine.supplier != null}"
       th:text="${medicine.supplier.name}"></small>
```
The guard is present. `isBugCondition_5` evaluates to `false` — no code change required.

---

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- ADMIN delete form on `list.html` (`/medicines`) continues to use `medicine.id` correctly (Bug 2 fix must not touch this form).
- "Update" links on `expiry-alerts.html` "Expiring Within 30 Days" section continue to use `medicine.id` correctly.
- Dashboard `#cardExpired` continues to be hidden when `expiredCount` is 0 or absent (Bug 4 fix adds the key with value 0, not a truthy value, so the existing JS `if (data.expiredCount)` guard still hides the card correctly).
- All existing CSS classes in `components.css` and other stylesheets are unaffected by adding a new CSS file.
- `entInitTableSort` continues to work on all other pages that call it after `layout.js` is loaded normally.
- Low-stock page continues to display supplier name when supplier is non-null.
- Filters, pagination, and server-side search on `/medicines` continue to work regardless of CSS or JS changes.

**Scope:**
All inputs that do NOT involve the five specific bug conditions are completely unaffected.

---

## Hypothesized Root Cause

### Bug 1 — Missing CSS
The inventory table in `list.html` was written with a custom design system (`inv-*` classes) that was
never added to any stylesheet. The classes were designed but the CSS rules were never committed.

### Bug 2 — Wrong Variable Name
A copy-paste or rename error: the loop variable was changed from `m` to `medicine` at some point
(for consistency with the rest of the template) but the form action expression was not updated.

### Bug 3 — Defer Timing
`layout.js` uses the `defer` attribute, which guarantees execution after HTML parsing but does not
guarantee execution before inline `DOMContentLoaded` callbacks registered by page-level scripts.
The inline script in `list.html` registers a `DOMContentLoaded` listener that calls `entInitTableSort`
— but at the time `DOMContentLoaded` fires, deferred external scripts have not yet executed.
The fix is to move the `entInitTableSort` call out of `DOMContentLoaded` and into a `window.load`
listener, or to use a `setTimeout` / polling approach. The simplest correct fix is to call
`entInitTableSort` from a `window.addEventListener('load', ...)` handler, which fires after all
deferred scripts have executed.

### Bug 4 — Missing Map Key
`buildBranchDashboard` and `buildOwnerDashboard` were written without the `expiredCount` key.
The admin dashboard was implemented first and included it; the branch/owner variants were added
later and the key was omitted. The fix is to add the same `expiredCount` query to both methods,
scoped to branch or owner respectively.

### Bug 5 — Already Mitigated
The null guard was already added to `low-stock.html`. No root cause to address.

---

## Correctness Properties

Property 1: Bug Condition 1 — Inventory Table CSS Classes Are Defined

_For any_ request to `/medicines` by any authenticated user, the rendered page SHALL apply
visual styles to all elements using `.inv-table`, `.inv-filter-bar`, `.inv-table-card`,
`.inv-table-header`, `.inv-table-wrapper`, `.inv-pagination`, `.inv-empty`, `.inv-row`,
`.cat-tag`, `.stock-pill`, `.expiry-badge`, `.med-icon`, `.inv-count-badge`, and `.inv-toolbar`
classes, producing a correctly separated, styled inventory table.

**Validates: Requirements 2.1**

Property 2: Bug Condition 2 — Expiry Alerts Delete Form Uses Correct Variable

_For any_ POST request submitted by the "Remove" button on the expired medicines table in
`/medicines/expiry-alerts`, the fixed template SHALL resolve the form action to
`/medicines/delete/{medicine.id}` using the correct loop variable `medicine`, resulting in
a successful delete and redirect rather than a 404.

**Validates: Requirements 2.2**

Property 3: Bug Condition 3 — Table Sort Initialises Without ReferenceError

_For any_ page load of `/medicines`, the fixed code SHALL successfully call
`entInitTableSort('inventoryTable')` only after `layout.js` has been executed, so that
`window.entInitTableSort` is defined at the time of the call, enabling column sort with no
console errors.

**Validates: Requirements 2.3**

Property 4: Bug Condition 4 — `expiredCount` Present for All Roles

_For any_ authenticated user of any role (ADMIN, OWNER, SHOPKEEPER) calling
`GET /api/v1/dashboard/kpis`, the fixed API SHALL include the `expiredCount` key in the
response map, allowing the dashboard JS to correctly show or hide `#cardExpired` based on
whether the count is greater than zero.

**Validates: Requirements 2.4**

Property 5: Preservation — Non-Buggy Inputs Unchanged

_For any_ input where none of the four active bug conditions hold (e.g., ADMIN delete on
`list.html`, "Update" links on expiry-alerts, low-stock page with non-null supplier, dashboard
card hidden when count is 0), the fixed code SHALL produce exactly the same behavior as the
original code.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**

---

## Fix Implementation

### Bug 1 — Add Missing CSS Classes

**File:** `src/main/resources/static/css/layout/medicines.css` *(new file)*

**Rationale:** Adding to `components.css` would make it harder to isolate medicines-specific styles.
A new file keeps the change minimal and scoped. It must be linked from `layout.html` alongside the
other component stylesheets.

**Also modify:** `src/main/resources/templates/layout.html`

**Specific Changes:**

1. Create `medicines.css` with rules for all missing classes:
   - `.inv-filter-bar` — styled filter row container with padding, background, border-radius
   - `.inv-table-card` — card wrapper with shadow and border
   - `.inv-table-header` — flex row with title and toolbar
   - `.inv-table-wrapper` — overflow-x: auto wrapper
   - `.inv-table` — table base styles (border-collapse, font-size)
   - `.inv-pagination` — flex pagination bar
   - `.inv-empty` — centered empty-state block
   - `.inv-row` — table row hover state
   - `.cat-tag` — small category pill badge
   - `.stock-pill` — stock level pill with `.critical`, `.low`, `.ok` variants and `.dot` indicator
   - `.expiry-badge` — expiry date badge with `.expiry-expired`, `.expiry-soon`, `.expiry-normal` variants
   - `.med-icon` — small circular medicine icon container
   - `.inv-count-badge` — item count badge in table header
   - `.inv-toolbar` — right-side toolbar in table header

2. Add `<link>` tag in `layout.html` `<head>` after the existing component CSS links:
   ```html
   <link rel="stylesheet" th:href="@{/css/layout/medicines.css}" href="/css/layout/medicines.css">
   ```

---

### Bug 2 — Fix Wrong Variable in expiry-alerts.html

**File:** `src/main/resources/templates/medicines/expiry-alerts.html`

**Specific Change:**

Replace `${m.id}` with `${medicine.id}` in the delete form action:

```html
<!-- Before -->
th:action="@{/medicines/delete/{id}(id=${m.id})}"

<!-- After -->
th:action="@{/medicines/delete/{id}(id=${medicine.id})}"
```

One character change. No other lines are touched.

---

### Bug 3 — Fix entInitTableSort Call Timing

**File:** `src/main/resources/templates/medicines/list.html`

**Specific Change:**

Move the `entInitTableSort` call from inside the `DOMContentLoaded` listener to a `window.load`
listener. `window.load` fires after all resources including deferred scripts have executed,
guaranteeing `entInitTableSort` is defined.

```javascript
// Before
document.addEventListener('DOMContentLoaded', () => {
    allRows = Array.from(document.querySelectorAll('#tableBody .inv-row'));
    entInitTableSort('inventoryTable');
});

// After
document.addEventListener('DOMContentLoaded', () => {
    allRows = Array.from(document.querySelectorAll('#tableBody .inv-row'));
});
window.addEventListener('load', () => {
    entInitTableSort('inventoryTable');
});
```

`allRows` population stays in `DOMContentLoaded` because it only needs the DOM, not `layout.js`.

---

### Bug 4 — Add `expiredCount` to Branch and Owner Dashboards

**File:** `src/main/java/com/medicalstore/service/DashboardService.java`

**Specific Changes:**

In `buildBranchDashboard(Long branchId)`, add after the existing `expiringIn90` line:
```java
data.put("expiredCount",
    medicineRepository.countByBranchIdAndExpiryDateBetween(
        branchId, LocalDate.of(2000, 1, 1), now.minusDays(1)));
```

In `buildOwnerDashboard(Long ownerId)`, add after the existing `expiringIn90` line:
```java
data.put("expiredCount",
    medicineRepository.countExpiringByOwner(
        ownerId, LocalDate.of(2000, 1, 1), now.minusDays(1)));
```

Both use existing repository methods already called in the same methods for other date ranges.
No new repository methods are needed.

---

### Bug 5 — No Change Required

`low-stock.html` already has `th:if="${medicine.supplier != null}"` guarding the supplier name
access. The fix is confirmed present. No code change.

---

## Testing Strategy

### Validation Approach

Two-phase approach: first run exploratory tests on unfixed code to confirm the bug manifests as
described, then verify fixes and run preservation checks.

---

### Exploratory Bug Condition Checking

**Goal:** Surface counterexamples that demonstrate each bug on unfixed code.

**Test Plan:** Write tests that exercise each bug condition and assert the expected (correct)
behavior. Run on unfixed code to observe failures and confirm root cause analysis.

**Test Cases:**

1. **CSS Classes Present (Bug 1)**: Load `/medicines` and assert that computed styles for
   `.inv-table`, `.stock-pill`, `.cat-tag` are non-empty. Will fail on unfixed code because
   the classes are undefined.

2. **Delete Form URL (Bug 2)**: Render `expiry-alerts.html` with a mock `expiredMedicines` list
   and assert the form action contains a numeric ID. Will fail on unfixed code because `${m.id}`
   resolves to empty string.

3. **entInitTableSort Available at Call Time (Bug 3)**: In a browser test, assert that
   `window.entInitTableSort` is defined when the `DOMContentLoaded` callback fires. Will fail
   on unfixed code because `layout.js` is deferred.

4. **expiredCount in API Response (Bug 4)**: Call `GET /api/v1/dashboard/kpis` as a Shopkeeper
   and assert `response.expiredCount` is not `undefined`. Will fail on unfixed code.

**Expected Counterexamples:**
- Bug 1: CSS rules not found → layout collapses.
- Bug 2: Form action resolves to `/medicines/delete/` → 404.
- Bug 3: `ReferenceError` in console → sort non-functional.
- Bug 4: `expiredCount` key absent → expired card never shown.

---

### Fix Checking

**Goal:** Verify that for all inputs where each bug condition holds, the fixed code produces the expected behavior.

**Pseudocode:**
```
FOR ALL request WHERE isBugCondition_N(request) DO
  result := fixedCode(request)
  ASSERT property_N(result)
END FOR
```

**Test Cases:**
1. Load `/medicines` after CSS fix → assert `.stock-pill.critical` has `background-color` set.
2. Render expiry-alerts with medicine ID 42 → assert form action is `/medicines/delete/42`.
3. Load `/medicines` after timing fix → assert no `ReferenceError`, column headers are clickable.
4. Call `/api/v1/dashboard/kpis` as Shopkeeper → assert `expiredCount` key present in JSON.

---

### Preservation Checking

**Goal:** Verify that for all inputs where the bug condition does NOT hold, the fixed code produces the same result as the original.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition_N(input) DO
  ASSERT originalCode(input) = fixedCode(input)
END FOR
```

**Testing Approach:** Property-based testing is recommended for the dashboard API (Bug 4) because
it can generate many role/state combinations. Unit tests suffice for the template fixes.

**Test Cases:**
1. **ADMIN delete on list.html (Bug 2 preservation)**: Assert form action still uses `medicine.id` correctly — unchanged line.
2. **"Update" link on expiry-alerts (Bug 2 preservation)**: Assert `th:href` for expiring-soon section still uses `medicine.id`.
3. **Dashboard card hidden when count=0 (Bug 4 preservation)**: Call API with no expired medicines → assert `expiredCount` is 0 → JS `if (data.expiredCount)` evaluates false → card hidden.
4. **Low-stock with non-null supplier (Bug 5 preservation)**: Assert supplier name renders when supplier is present.
5. **Other pages using entInitTableSort (Bug 3 preservation)**: Assert sort still works on pages where `layout.js` loads normally.

---

### Unit Tests

- Render `expiry-alerts.html` with Thymeleaf test context; assert form action URL contains medicine ID.
- Call `DashboardService.buildBranchDashboard(branchId)` directly; assert returned map contains `expiredCount` key.
- Call `DashboardService.buildOwnerDashboard(ownerId)` directly; assert returned map contains `expiredCount` key.
- Call `DashboardService.buildAdminDashboard()` directly; assert `expiredCount` still present (regression check).

### Property-Based Tests

- Generate random branch IDs and assert `buildBranchDashboard` always returns a map with `expiredCount` ≥ 0.
- Generate random owner IDs and assert `buildOwnerDashboard` always returns a map with `expiredCount` ≥ 0.
- Generate random medicine lists (some with null supplier, some without) and assert low-stock page renders without error for all combinations.

### Integration Tests

- `GET /medicines` as Shopkeeper → HTTP 200, response body contains rendered table with styled classes.
- `POST /medicines/delete/{id}` from expiry-alerts page as ADMIN → HTTP 302 redirect (not 404).
- `GET /api/v1/dashboard/kpis` as Shopkeeper → JSON contains `expiredCount`.
- `GET /api/v1/dashboard/kpis` as Owner → JSON contains `expiredCount`.
- `GET /medicines/low-stock` with medicines having null supplier → HTTP 200, no exception.
