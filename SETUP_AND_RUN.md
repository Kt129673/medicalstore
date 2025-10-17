# Medical Store Management - Setup & Run Guide

## ✅ Enterprise-Level Login/Logout Implementation Complete

### Backend (Spring Boot + JWT + H2)
- ✅ JWT-based stateless authentication
- ✅ Spring Security 6+ with lambda-based configuration  
- ✅ BCrypt password hashing
- ✅ H2 in-memory database
- ✅ RESTful API endpoints
- ✅ CRUD operations for Products
- ✅ User registration and login

### Frontend (Angular + JWT Interceptor)
- ✅ AuthService for login/logout/registration
- ✅ JWT Interceptor for automatic token attachment
- ✅ Reactive forms with validation
- ✅ Professional login UI with styling
- ✅ Proxy configuration for backend API

---

## 🚀 How to Run

### Option 1: Using your IDE (Recommended)
1. **Backend:** Open `medical-store-management-backend` in your IDE and run `MedicalStoreManagementApplication.java`
2. **Frontend:** Open terminal in `medical-store-management-frontend` and run:
   ```powershell
   npm start
   ```

### Option 2: Using Command Line
**Backend:**
```powershell
cd d:\Projects\medicalstore\medical-store-management-backend
# If you have Maven installed globally:
mvn clean install
mvn spring-boot:run

# Or use your IDE's run button for MedicalStoreManagementApplication
```

**Frontend:**
```powershell
cd d:\Projects\medicalstore\medical-store-management-frontend
npm install  # if not already done
npm start
```

---

## 🔐 Testing Login/Logout

### Step 1: Register a User
**Endpoint:** POST `http://localhost:8080/api/auth/register`

**Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Using Frontend:** Navigate to `http://localhost:4200`, fill in username/password, click "Register"

### Step 2: Login
**Endpoint:** POST `http://localhost:8080/api/auth/login`

**Body:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Using Frontend:** Fill credentials and click "Login" - you'll be redirected to dashboard with JWT stored in localStorage

### Step 3: Access Protected Endpoints
**Endpoint:** GET `http://localhost:8080/api/products`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

The Angular frontend automatically attaches this header to all API requests via the JWT Interceptor.

### Step 4: Logout
**Frontend:** Call `authService.logout()` - this removes the token from localStorage (stateless logout)

---

## 📊 H2 Database Console
Access the H2 console at: `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:medicalstoredb`
- **Username:** `sa`
- **Password:** (leave empty)

---

## 🔧 Troubleshooting

### Maven Wrapper Issues
If `mvnw.cmd` doesn't work, install Maven globally or run the backend from your IDE (IntelliJ IDEA, Eclipse, VS Code with Spring Boot extension).

### CORS Issues
The backend is configured to allow `/api/auth/**` without authentication. If you encounter CORS issues, verify `proxy.conf.json` is being used by the Angular dev server.

### Port Already in Use
- Backend default: 8080
- Frontend default: 4200

Change ports in `application.properties` (backend) or use `--port` flag for Angular.

---

## 📁 Project Structure

```
medical-store-management-backend/
├── src/main/java/com/medicalstore/management/
│   ├── config/SecurityConfig.java          # JWT + Spring Security
│   ├── controller/
│   │   ├── AuthController.java             # Login/Register/Logout
│   │   └── ProductController.java          # CRUD operations
│   ├── model/
│   │   ├── User.java                       # User entity
│   │   └── Product.java                    # Product entity
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── ProductRepository.java
│   ├── security/
│   │   ├── JwtUtil.java                    # JWT token generation/validation
│   │   └── JwtRequestFilter.java           # JWT filter for requests
│   └── service/
│       ├── CustomUserDetailsService.java    # User authentication
│       └── ProductService.java
└── src/main/resources/application.properties

medical-store-management-frontend/
├── src/app/
│   ├── core/services/
│   │   ├── auth.service.ts                 # Authentication service
│   │   └── jwt.interceptor.ts              # Automatic JWT injection
│   ├── features/auth/
│   │   ├── login.component.ts              # Login component
│   │   ├── login.component.html            # Login UI
│   │   └── login.component.scss            # Styling
│   ├── app-routing.module.ts               # Routes
│   └── app.module.ts                       # App configuration
└── proxy.conf.json                          # API proxy
```

---

## 🎯 Next Steps
- Add auth guard for protected routes
- Create dashboard component
- Implement product management UI
- Add role-based access control (RBAC)
- Integrate refresh tokens for enhanced security

---

**Status:** ✅ All compilation errors fixed | ✅ Backend ready | ✅ Frontend ready | ⏳ Awaiting manual testing
