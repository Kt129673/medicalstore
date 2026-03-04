# Project Structure Guide (Maintainable + Reusable)

This structure keeps shared behavior in one place so future updates need only one change.

## Backend package structure

```
com.medicalstore
├── config/        # app configuration, interceptors, global model attributes, shared route constants
├── controller/    # web and API controllers only
│   └── api/
├── dto/           # request/response transfer objects
├── model/         # JPA entities
├── repository/    # Spring Data repositories
├── service/       # business logic
└── util/          # security and helper utilities
```

## Frontend/template structure

```
src/main/resources/templates
├── layout.html           # main reusable app layout
├── fragments.html        # reusable UI blocks (header, search, empty states)
├── login.html            # auth page
└── <feature folders>/    # admin, analytics, customers, medicines, etc.
```

## Frontend static assets structure

```
src/main/resources/static
├── css/
│   ├── layout.css        # manifest that imports layout modules (in order)
│   └── layout/
│       ├── base.css       # CSS variables (design tokens), reset, global typography
│       ├── sidebar.css    # sidebar, nav links, nav groups
│       ├── header.css     # top header, breadcrumb, search, notifications, clock
│       ├── components.css # shared component library:
│       │                  #   ent-badge, ent-action-row, ent-search, ent-table-toolbar,
│       │                  #   ent-bulk-bar, ent-sortable, ent-filter-chip-bar, ent-skeleton,
│       │                  #   ent-form-section, ent-alert, ent-kpi, ent-row-actions,
│       │                  #   ent-tooltip, ent-shortcuts-dialog, ent-pagination,
│       │                  #   ent-export-btn, ent-dirty-indicator
│       └── responsive.css # media queries + print rules for all enterprise components
└── js/
    └── layout.js          # shared application layout behaviour + enterprise utilities:
                           #   entExportCSV(), entExportMenu(), entInitTableSort(),
                           #   entInitBulkSelect(), entGetSelectedIds(), entClearBulkSelect(),
                           #   entBulkDelete(), entInitFormDirty(), entCharCounter(),
                           #   entConfirmModal(), entToggleDark()
                           #   Keyboard shortcuts helper (press '?' anywhere)
                           #   Auto-dismiss flash alerts (5 s)
```

## Reusability rules

1. Keep route path strings in one place (`config/RoutePaths.java`).
2. Keep current-user / owner resolution in one place (`util/SecurityUtils.java`).
3. Keep subscription warnings and global counters in one place (`config/GlobalModelAttribute.java`).
4. Controllers should call services; they should not duplicate role/owner resolution logic.
5. New shared UI pieces should go in `templates/fragments.html`.
6. Layout-wide CSS should be updated in `static/css/layout/*.css` (keep import order in `static/css/layout.css`).
7. Layout-wide JS changes should be done in `static/js/layout.js`.
8. Use enterprise fragments for new list pages:
   - `ent-action-row`     instead of a custom search+add bar.
   - `ent-table-toolbar`  instead of a custom row-count label.
   - `ent-bulk-bar`       + `entInitBulkSelect()` for multi-select actions.
   - `ent-pagination-bar` instead of custom pagination HTML.
   - `ent-kpi-card`       for dashboard stat tiles.
   - `ent-status-badge`   instead of inline `<span class="badge ...">`.
   - `ent-alert-banner`   instead of Bootstrap alert divs inside page templates.
   - `ent-form-section`   to divide long forms into named groups.
9. Use CSS design tokens (`var(--status-active-bg)`, `var(--shadow-md)`, etc.) for all new inline colours — never hard-code hex values in templates.
10. Call `entInitTableSort('tableId')` to enable column sorting with zero extra code.

## Single-change policy

When adding or changing behavior, update shared classes first:
- Routing/public URL behavior: `RoutePaths`
- Current owner/user resolution: `SecurityUtils`
- Global template model attributes: `GlobalModelAttribute`

This keeps future modifications centralized and reduces repeated edits.
