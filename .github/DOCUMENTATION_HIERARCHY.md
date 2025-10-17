# Documentation File Hierarchy

This document explains the naming convention and priority order for documentation files in this project.

## 📋 File Naming Convention Priority

### 1️⃣ **AGENT_INSTRUCTIONS.md** (ROOT) - HIGHEST PRIORITY
- **Location:** `d:\Projects\medicalstore\AGENT_INSTRUCTIONS.md`
- **Purpose:** Primary instruction set for AI agents
- **Read By:** GitHub Copilot, Claude, ChatGPT, and all AI assistants
- **Contains:** 
  - Step-by-step operational procedures
  - Terminal management rules
  - Auto-reload vs restart guidelines
  - Error handling workflows
  - Debugging decision trees
- **When to Read:** ALWAYS read this file FIRST before any development task

### 2️⃣ **.github/copilot-instructions.md** - HIGH PRIORITY
- **Location:** `d:\Projects\medicalstore\.github\copilot-instructions.md`
- **Purpose:** Quick reference for GitHub Copilot (auto-discovered)
- **Read By:** GitHub Copilot (automatically)
- **Contains:**
  - Quick reference guide
  - Critical rules summary
  - Common commands
  - Link to main AGENT_INSTRUCTIONS.md

### 3️⃣ **README.md** (ROOT) - MEDIUM PRIORITY
- **Location:** `d:\Projects\medicalstore\README.md`
- **Purpose:** Project overview and quick start guide
- **Read By:** Developers, users, AI agents (for context)
- **Contains:**
  - Technology stack
  - Quick start commands
  - Project structure
  - Links to all documentation

### 4️⃣ **SETUP_AND_RUN.md** - MEDIUM PRIORITY
- **Location:** `d:\Projects\medicalstore\SETUP_AND_RUN.md`
- **Purpose:** Installation and setup instructions
- **Read By:** New developers, users
- **Contains:**
  - Prerequisites
  - Installation steps
  - Configuration details

### 5️⃣ **LOGIN_TEST_GUIDE.md** - LOW PRIORITY
- **Location:** `d:\Projects\medicalstore\LOGIN_TEST_GUIDE.md`
- **Purpose:** Testing guide for login functionality
- **Read By:** Testers, QA, users
- **Contains:**
  - Test scenarios
  - Expected results
  - Troubleshooting

## 🤖 Agent Reading Order

When an AI agent starts working on this project, it should read files in this order:

```
1. AGENT_INSTRUCTIONS.md          ← Read FIRST (complete operational guide)
   ↓
2. .github/copilot-instructions.md ← Read SECOND (quick reference)
   ↓
3. README.md                       ← Read THIRD (project overview)
   ↓
4. Other docs as needed            ← Read as needed for specific tasks
```

## 📁 File Location Standards

### Root Directory Files
```
medicalstore/
├── AGENT_INSTRUCTIONS.md         ← Agent primary instructions
├── README.md                      ← Project overview
├── SETUP_AND_RUN.md              ← Setup guide
├── LOGIN_TEST_GUIDE.md           ← Testing guide
└── .github/
    └── copilot-instructions.md   ← Copilot quick reference
```

### Why This Structure?

1. **AGENT_INSTRUCTIONS.md in root:**
   - Easy to find
   - Conventional name recognized by AI agents
   - Not hidden in subdirectories

2. **.github/copilot-instructions.md:**
   - Auto-discovered by GitHub Copilot
   - GitHub's standard location for Copilot instructions
   - Quick reference that links to main docs

3. **README.md in root:**
   - GitHub's default documentation file
   - First thing developers see
   - Contains links to all other docs

## 🎯 Best Practices for Future Docs

### Naming Conventions
- Use **UPPERCASE_WITH_UNDERSCORES.md** for critical agent instructions
- Use **lowercase-with-dashes.md** for user-facing guides
- Use descriptive names (e.g., `AGENT_INSTRUCTIONS.md` not `ai.md`)

### File Headers
Always include metadata comments at the top:
```markdown
<!--
  File: FILENAME.md
  Purpose: Brief description
  Priority: CRITICAL/HIGH/MEDIUM/LOW
  Last Updated: Date
-->
```

### Linking
- Always link to primary documentation from secondary docs
- Use relative paths: `[Main Docs](../AGENT_INSTRUCTIONS.md)`
- Include "See X for details" references

## ✅ Checklist for New Documentation

When creating new documentation files:

- [ ] Choose appropriate naming convention
- [ ] Add file to this hierarchy document
- [ ] Include metadata header
- [ ] Link from README.md
- [ ] Link from AGENT_INSTRUCTIONS.md if relevant
- [ ] Update .github/copilot-instructions.md if needed

---

**Remember:** The agent should ALWAYS read `AGENT_INSTRUCTIONS.md` FIRST!
