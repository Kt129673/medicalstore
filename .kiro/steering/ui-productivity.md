# UI Productivity Guide — Fast, Correct UI Development

Never build from scratch. Every new page has a near-identical existing page — copy it, adapt it.

| Building a... | Copy from |
|---|---|
| List page | `templates/medicines/list.html` |
| Form (add/edit) | `templates/medicines/form.html` |
| Detail/view page | `templates/sales/view.html` |
| Report page | `templates/reports/daily.html` |
| Dashboard card | `templates/index.html` |

---

## File Locations

```
templates/
  layout.html                  ← Base layout — ALL pages extend this
  fragments.html               ← Reusable components (search bars, pagination, alerts)
  fragments-role-guards.html   ← Role-gated UI blocks

static/css/
  layout.css                   ← Entry point, imports all below
  layout/base.css              ← Reset, typography, CSS variables
  layout/components.css        ← Buttons, cards, badges, tables
  layout/sidebar.css           ← Sidebar nav
  layout/header.css            ← Top header
  layout/responsive.css        ← Mobile/tablet breakpoints
  layout/pos.css               ← POS-specific styles
  layout/medicines.css         ← Medicine list/form specific
  layout/enhancements.css      ← Animations, transitions
  layout/fixes.css             ← Targeted overrides

static/js/
  layout.js                    ← Global JS (sidebar toggle, alerts, form helpers)
```

Add new CSS to the most relevant existing file. Only create a new file for a large self-contained module.

---

## Page Template Skeleton

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/springsecurity6"
      th:replace="~{layout :: layout(~{::title}, ~{::main})}">
<head>
    <title>Page Title — MedicalStore</title>
</head>
<body>
<main>
    <!-- Page header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="mb-1">Page Title</h2>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a th:href="@{/}">Dashboard</a></li>
                    <li class="breadcrumb-item active">Page Title</li>
                </ol>
            </nav>
        </div>
        <!-- Action buttons -->
        <div>
            <a th:href="@{/path/new}" class="btn btn-primary">
                <i class="fas fa-plus me-1"></i> Add New
            </a>
        </div>
    </div>

    <!-- Flash messages -->
    <div th:if="${successMessage}" class="alert alert-success alert-dismissible fade show" role="alert">
        <span th:text="${successMessage}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <div th:if="${errorMessage}" class="alert alert-danger alert-dismissible fade show" role="alert">
        <span th:text="${errorMessage}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>

    <!-- Page content -->

</main>
</body>
</html>
```

---

## Standard Table Pattern

```html
<div class="card shadow-sm">
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-striped table-hover mb-0">
                <thead class="table-dark">
                    <tr>
                        <th>#</th>
                        <th>Name</th>
                        <th class="text-end">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="item, itemStat : ${items}">
                        <td th:text="${itemStat.count}"></td>
                        <td th:text="${item.name}"></td>
                        <td class="text-end">
                            <a th:href="@{/path/{id}(id=${item.id})}" class="btn btn-sm btn-outline-primary">
                                <i class="fas fa-eye"></i>
                            </a>
                            <a th:href="@{/path/{id}/edit(id=${item.id})}" class="btn btn-sm btn-outline-secondary">
                                <i class="fas fa-edit"></i>
                            </a>
                            <button class="btn btn-sm btn-outline-danger"
                                    data-bs-toggle="modal" data-bs-target="#deleteModal"
                                    th:attr="data-id=${item.id}, data-name=${item.name}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </td>
                    </tr>
                    <tr th:if="${#lists.isEmpty(items)}">
                        <td colspan="3" class="text-center py-5 text-muted">
                            <i class="fas fa-inbox fa-2x mb-2 d-block"></i>
                            No records found.
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>
```

---

## Standard Form Pattern

```html
<div class="card shadow-sm">
    <div class="card-header">
        <h5 class="mb-0">Form Title</h5>
    </div>
    <div class="card-body">
        <form th:action="@{/path/save}" th:object="${dto}" method="post" novalidate>
            <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>

            <div class="row g-3">
                <div class="col-md-6">
                    <label for="fieldName" class="form-label">Label <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" id="fieldName" th:field="*{fieldName}"
                           th:classappend="${#fields.hasErrors('fieldName')} ? 'is-invalid'"/>
                    <div class="invalid-feedback" th:errors="*{fieldName}"></div>
                </div>
            </div>

            <div class="mt-4 d-flex gap-2">
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save me-1"></i> Save
                </button>
                <a th:href="@{/path}" class="btn btn-secondary">
                    <i class="fas fa-times me-1"></i> Cancel
                </a>
            </div>
        </form>
    </div>
</div>
```

---

## Delete Confirmation Modal

Place once at the bottom of any list page that has delete actions:

```html
<div class="modal fade" id="deleteModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Confirm Delete</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                Are you sure you want to delete <strong id="deleteItemName"></strong>?
                This action cannot be undone.
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <form id="deleteForm" method="post">
                    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
                    <button type="submit" class="btn btn-danger">
                        <i class="fas fa-trash me-1"></i> Delete
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
document.getElementById('deleteModal').addEventListener('show.bs.modal', function(e) {
    const btn = e.relatedTarget;
    document.getElementById('deleteItemName').textContent = btn.dataset.name;
    document.getElementById('deleteForm').action = '/path/' + btn.dataset.id + '/delete';
});
</script>
```

---

## Flash Messages in Controllers

**IMPORTANT:** Use exactly these attribute names — the layout template listens for them:

```java
// ✅ Correct attribute names
redirectAttributes.addFlashAttribute("successMessage", "Saved successfully.");
redirectAttributes.addFlashAttribute("errorMessage", "Failed to save. Please try again.");

// ❌ Wrong — these won't render
redirectAttributes.addFlashAttribute("success", "...");
redirectAttributes.addFlashAttribute("error", "...");
```

---

## URL Constants — Always Use RoutePaths

```java
// ✅ Correct
return RoutePaths.redirectTo(RoutePaths.MEDICINES);   // "redirect:/medicines"

// ❌ Wrong
return "redirect:/medicines";
```

In Thymeleaf, always use `@{/path}` syntax — never string-concatenate URLs.

---

## Controller → Template Naming

| Controller returns | Template file |
|---|---|
| `"medicines/list"` | `templates/medicines/list.html` |
| `"medicines/form"` | `templates/medicines/form.html` |
| `"sales/view"` | `templates/sales/view.html` |

Model attributes: camelCase — `model.addAttribute("medicineList", ...)`, `model.addAttribute("currentPage", page)`.

---

## Role-Based Visibility (Thymeleaf)

```html
<!-- ADMIN only -->
<div sec:authorize="hasRole('ADMIN')">...</div>

<!-- OWNER or ADMIN -->
<div sec:authorize="hasAnyRole('OWNER', 'ADMIN')">...</div>

<!-- Not SHOPKEEPER -->
<div sec:authorize="!hasRole('SHOPKEEPER')">...</div>
```

For complex or repeated role blocks, use `fragments-role-guards.html` instead of inline checks.

---

## Common Fragments — Use, Don't Rebuild

Check `fragments.html` before writing any of these from scratch:
- Search/filter bar
- Pagination controls
- Status badges
- Export buttons (PDF/Excel)
- Empty state message

```html
<div th:replace="~{fragments :: search-bar}"></div>
<div th:replace="~{fragments :: pagination(${page})}"></div>
```

---

## Pre-Submit Checklist

- [ ] Page extends `layout.html` via `th:replace`
- [ ] CSRF token in all POST forms
- [ ] Flash messages use `successMessage` / `errorMessage` attribute names
- [ ] Empty state shown when list is empty
- [ ] Delete uses confirmation modal (never a direct link)
- [ ] No inline styles
- [ ] Responsive at 375px, 768px, 1280px
- [ ] Role guards applied where needed
- [ ] URLs use `RoutePaths` constants or `@{/...}` syntax
