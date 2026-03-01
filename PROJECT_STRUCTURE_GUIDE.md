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
│   ├── layout.css        # manifest that imports layout modules
│   └── layout/
│       ├── base.css
│       ├── sidebar.css
│       ├── header.css
│       ├── components.css
│       └── responsive.css
└── js/
    └── layout.js         # shared application layout behavior
```

## Reusability rules

1. Keep route path strings in one place (`config/RoutePaths.java`).
2. Keep current-user / owner resolution in one place (`util/SecurityUtils.java`).
3. Keep subscription warnings and global counters in one place (`config/GlobalModelAttribute.java`).
4. Controllers should call services; they should not duplicate role/owner resolution logic.
5. New shared UI pieces should go in `templates/fragments.html`.
6. Layout-wide CSS should be updated in `static/css/layout/*.css` (keep import order in `static/css/layout.css`).
7. Layout-wide JS changes should be done in `static/js/layout.js`.

## Single-change policy

When adding or changing behavior, update shared classes first:
- Routing/public URL behavior: `RoutePaths`
- Current owner/user resolution: `SecurityUtils`
- Global template model attributes: `GlobalModelAttribute`

This keeps future modifications centralized and reduces repeated edits.
