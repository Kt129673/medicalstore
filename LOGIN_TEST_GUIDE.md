# Login Functionality Test Guide

## ✅ System Status
- **Backend:** Running on http://localhost:8080
- **Frontend:** Running on http://localhost:4200
- **Database:** H2 In-Memory (auto-configured)

## 🧪 How to Test Login/Logout

### Step 1: Access the Application
1. Open your browser
2. Navigate to: **http://localhost:4200**
3. You should see the **Medical Store Management** login form

### Step 2: Register a New User
1. Enter a username (e.g., `admin`)
2. Enter a password (minimum 6 characters, e.g., `password123`)
3. Click the **"Register"** button
4. Wait for confirmation (user will be auto-logged in after registration)

### Step 3: Manual Login Test
1. If not auto-logged in, enter your credentials:
   - Username: `admin`
   - Password: `password123`
2. Click the **"Login"** button
3. Check browser console (F12) for:
   - Success message
   - JWT token stored in localStorage
   - Redirect attempt to `/dashboard`

### Step 4: Verify Authentication
Open browser Developer Tools (F12) and check:

**Console Tab:**
```
Login successful
```

**Application Tab → Local Storage → http://localhost:4200:**
```
auth_token: eyJhbGciOiJIUzUxMiJ9... (your JWT token)
```

**Network Tab:**
- POST request to `/api/auth/login` should return status 200
- Response body should contain: `{ "token": "..." }`

### Step 5: Test Protected API
With the token stored, test a protected endpoint:
```javascript
// Open browser console and run:
fetch('http://localhost:8080/api/products', {
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('auth_token')
  }
}).then(r => r.json()).then(console.log)
```

### Step 6: Test Logout
```javascript
// In browser console:
localStorage.removeItem('auth_token')
console.log('Logged out - token removed')
```

## 🔍 Troubleshooting

### Blank Page / Login Not Showing
1. Clear browser cache (Ctrl+Shift+Delete)
2. Hard refresh (Ctrl+F5)
3. Try incognito/private window
4. Check browser console for errors

### "401 Unauthorized" Error
- Check if backend is running on port 8080
- Verify JWT token is in localStorage
- Check token expiration (default: 1 hour)

### "CORS" Error
- Backend should allow `/api/auth/**` without authentication
- Frontend proxy should forward `/api/*` to `http://localhost:8080`
- Check `proxy.conf.json` exists and is configured

### Backend Not Starting
- Check if port 8080 is already in use
- Look for circular dependency errors (should be fixed)
- Verify H2 database configuration in `application.properties`

### Frontend Not Starting
- Run `npm install` if dependencies are missing
- Check if port 4200 is available
- Verify `app.component.html` contains only `<router-outlet></router-outlet>`

## 📊 Expected API Endpoints

### Public Endpoints (No Auth Required)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/logout` - Logout (client-side)
- `GET /h2-console` - H2 Database Console

### Protected Endpoints (Auth Required)
- `GET /api/products` - List all products
- `POST /api/products` - Create new product
- `GET /api/products/{id}` - Get product by ID
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

## 🎯 Success Criteria

✅ **Login Form Displays** - Professional UI with username/password fields  
✅ **Registration Works** - New users can register successfully  
✅ **Login Works** - JWT token received and stored  
✅ **Token Auto-Attached** - All API requests include Authorization header  
✅ **Protected APIs Work** - Can access `/api/products` with valid token  
✅ **Logout Works** - Token removed from localStorage  

## 🚀 Next Steps After Login Works

1. Create Dashboard component for post-login landing page
2. Add Auth Guard to protect routes
3. Implement token refresh mechanism
4. Add role-based access control (RBAC)
5. Create Product Management UI
6. Add user profile management
7. Implement session timeout handling

---

**Test Date:** October 17, 2025  
**Status:** ✅ Ready for Testing
