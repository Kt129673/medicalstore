# GitHub Copilot Instructions

> **Note:** This is a reference file. For complete agent instructions, see [`AGENT_INSTRUCTIONS.md`](../AGENT_INSTRUCTIONS.md) in the root directory.

## Quick Reference

### Project Type
- Full-stack Medical Store Management System
- **Backend:** Spring Boot 3.5.6 (Java 17, Maven, H2 Database, JWT Auth)
- **Frontend:** Angular 15 (TypeScript, npm)

### Critical Rules
1. **Always check active terminals** before starting servers
2. **Never open new terminals** if servers are already running
3. **Always use SAME terminal** for restarts
4. **Verify directory** before running `npm start` or `mvn spring-boot:run`
5. **Backend on port 8080**, **Frontend on port 4200**

### File Structure
```
medicalstore/
├── AGENT_INSTRUCTIONS.md           ← READ THIS FIRST (Primary instructions)
├── medical-store-management-backend/
│   └── (Spring Boot application)
└── medical-store-management-frontend/
    └── (Angular application)
```

### When Code Changes
- **Java files (.java):** Auto-reloads with DevTools (no restart)
- **TypeScript/HTML/SCSS:** Auto-compiles (no restart)
- **Module imports in app.module.ts:** Restart Angular required
- **pom.xml changes:** Restart backend required

### Common Commands
```powershell
# Backend (from backend directory)
mvn spring-boot:run

# Frontend (from frontend directory)  
npm start
```

---

**For complete step-by-step instructions, workflows, error handling, and decision trees:**
👉 **See [`AGENT_INSTRUCTIONS.md`](../AGENT_INSTRUCTIONS.md)**
