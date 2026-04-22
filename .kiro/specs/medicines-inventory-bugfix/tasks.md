# Implementation Plan

- [x] 1. Write bug condition exploration tests (BEFORE implementing any fix)
  - **Property 1: Bug Condition** - All Four Active Bugs
  - **CRITICAL**: These tests MUST FAIL on unfixed code — failure confirms each bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: Tests encode expected behavior — they will validate the fixes when they pass after implementation
  - **GOAL**: Surface counterexamples that demonstrate each bug on unfixed code
  - **Scoped PBT Approach**: For deterministic bugs (2, 3, 4), scope to concrete failing cases for reproducibility
  - Create `src/test/java/com/medicalstore/service/DashboardServiceBugExplorationTest.java`
    - Bug 4 exploration: call `buildBranchDashboard(anyBranchId)` and assert returned map contains key `"expiredCount"` — FAILS on unfixed code because the key is absent
    - Call `buildOwnerDashboard(anyOwnerId)` and assert returned map contains key `"expiredCount"` — FAILS on unfixed code
    - Scoped PBT: for any branchId in [1L, 2L, 3L], `buildBranchDashboard(branchId)` map must contain `"expiredCount"` — generates counterexample on unfixed code
  - Create `src/test/java/com/medicalstore/controller/MedicineExpiryAlertsBugExplorationTest.java`
    - Bug 2 exploration: render `expiry-alerts.html` via MockMvc GET `/medicines/expiry-alerts` as ADMIN with at least one expired medicine in DB; assert response body contains `/medicines/delete/` followed by a numeric ID — FAILS on unfixed code because `${m.id}` resolves to empty string, producing `/medicines/delete/`
  - Document counterexamples found:
    - Bug 2: form action resolves to `/medicines/delete/` (no ID) → 404 on submit
    - Bug 3: `ReferenceError: entInitTableSort is not defined` in browser console (manual verification note)
    - Bug 4: `buildBranchDashboard` map keys do not include `"expiredCount"` → `data.expiredCount` is `undefined` on dashboard
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests FAIL (this is correct — it proves the bugs exist)
  - Mark task complete when tests are written, run, and failures are documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Write preservation property tests (BEFORE implementing any fix)
  - **Property 2: Preservation** - Non-Buggy Inputs Unchanged
  - **IMPORTANT**: Follow observation-first methodology — observe unfixed code behavior for non-buggy inputs first
  - Create `src/test/java/com/medicalstore/service/DashboardServicePreservationTest.java`
    - Observe: `buildAdminDashboard()` already returns `expiredCount` on unfixed code — record this as baseline
    - Observe: `buildBranchDashboard(branchId)` returns `expiringIn30`, `expiringIn60`, `expiringIn90` correctly on unfixed code
    - Write property-based test: for any branchId, `buildBranchDashboard` map always contains keys `"todaySales"`, `"monthlyRevenue"`, `"totalMedicines"`, `"lowStockCount"`, `"expiringIn30"`, `"expiringIn60"`, `"expiringIn90"` (the non-buggy keys that must be preserved)
    - Write property-based test: for any ownerId, `buildOwnerDashboard` map always contains the same non-buggy keys
    - Write test: `buildAdminDashboard()` still contains `"expiredCount"` after fix (regression guard)
    - Write test: when `expiredCount` is 0, the value is `0L` (not absent) — JS `if (data.expiredCount)` evaluates false, card stays hidden
  - Create `src/test/java/com/medicalstore/controller/MedicineListPreservationTest.java`
    - Observe: ADMIN delete form on `list.html` uses `medicine.id` correctly on unfixed code — record as baseline
    - Observe: "Update" link on expiry-alerts "Expiring Within 30 Days" section uses `medicine.id` correctly on unfixed code
    - Write test: GET `/medicines/expiry-alerts` response body contains `href="/medicines/edit/` followed by numeric ID in the expiring-soon section (preservation of correct variable usage)
    - Write test: GET `/medicines` response body contains `action="/medicines/delete/` followed by numeric ID in the main list delete form (ADMIN delete on list.html must be unaffected)
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [-] 3. Fix all four active bugs

  - [x] 3.1 Create `src/main/resources/static/css/layout/medicines.css` with all missing `inv-*` CSS classes
    - Create new file `src/main/resources/static/css/layout/medicines.css`
    - Add `.inv-filter-bar` — filter row container with padding, background, border-radius, border-bottom
    - Add `.inv-table-card` — card wrapper with shadow, border, border-radius
    - Add `.inv-table-header` — flex row (space-between) for title and toolbar, padding, border-bottom
    - Add `.inv-table-wrapper` — `overflow-x: auto` wrapper
    - Add `.inv-table` — table base styles: `border-collapse: collapse`, `width: 100%`, `font-size: 13.5px`; thead uppercase, small letter-spacing; tbody row hover state
    - Add `.inv-pagination` — flex row (space-between, align-center), padding, border-top; `.page-btn` pill buttons with active/hover states
    - Add `.inv-empty` — centered empty-state block: flex column, align-center, padding 60px; icon font-size 56px, opacity 0.35
    - Add `.inv-row` — table row hover: `background: var(--bg-secondary)` transition
    - Add `.cat-tag` — small category pill: inline-flex, padding 2px 8px, border-radius full, font-size 11px, font-weight 700, background `var(--primary-subtle)`, color `var(--primary-color)`
    - Add `.stock-pill` — stock level pill: inline-flex, align-center, gap 5px, padding 3px 10px, border-radius full, font-size 12px, font-weight 600; `.dot` — 6px circle; `.critical` red variant; `.low` amber variant; `.ok` green variant
    - Add `.expiry-badge` — expiry date badge: inline-flex, align-center, gap 4px, padding 3px 8px, border-radius 4px, font-size 12px; `.expiry-expired` red; `.expiry-soon` amber; `.expiry-normal` green
    - Add `.med-icon` — 32px circular icon container: flex center, border-radius 50%, background `var(--primary-subtle)`, color `var(--primary-color)`, font-size 14px, flex-shrink 0
    - Add `.inv-count-badge` — item count badge: inline-flex, padding 2px 8px, border-radius full, font-size 11px, font-weight 700, background `var(--bg-tertiary)`, color `var(--text-secondary)`
    - Add `.inv-toolbar` — right-side toolbar: display flex, align-center, gap 8px
    - _Bug_Condition: isBugCondition_1 — CSS classes used by list.html not defined in any loaded stylesheet_
    - _Expected_Behavior: all inv-* elements render with correct visual styles_
    - _Preservation: all existing classes in components.css and other stylesheets are unaffected_
    - _Requirements: 2.1_

  - [x] 3.2 Link `medicines.css` in `layout.html`
    - Open `src/main/resources/templates/layout.html`
    - Add after the existing `<link rel="stylesheet" th:href="@{/css/layout/responsive.css}" ...>` line:
      ```html
      <link rel="stylesheet" th:href="@{/css/layout/medicines.css}" href="/css/layout/medicines.css">
      ```
    - Verify the link tag follows the same pattern as the other component CSS links (no `media` or `onload` — medicines.css is render-critical for the inventory page)
    - _Bug_Condition: isBugCondition_1 — medicines.css not linked in layout.html_
    - _Requirements: 2.1_

  - [x] 3.3 Fix `${m.id}` → `${medicine.id}` in `expiry-alerts.html` delete form
    - Open `src/main/resources/templates/medicines/expiry-alerts.html`
    - Locate the delete form in the "Expired Medicines" table (line with `th:action="@{/medicines/delete/{id}(id=${m.id})}"`)
    - Replace `${m.id}` with `${medicine.id}` — one token change, no other lines touched
    - Verify the loop declaration `th:each="medicine : ${expiredMedicines}"` is unchanged
    - Verify the "Expiring Within 30 Days" section's `th:href="@{/medicines/edit/{id}(id=${medicine.id})}"` is unchanged (already correct)
    - _Bug_Condition: isBugCondition_2 — loopVar "medicine" != formVar "m"_
    - _Expected_Behavior: form action resolves to /medicines/delete/{medicine.id} with numeric ID_
    - _Preservation: Update links in expiring-soon section continue to use medicine.id correctly_
    - _Requirements: 2.2, 3.1, 3.2_

  - [x] 3.4 Fix `entInitTableSort` timing in `list.html` (move to `window.load`)
    - Open `src/main/resources/templates/medicines/list.html`
    - Locate the inline `<script>` block containing the `DOMContentLoaded` listener that calls `entInitTableSort('inventoryTable')`
    - Split the listener: keep `allRows` population inside `DOMContentLoaded` (only needs DOM); move `entInitTableSort('inventoryTable')` to a new `window.addEventListener('load', ...)` handler
    - Result:
      ```javascript
      document.addEventListener('DOMContentLoaded', () => {
          allRows = Array.from(document.querySelectorAll('#tableBody .inv-row'));
      });
      window.addEventListener('load', () => {
          entInitTableSort('inventoryTable');
      });
      ```
    - Do not touch the separate skeleton/real-rows `DOMContentLoaded` script block (it does not call `entInitTableSort`)
    - _Bug_Condition: isBugCondition_3 — layout.js deferred AND entInitTableSort called in DOMContentLoaded before deferred scripts run_
    - _Expected_Behavior: entInitTableSort called only after window.load, guaranteeing layout.js is executed_
    - _Preservation: allRows population and CSV export continue to work; entInitTableSort on other pages unaffected_
    - _Requirements: 2.3_

  - [x] 3.5 Add `expiredCount` to `buildBranchDashboard` and `buildOwnerDashboard` in `DashboardService.java`
    - Open `src/main/java/com/medicalstore/service/DashboardService.java`
    - In `buildBranchDashboard(Long branchId)`: add after the `expiringIn90` line:
      ```java
      data.put("expiredCount",
          medicineRepository.countByBranchIdAndExpiryDateBetween(
              branchId, LocalDate.of(2000, 1, 1), now.minusDays(1)));
      ```
    - In `buildOwnerDashboard(Long ownerId)`: add after the `expiringIn90` line:
      ```java
      data.put("expiredCount",
          medicineRepository.countExpiringByOwner(
              ownerId, LocalDate.of(2000, 1, 1), now.minusDays(1)));
      ```
    - Both use existing repository methods already called in the same methods — no new repository methods needed
    - Verify `buildAdminDashboard()` `expiredCount` line is unchanged
    - _Bug_Condition: isBugCondition_4 — role in [SHOPKEEPER, OWNER] AND "expiredCount" NOT IN dashboardKpiMap_
    - _Expected_Behavior: expiredCount key present in map for all roles; value is 0 when no expired medicines_
    - _Preservation: expiredCount=0 keeps #cardExpired hidden (JS if(data.expiredCount) evaluates false); admin dashboard expiredCount unaffected_
    - _Requirements: 2.4, 3.4_

  - [-] 3.6 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - All Four Active Bugs Fixed
    - **IMPORTANT**: Re-run the SAME tests from task 1 — do NOT write new tests
    - The tests from task 1 encode the expected behavior
    - When these tests pass, it confirms the expected behavior is satisfied for all four bugs
    - Run `DashboardServiceBugExplorationTest` — assert `buildBranchDashboard` and `buildOwnerDashboard` maps contain `"expiredCount"`
    - Run `MedicineExpiryAlertsBugExplorationTest` — assert form action contains numeric medicine ID
    - **EXPECTED OUTCOME**: All exploration tests PASS (confirms bugs are fixed)
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [ ] 3.7 Verify preservation tests still pass
    - **Property 2: Preservation** - Non-Buggy Inputs Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 — do NOT write new tests
    - Run `DashboardServicePreservationTest` — assert all non-buggy keys still present, admin expiredCount still present, expiredCount=0 case still returns 0
    - Run `MedicineListPreservationTest` — assert ADMIN delete form on list.html still uses numeric ID, Update links on expiry-alerts still use numeric ID
    - **EXPECTED OUTCOME**: All preservation tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix

- [ ] 4. Write property-based tests for dashboard `expiredCount`
  - Create `src/test/java/com/medicalstore/service/DashboardServiceExpiredCountPropertyTest.java`
  - Use `@ExtendWith(MockitoExtension.class)` with mocked `MedicineRepository`, `SaleRepository`, `CustomerRepository`
  - **Property: for any branchId (sampled from a range of valid IDs), `buildBranchDashboard(branchId)` always returns a map where `expiredCount` is present and its value is ≥ 0**
    - Parameterize with `@MethodSource` or `@ValueSource` over a set of representative branchIds: [1L, 2L, 10L, 100L, Long.MAX_VALUE / 2]
    - Mock `countByBranchIdAndExpiryDateBetween` to return a non-negative long for any input
    - Assert `data.containsKey("expiredCount")` is true
    - Assert `(Long) data.get("expiredCount") >= 0`
  - **Property: for any ownerId, `buildOwnerDashboard(ownerId)` always returns a map where `expiredCount` is present and its value is ≥ 0**
    - Same parameterization pattern for ownerIds
    - Mock `countExpiringByOwner` to return a non-negative long
    - Assert `data.containsKey("expiredCount")` is true
    - Assert `(Long) data.get("expiredCount") >= 0`
  - **Property: when `expiredCount` is 0, the value is exactly `0L` (not null, not absent) — JS falsy check correctly hides the card**
    - Mock repository to return 0L for expired count
    - Assert `data.get("expiredCount")` equals `0L`
  - **Property: `buildAdminDashboard()` still contains `expiredCount` (regression guard)**
    - Assert admin map still has `"expiredCount"` key with value ≥ 0
  - _Requirements: 2.4, 3.4_

- [ ] 5. Write integration tests
  - Create `src/test/java/com/medicalstore/controller/api/MedicinesInventoryBugfixIntegrationTest.java`
  - Use `@SpringBootTest` + `@AutoConfigureMockMvc` with H2 in-memory DB (existing test profile)
  - **Bug 1 — CSS file served**: `GET /css/layout/medicines.css` returns HTTP 200 and `Content-Type: text/css`
  - **Bug 2 — Expiry alerts delete form**: `GET /medicines/expiry-alerts` as ADMIN returns HTTP 200; response body matches regex `action="/medicines/delete/\d+"` (numeric ID present in form action)
  - **Bug 4 — expiredCount for SHOPKEEPER**: `GET /api/v1/dashboard/kpis` as SHOPKEEPER returns HTTP 200; JSON response contains field `expiredCount` with a numeric value
  - **Bug 4 — expiredCount for OWNER**: `GET /api/v1/dashboard/kpis` as OWNER returns HTTP 200; JSON response contains field `expiredCount` with a numeric value
  - **Bug 4 — expiredCount for ADMIN (regression)**: `GET /api/v1/dashboard/kpis` as ADMIN still returns `expiredCount`
  - **Preservation — ADMIN delete on list.html**: `GET /medicines` as ADMIN returns HTTP 200; response body matches regex `action="/medicines/delete/\d+"` in the main inventory table (not expiry-alerts)
  - **Preservation — low-stock page**: `GET /medicines/low-stock` as SHOPKEEPER returns HTTP 200 with no exception
  - _Requirements: 2.1, 2.2, 2.4, 3.1, 3.3_

- [ ] 6. Checkpoint — Ensure all tests pass
  - Run `./mvnw test` and confirm zero test failures
  - Verify the following test classes all pass:
    - `DashboardServiceBugExplorationTest`
    - `MedicineExpiryAlertsBugExplorationTest`
    - `DashboardServicePreservationTest`
    - `MedicineListPreservationTest`
    - `DashboardServiceExpiredCountPropertyTest`
    - `MedicinesInventoryBugfixIntegrationTest`
  - Manually verify in browser (or note for manual QA):
    - `/medicines` renders with styled table, stock pills, category tags, expiry badges
    - `/medicines/expiry-alerts` "Remove" button submits to correct URL with medicine ID
    - Browser console shows no `ReferenceError` on `/medicines` page load; column sort works
    - Dashboard expired medicines card appears for Shopkeeper and Owner when expired medicines exist
  - Ask the user if any questions arise
