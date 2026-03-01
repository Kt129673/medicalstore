# 🔌 API Permissions Matrix & Endpoint Documentation

> Complete reference for all API endpoints with role-based access control

---

## API Overview

All APIs are protected by Spring Security `@PreAuthorize` decorators.

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
- **Type:** Session-based (Form Login)
- **Session Header:** `JSESSIONID` (automatic in cookies)
- **CSRF Token:** Required for POST/PUT/DELETE (automatic with forms)

---

## Authentication API

### Login
```http
POST /login
Content-Type: application/x-www-form-urlencoded

username=admin&password=admin123&remember-me=on
```

**Response (Redirect):**
- **ADMIN** → `/admin`
- **OWNER** → `/owner`
- **SHOPKEEPER** → `/`

---

## Dashboard API

### Get Dashboard KPIs
```http
GET /api/v1/dashboard/kpis
Authorization: Bearer {session}
```

| Role | Scope | Permission |
|------|-------|-----------|
| ADMIN | Platform-wide | Allowed ✅ |
| OWNER | Their branches | Allowed ✅ |
| SHOPKEEPER | Their branch | Allowed ✅ |

**Response:**
```json
{
    "todaySales": 15000.50,
    "monthlyRevenue": 450000.00,
    "totalCustomers": 234,
    "totalMedicines": 567,
    "lowStockCount": 12,
    "expiringIn30": 5
}
```

---

## Inventory API (Medicines)

### List Medicines
```http
GET /medicines
Authorization: Bearer {session}
```

| Role | Access |
|------|--------|
| ADMIN | All medicines, all branches |
| SHOPKEEPER | Their branch only |
| OWNER | ❌ Denied |

### Search Medicines (POS)
```http
GET /api/v1/medicines/search?q=aspirin
Authorization: Bearer {session}
```

| Role | Access |
|------|--------|
| ADMIN | ✅ |
| SHOPKEEPER | ✅ |
| OWNER | ❌ Denied |

### Create Medicine
```http
POST /medicines/save
Content-Type: application/json
Authorization: Bearer {session}

{
    "name": "Aspirin 500mg",
    "category": "Pain Relief",
    "supplier": 1,
    "quantity": 100,
    "price": 5.50,
    "expiryDate": "2025-12-31"
}
```

| Role | Access | Audit Log |
|------|--------|-----------|
| ADMIN | ✅ | Yes |
| SHOPKEEPER | ✅ | Yes |
| OWNER | ❌ | ESCALATION_ATTEMPT |

---

## Sales API

### List Sales
```http
GET /sales?page=0&size=15
Authorization: Bearer {session}
```

| Role | View |
|------|------|
| ADMIN | All sales from all branches |
| SHOPKEEPER | Their branch only |
| OWNER | ❌ Denied |

**Query Parameters:**
- `page` — 0-indexed page number
- `size` — Items per page
- `search` — Customer name/phone
- `startDate` — YYYY-MM-DD
- `endDate` — YYYY-MM-DD
- `paymentMethod` — CASH, CARD, CHECK

### Create Sale
```http
POST /sales/save
Content-Type: application/json
Authorization: Bearer {session}

{
    "customer": {
        "name": "John Doe",
        "phone": "9876543210",
        "address": "123 Main St"
    },
    "items": [
        {
            "medicineId": 1,
            "quantity": 5,
            "price": 10.00
        }
    ],
    "paymentMethod": "CASH",
    "discountPercentage": 5,
    "gstPercentage": 5
}
```

| Role | Access |
|------|--------|
| ADMIN | ✅ |
| SHOPKEEPER | ✅ (auto-scoped to their branch) |
| OWNER | ❌ Denied |

**Returns:** Sale #ID, Invoice PDF link

### Delete Sale
```http
POST /sales/delete/{id}
Authorization: Bearer {session}
```

| Role | Access | Side Effects |
|------|--------|--------------|
| ADMIN | ✅ | Restores stock |
| SHOPKEEPER | ✅ | Restores stock |
| OWNER | ❌ | — |

---

## Customers API

### List Customers
```http
GET /customers
Authorization: Bearer {session}
```

| Role | Access |
|------|--------|
| ADMIN | All customers |
| SHOPKEEPER | Their branch only |
| OWNER | ❌ Blocked |

### Create Customer
```http
POST /customers/save
Content-Type: application/json
Authorization: Bearer {session}

{
    "name": "John Doe",
    "phone": "9876543210",
    "email": "john@example.com",
    "address": "123 Main St",
    "city": "New York",
    "gstin": "36AABCU9603R1Z5"
}
```

| Role | Access |
|------|--------|
| ADMIN | ✅ |
| SHOPKEEPER | ✅ (store branch) |
| OWNER | ❌ |

---

## Admin API

### List Users
```http
GET /admin/users
Authorization: Bearer {session}
```

**Access Control:**
```
@PreAuthorize("hasRole('ADMIN')")
```

**Response:**
```json
{
    "users": [
        {
            "id": 1,
            "username": "admin",
            "fullName": "System Admin",
            "roles": ["ADMIN"],
            "enabled": true,
            "lastLogin": "2026-03-01T14:23:45"
        }
    ]
}
```

### Create User
```http
POST /admin/users/create
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer {session}

username=john_owner&password=SecurePass123&fullName=John Owner&email=john@example.com&role=OWNER
```

**Access Control:**
```
@PreAuthorize("hasRole('ADMIN')")
```

**Audit Log:**
```
AUDIT_LOG | User: admin | Roles: ADMIN | Action: USER_CREATED | Description: Created user 'john_owner' with role 'OWNER'
```

### Reset User Password
```http
POST /admin/users/reset-password/{id}
Authorization: Bearer {session}

newPassword=NewSecurePass456
```

**Access Control:**
```
@PreAuthorize("hasRole('ADMIN')")
```

**Audit Log:**
```
AUDIT_LOG | User: admin | Roles: ADMIN | Action: PASSWORD_RESET_BY_ADMIN | Description: Reset password for user: john_owner
```

### Edit User
```http
POST /admin/users/edit/{id}
Authorization: Bearer {session}

fullName=John Owner Updated&email=john.new@example.com&enabled=true
```

**Access Control:**
```
@PreAuthorize("hasRole('ADMIN')")
```

---

## Owner API

### Owner Dashboard
```http
GET /owner
Authorization: Bearer {session}
```

**Access Control:**
```
@PreAuthorize("hasRole('OWNER')")
```

Returns:
- Aggregated KPIs across all owned branches
- List of owned branches
- Shopkeeper count

### Compare Branches
```http
GET /owner/compare
Authorization: Bearer {session}
```

**Access Control:**
```
@PreAuthorize("hasRole('OWNER')")
```

Returns:
- Branch-by-branch comparison data for charts
- Revenue comparison
- Medicine count comparison
- Low stock comparison

### Manage Shopkeepers
```http
GET /owner/shopkeepers
Authorization: Bearer {session}
```

**Access Control:**
```
@PreAuthorize("hasRole('OWNER')")
```

Returns: Shopkeepers assigned to owner's branches only

### View Subscription
```http
GET /owner/subscription
Authorization: Bearer {session}
```

**Access Control:**
```
@PreAuthorize("hasRole('OWNER')")
```

Returns:
- Current plan (FREE, PRO, ENTERPRISE)
- Plan expiry date
- Usage statistics
- Upgrade options

---

## Reports API

### Sales Report
```http
GET /reports/sales?reportType=Daily&reportDate=2026-03-01
Authorization: Bearer {session}
```

**Access Control:**
```
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'SHOPKEEPER')")
```

**Data Scoping:**
- ADMIN → Global sales
- OWNER → Their branches only
- SHOPKEEPER → Their branch only

### Profit & Loss Report
```http
GET /reports/profit-loss?month=2026-03&year=2026
Authorization: Bearer {session}
```

**Access Control:** All 3 roles (scoped)

### GST Report
```http
GET /reports/gst?month=2026-03&year=2026
Authorization: Bearer {session}
```

**Access Control:** All 3 roles (scoped)

### Export Report
```http
GET /reports/{report_type}/export?format=CSV
Authorization: Bearer {session}
```

| Role | Format | Access |
|------|--------|--------|
| ADMIN | CSV, PDF | ✅ |
| OWNER | CSV, PDF | ✅ |
| SHOPKEEPER | CSV only | ❌ PDF |

---

## Profile API

### Change Password
```http
POST /profile/change-password
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer {session}

currentPassword=OldPass123&newPassword=NewPass456&confirmPassword=NewPass456
```

**Access Control:**
```
@PostMapping
// All authenticated roles
```

**Validation:**
- Current password must match
- New password ≥ 6 characters
- New password must match confirmation

---

## Error Responses

### 403 Forbidden
```json
{
    "status": 403,
    "error": "Forbidden",
    "message": "You don't have permission to access this resource"
}
```

### 401 Unauthorized
```json
{
    "status": 401,
    "error": "Unauthorized",
    "message": "Please log in to continue"
}
```

### 404 Not Found
```json
{
    "status": 404,
    "error": "Not Found",
    "message": "Resource not found"
}
```

---

## Rate Limiting & Quotas

| Feature | ADMIN | OWNER | SHOPKEEPER |
|---------|:-----:|:-----:|:----------:|
| API calls/hour | Unlimited | 1000 | 500 |
| Bulk export | Allowed | Allowed | Not allowed |
| User creation | Allowed | Not allowed | Not allowed |
| Report generation | Unlimited | Per-branch | Per-branch |

---

## Error Audit Trail

All access denials are logged:

```
AUDIT_DENIED | User: owner_1 | Roles: OWNER | Attempted: /admin/users | Reason: Forbidden - insufficient permissions
```

View logs:
```bash
grep "AUDIT_DENIED" logs/application.log
```

---

**Last Updated:** March 1, 2026  
**API Version:** 1.0
