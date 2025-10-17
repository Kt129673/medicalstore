# AI Agent Instructions for Medical Store Management Project

**Critical Rule:** Always check end-to-end integration for both frontend and backend. Do not prompt the user about frontend integration—assume it is required for every feature or change.

---

## 📁 Project Structure

```
medicalstore/
├── medical-store-management-backend/    # Spring Boot (Java 17, Maven)
│   ├── pom.xml                         # Backend dependencies
│   └── src/main/java/...
└── medical-store-management-frontend/   # Angular 15 (TypeScript, npm)
    ├── package.json                    # Frontend dependencies
    └── src/app/...
```

**Key Paths:**
- Backend: `d:\Projects\medicalstore\medical-store-management-backend`
- Frontend: `d:\Projects\medicalstore\medical-store-management-frontend`

---

## 🎯 STEP 1: Check Active Terminals (ALWAYS DO THIS FIRST)

Before starting any servers, **ALWAYS** check if terminals are already running:

### Check Backend (Spring Boot on Port 8080)
1. Look for terminal with `mvn spring-boot:run` or `java -jar`
2. Verify: Visit `http://localhost:8080/api/products` or check terminal output
3. If running: **REUSE IT** - don't open a new terminal

### Check Frontend (Angular on Port 4200)
1. Look for terminal with `npm start` or `ng serve`
2. Verify: Visit `http://localhost:4200` or check terminal output
3. If running: **REUSE IT** - don't open a new terminal

**Agent Rule:** Never open new terminals if servers are already running.

---

## 🚀 STEP 2: Starting Servers (First Time Only)

### Start Backend First
```powershell
cd d:\Projects\medicalstore\medical-store-management-backend
mvn spring-boot:run
```
**Expected Output:**
- `Started MedicalStoreManagementApplication in X seconds`
- `Tomcat started on port 8080`
- `Devtools property defaults active!`

### Start Frontend Second
```powershell
cd d:\Projects\medicalstore\medical-store-management-frontend
npm start
```
**Expected Output:**
- `√ Compiled successfully`
- `Angular Live Development Server is listening on localhost:4200`
- `Build at: [timestamp] - Hash: [hash]`

**Important:** Keep both terminals open for entire development session.

---

## 🔄 STEP 3: When to Restart vs Auto-Reload

### ✅ Auto-Reload (NO Restart Needed)

#### Backend (Spring Boot DevTools Active)
- ✅ Modify `.java` files (Controllers, Services, Models, etc.)
- ✅ Change business logic
- ✅ Update method implementations
- **Result:** DevTools auto-restarts in ~2-5 seconds

#### Frontend (Angular Watch Mode Active)
- ✅ Modify `.ts` files (Components, Services)
- ✅ Change `.html` templates
- ✅ Update `.scss` styles
- **Result:** Angular auto-recompiles in ~1-3 seconds

### ⚠️ Restart Required (In SAME Terminal)

#### Backend - Restart When:
1. `pom.xml` dependencies change
2. `application.properties` significant changes

**How to Restart Backend:**
```powershell
# In the backend terminal:
Ctrl+C  # Stop the server
mvn spring-boot:run  # Restart in SAME terminal
```

#### Frontend - Restart When:
1. `package.json` dependencies change (run `npm install` first)
2. New modules added to `app.module.ts` imports (e.g., `CommonModule`, `HttpClientModule`)
3. `angular.json` or `tsconfig.json` changes

**How to Restart Frontend:**
```powershell
# In the frontend terminal:
Ctrl+C  # Stop the server
npm start  # Restart in SAME terminal
```

**Critical:** Always use the **SAME terminal** - never open new terminals.

---

## 🌐 STEP 4: Port & Proxy Configuration

### Port Assignments
- **Backend:** `http://localhost:8080`
- **Frontend:** `http://localhost:4200`

### Proxy Configuration
- File: `medical-store-management-frontend/proxy.conf.json`
- All `/api/*` requests from frontend → auto-forwarded to `http://localhost:8080`
- **No CORS issues** because proxy handles it

### Verification Checklist
1. ✅ Backend: `http://localhost:8080/api/products` returns JSON or 401
2. ✅ Frontend: `http://localhost:4200` shows the application
3. ✅ Proxy: Frontend API calls go to backend without CORS errors

---

## 🐛 STEP 5: Common Errors & Solutions

### Error 1: "npm ERR! Missing script: 'start'"
**Cause:** Terminal is in wrong directory (not in frontend folder)

**Solution:**
```powershell
# DON'T: npm start  (when in d:\Projects\medicalstore)
# DO: Navigate first
cd d:\Projects\medicalstore\medical-store-management-frontend
npm start

# OR use combined command:
cd d:\Projects\medicalstore\medical-store-management-frontend; npm start
```

### Error 2: "Port 4200 is already in use"
**Cause:** Angular is already running in another terminal

**Solution:**
1. Find the terminal where Angular is running
2. **REUSE that terminal** - don't start a new server
3. Make your code changes - Angular will auto-reload
4. Only restart if absolutely necessary (Ctrl+C, then `npm start`)

### Error 3: "Port 8080 is already in use"
**Cause:** Spring Boot is already running

**Solution:**
1. Find the terminal where Spring Boot is running
2. **REUSE that terminal** - don't start a new server
3. Make your code changes - DevTools will auto-restart

### Error 4: Blank Page or "��<" in Browser
**Causes:**
- Angular compilation errors (check terminal)
- Browser caching issue
- Simple Browser rendering issue

**Solutions:**
1. Check Angular terminal for compilation errors
2. Hard refresh: `Ctrl+Shift+R` or `Ctrl+F5`
3. Clear browser cache
4. Use external browser instead of Simple Browser
5. Check browser console (F12) for JavaScript errors

---

## 🔍 STEP 6: Debugging with Simple Browser

### Agent Workflow for Debugging
1. **Open Simple Browser:** Use `open_simple_browser` tool with `http://localhost:4200`
2. **Request Feedback:** Ask user "What do you see in the Simple Browser?"
3. **Get User Response:** User describes (login form / blank page / error / symbols)
4. **Analyze Issue:** Based on response, diagnose the problem
5. **Make Fixes:** Edit code files to fix the issue
6. **Verify:** Ask user to check Simple Browser again (auto-reloads)

### User Manual Debugging
```powershell
# Open Simple Browser manually
Ctrl+Shift+P → "Simple Browser: Show" → http://localhost:4200

# Inspect in Simple Browser
Right-click → "Open Browser Console"
- Console tab: JavaScript errors
- Network tab: File loading status
- Elements tab: HTML structure
```

### Simple Browser vs External Browser
- **Simple Browser:** Quick debugging with agent, side-by-side view, no window switching
- **External Browser:** Full feature testing, responsive design, production testing

---

## 📋 STEP 7: Agent Decision Tree

```
User Request Received
├─ Is it a code change?
│  ├─ Backend Java file? → Save file → DevTools auto-restarts ✅
│  ├─ Frontend TS/HTML/SCSS? → Save file → Angular auto-compiles ✅
│  ├─ New module in app.module.ts? → Save file → Restart Angular ⚠️
│  └─ pom.xml change? → Save file → Restart Backend ⚠️
│
├─ User reports error?
│  ├─ "Missing script: start" → Check directory → Use `cd .../frontend; npm start`
│  ├─ "Port already in use" → Find existing terminal → Reuse it
│  ├─ "Blank page" → Open Simple Browser → Request user feedback
│  └─ Compilation error → Read terminal output → Fix code
│
└─ User asks to run servers?
   ├─ Check if already running (STEP 1)
   │  ├─ Running? → Inform user, no action needed ✅
   │  └─ Not running? → Start servers (STEP 2)
   └─ Never open duplicate terminals ⚠️
```

---

## ✅ STEP 8: Pre-Action Checklist for Agent

Before executing any command, verify:

1. **Check Active Terminals:** Are backend/frontend already running?
2. **Verify Directory:** Is the command being run in the correct folder?
3. **Assess Change Type:** Does this change require restart or auto-reload?
4. **Use Correct Terminal:** Reusing existing terminal or creating new one?
5. **Confirm Tool Usage:** Is `open_simple_browser` appropriate for this debugging task?

---

## 🎓 STEP 9: Agent Learning Points

### Terminal Management
- **Never** open new terminals if servers are running
- **Always** check active terminals first using `terminal_last_command` or `get_terminal_output`
- **Reuse** existing terminals for restart operations
- **Navigate** to correct directory before running `npm start` or `mvn spring-boot:run`

### Auto-Reload Understanding
- Spring Boot DevTools: Auto-restarts on `.java` file changes
- Angular Watch Mode: Auto-compiles on `.ts/.html/.scss` changes
- Module/Config changes: Require manual restart

### Debugging Approach
- Use Simple Browser for visual feedback
- Request user input for what they see
- Check terminal output for compilation errors
- Verify ports and proxy configuration

---

## 🤝 Collaboration Guidelines

- Use clear, descriptive commit messages
- For issues/bugs, provide steps to reproduce and screenshots if possible
- Pull requests should be linked to an open issue (if applicable) and include a summary of changes
- Use Discussions for general questions or suggestions
- Follow the project's code style and contribution guidelines
- Be respectful and constructive in all interactions
- For setup or running instructions, see the project README
- If you need help, mention me or open a new discussion!

Happy coding! 🚀

## ⚠️ IMPORTANT: Terminal Management & Hot Reload

### Terminal Reuse Policy
- **NEVER open new terminals** for frontend or backend if they are already running.
- **Reuse existing terminals** for subsequent requests—both Angular and Spring Boot support automatic recompilation.
- **ALWAYS check active terminals** before starting new processes.
- **Verify backend port 8080** is accessible before making frontend changes.
- **Test API endpoints** (`http://localhost:8080/api/...`) to confirm backend is responsive.

### Frontend (Angular) - Auto-Compilation
- Angular CLI (`ng serve`) **automatically watches** for file changes and recompiles instantly.
- No need to restart or recompile manually after code changes.
- Just save your file—the browser will auto-refresh.
- **Exception: When adding NEW modules or imports** (e.g., `BrowserAnimationsModule`, `CommonModule`):
  - Stop the Angular server (Ctrl+C in the terminal)
  - Restart using the **SAME terminal**: `npm start`
  - This ensures the new module is properly loaded into the Angular runtime

### Backend (Spring Boot) - Auto-Compilation with DevTools
- **Spring Boot DevTools** is added to `pom.xml` for automatic restart on code changes.
- When you modify Java files, Spring Boot **automatically restarts** the application context.
- No need to stop and restart the server manually.
- DevTools triggers faster restarts by reloading only changed classes.

### Backend Port & Frontend Communication
- **Backend runs on port 8080** (default Spring Boot port)
- **Frontend runs on port 4200** (default Angular port)
- **Frontend proxy** (`proxy.conf.json`) forwards all `/api/*` requests to `http://localhost:8080`
- **Always verify backend is running** before testing frontend features
- **Check backend health**: Open `http://localhost:8080/api/products` or `http://localhost:8080/h2-console`

### How It Works
1. **First Request:** Start both servers in separate terminals (once).
2. **Subsequent Requests:** Make code changes—both servers auto-reload.
3. **No manual compilation or restart needed** unless:
   - Backend: `pom.xml` dependencies change
   - Frontend: `package.json` dependencies change OR new modules imported in `app.module.ts`
4. **When restart is needed:** Use Ctrl+C to stop, then restart in the **SAME terminal**
5. **Frontend automatically calls backend** through proxy—no CORS issues.

### Example (Manual - First Time Only)

1. Open two terminals (reuse them for all future requests).
2. In the first terminal:
	- Navigate to the backend folder: `cd medical-store-management-backend`
	- Run: `mvnw spring-boot:run` (or `mvn spring-boot:run`)
	- **DevTools will auto-restart** on any Java file changes
3. In the second terminal:
	- Navigate to the frontend folder: `cd medical-store-management-frontend`
	- Run: `npm start` or `npx ng serve --open`
	- **Angular auto-compiles** on any TypeScript/HTML/SCSS changes

**Important:** Keep both terminals running. Restart them in the **SAME terminal** only when:
- **Backend:** Dependencies added/removed in `pom.xml`
- **Frontend:** 
  - Dependencies added/removed in `package.json` (requires `npm install` first)
  - New modules imported in `app.module.ts` (e.g., `CommonModule`, `FormsModule`, `BrowserAnimationsModule`)

### Port Verification Checklist
Before making any code changes, verify:
1. ✅ **Backend running on port 8080**: Check terminal output or visit `http://localhost:8080/api/products`
2. ✅ **Frontend running on port 4200**: Check terminal output or visit `http://localhost:4200`
3. ✅ **Proxy configured**: Ensure `proxy.conf.json` exists in frontend root with target `http://localhost:8080`
4. ✅ **Angular CLI using proxy**: Run with `ng serve --proxy-config proxy.conf.json` (or `npm start` which includes this)

### When to Restart Angular (in SAME terminal)
- ❌ **DO NOT restart** for: Component changes, Service changes, HTML/SCSS changes, TypeScript code changes
- ✅ **DO restart** for: 
  - Adding new modules to `app.module.ts` imports array (e.g., `CommonModule`, `HttpClientModule`, `BrowserAnimationsModule`)
  - Adding new packages to `package.json` (run `npm install` first, then restart)
  - Changing Angular configuration files (`angular.json`, `tsconfig.json`)
- 💡 **How to restart**: Press Ctrl+C in the existing Angular terminal, then run `npm start` in the same terminal

### ⚠️ Common Terminal Errors

#### Error: "npm ERR! Missing script: 'start'"
**Cause:** Terminal is not in the frontend directory (wrong working directory)

**Solution:**
1. Make sure you're in the correct directory: `cd d:\Projects\medicalstore\medical-store-management-frontend`
2. Then run: `npm start`
3. Or use combined command: `cd d:\Projects\medicalstore\medical-store-management-frontend; npm start`

**Why this happens:** 
- If terminal is in root `d:\Projects\medicalstore`, it won't find `package.json`
- `npm start` only works when you're inside the folder with `package.json`

#### Error: "Port 4200 is already in use"
**Cause:** Angular is already running in another terminal

**Solution:**
1. Don't start a new server - reuse the existing one
2. Find the terminal where Angular is running
3. Make your code changes - Angular will auto-reload
4. If you must restart: Stop the existing server (Ctrl+C) in its terminal, then `npm start`

## 🔍 Debugging with VS Code Simple Browser

### How Agent Uses Simple Browser for Debugging
The AI agent can open and preview your application directly inside VS Code using the **Simple Browser** feature:

1. **Agent opens Simple Browser**: Using `open_simple_browser` tool with URL `http://localhost:4200`
2. **Simple Browser panel appears**: A new panel/tab opens inside VS Code (usually on the right side)
3. **Agent can request information**: Agent asks you what you see in the Simple Browser
4. **You provide feedback**: Describe what's displayed (login form, blank page, error message, etc.)
5. **Agent debugs based on feedback**: Makes fixes and can reload the Simple Browser to verify

### How You Can Debug with Simple Browser
1. **Open Simple Browser manually**:
   - Press `Ctrl+Shift+P` (Command Palette)
   - Type: `Simple Browser: Show`
   - Enter URL: `http://localhost:4200`

2. **Inspect elements in Simple Browser**:
   - Right-click in Simple Browser → **"Open Browser Console"**
   - Check Console tab for JavaScript errors
   - Check Network tab to see if files are loading
   - View page source to verify HTML structure

3. **Benefits of Simple Browser**:
   - ✅ No need to switch between VS Code and external browser
   - ✅ Live reload works automatically when code changes
   - ✅ Easier for agent to request visual feedback
   - ✅ Side-by-side view of code and running app

4. **Refresh Simple Browser**:
   - Click refresh button in Simple Browser toolbar
   - Or press `Ctrl+R` while focused on Simple Browser

### Debugging Workflow with Agent
```
User: "Getting blank page"
  ↓
Agent: Opens Simple Browser at http://localhost:4200
  ↓
Agent: "What do you see in the Simple Browser panel?"
  ↓
User: "I see [blank page/error/login form]"
  ↓
Agent: Analyzes issue, makes code fixes
  ↓
Agent: "Check Simple Browser again - it should auto-reload"
  ↓
User: "Now I see the login form!" ✅
```

### Simple Browser vs External Browser
- **Use Simple Browser when**: Working with agent for debugging, quick previews, iterative development
- **Use External Browser when**: Testing full browser features, checking responsive design, production testing

### Example (Automated with VS Code Tasks)

- Configure `.vscode/tasks.json` to run both servers with one command.
- Or use the `concurrently` npm package in the frontend to run both commands from a single script.

This approach saves time and ensures both parts of your application are always running together for development and testing.

## Collaboration Guidelines

- Use clear, descriptive commit messages.
- For issues/bugs, provide steps to reproduce and screenshots if possible.
- Pull requests should be linked to an open issue (if applicable) and include a summary of changes.
- Use Discussions for general questions or suggestions.
- Follow the project’s code style and contribution guidelines.
- Be respectful and constructive in all interactions.
- For setup or running instructions, see the project README.
- If you need help, mention me or open a new discussion!

Happy coding! 🚀
