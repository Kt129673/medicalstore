# UI Standards — Enterprise-Grade Consistency

**CRITICAL: All UI implementations MUST maintain strict consistency and enterprise-grade quality.**

## Layout & Structure

- ALL pages MUST extend `layout.html` — never create standalone HTML files
- Use `fragments.html` and `fragments-role-guards.html` for common components
- Consistent spacing: Bootstrap utility classes only (`p-4`, `mb-3`, etc.)
- Every page must be responsive: desktop, tablet (768px), mobile (375px)
- Sidebar nav: keep structure consistent, highlight the active page
- Page header pattern: title + breadcrumb on left, action buttons on right

## Color Palette (CSS variables from `layout.css`)

| Use | Color |
|---|---|
| Primary actions | `#007bff` (`.btn-primary`) |
| Success | `#28a745` (`.btn-success`, `.alert-success`) |
| Danger / delete | `#dc3545` (`.btn-danger`) |
| Warning | `#ffc107` (`.alert-warning`) |
| Info | `#17a2b8` (`.alert-info`) |

Never hardcode hex values — use Bootstrap classes or CSS variables.

## Typography

- Page titles: `<h2>` (inside page header div)
- Section headers: `<h5>` (inside card headers)
- Body text: default Bootstrap paragraph styling
- Icons: Font Awesome (`fas fa-*`) — consistent across all pages

## Components

**Buttons**
- Main action: `.btn-primary` with icon + text (`<i class="fas fa-plus me-1"></i> Add`)
- Cancel/back: `.btn-secondary`
- Delete: `.btn-danger` — always behind a confirmation modal
- Small table actions: `.btn-sm .btn-outline-*`

**Tables**
- Always: `table-striped table-hover` + `table-dark` thead
- Actions column: `text-end`, last column
- Empty state row when list is empty (see ui-productivity.md for pattern)
- Wrap in `table-responsive` div

**Cards**
- `card shadow-sm` for all content cards
- `card-header` with `<h5 class="mb-0">` for section title
- `card-body` with default padding

**Modals**
- Standard Bootstrap modal structure (header / body / footer)
- Footer: Cancel left, confirm action right
- Always include CSRF token in modal forms

**Alerts / Flash messages**
- Use `alert-dismissible fade show` with `.btn-close`
- Model attribute names: `successMessage` (green) and `errorMessage` (red)
- See ui-productivity.md for the exact template pattern

**Status badges**
- Active: `badge bg-success`
- Expired / Inactive: `badge bg-danger`
- Pending: `badge bg-warning text-dark`
- Info: `badge bg-info`

## Data Display Conventions

| Data type | Format |
|---|---|
| Currency | `₹1,234.56` |
| Date | `DD MMM YYYY` (e.g. `15 Apr 2026`) |
| Date + time | `DD MMM YYYY, HH:mm` |
| Empty cell | `—` (em dash) |
| Long text | Truncate with ellipsis, full text on hover |

## Interaction Patterns

- Destructive actions (delete, archive) MUST use a confirmation modal — never a direct POST link
- Forms: prevent double-submit (disable button on submit, show spinner)
- Search inputs: debounce API calls (300ms)
- Loading states: show spinner for async operations
- Error handling: user-friendly messages only — never expose stack traces or raw exception messages

## Role-Based UI

- Use `sec:authorize` in Thymeleaf for inline role checks
- Use `fragments-role-guards.html` for complex or repeated role-gated blocks
- Check `FeatureFlags` (via model attribute or controller logic) for feature-level gating
- If a feature is unavailable for a role, show a disabled state or upgrade prompt — never a blank/broken UI

## CSS Architecture

- Add new styles to the most relevant file in `static/css/layout/`
- Only create a new CSS file for a large self-contained module
- No inline styles — ever
- Use Bootstrap utilities for spacing, colors, display
- Custom classes follow BEM: `.medicine-card__header`, `.pos-item--selected`
- CSS variables for theme values (defined in `base.css`)

## JavaScript Standards

- Core functionality must work without JS (progressive enhancement)
- Use event delegation for dynamically rendered content
- All `fetch` calls must handle errors and show user-friendly messages
- Prevent double-submit: disable the submit button and show a loading spinner on form submit
- Client-side validation before server submission

## Anti-Patterns — Never Do These

- ❌ Standalone HTML that doesn't extend `layout.html`
- ❌ Inline styles (`style="..."`)
- ❌ Hardcoded hex colors
- ❌ Delete without confirmation modal
- ❌ Missing empty state on list pages
- ❌ Missing loading state for async operations
- ❌ Showing raw exception messages or stack traces to users
- ❌ Inconsistent button styles across pages
- ❌ Breaking responsive layout on mobile
