---
trigger: always_on
glob:
description: MedicalStore project rules — README sync, commit standards, and code quality
---

## README Sync Rule

**MANDATORY**: Whenever you make code changes that add, remove, or modify features, endpoints, dependencies, configurations, or project structure, you MUST also update the `README.md` file to reflect those changes BEFORE committing.

### What triggers a README update:

1. **New dependencies added** to `pom.xml` → Update the "Tech Stack" table
2. **New API endpoints** created → Update the "API Endpoints" section
3. **New controllers/services** added → Update "Project Structure" tree
4. **Configuration changes** in `application.properties` → Update "Configuration" table
5. **New scheduled jobs** added → Update "Scheduled Jobs" table
6. **Security/role changes** → Update "Role-Based Access Control" section
7. **New build commands or plugins** → Update "Building & Testing" section
8. **New documentation files** added → Update "Documentation" table
9. **CI/CD pipeline changes** → Update relevant deployment/testing sections
10. **New modules/features** → Update "Features" table and "Modules" section

### README update checklist:

- [ ] Badges updated (if version changed)
- [ ] Feature table reflects new capabilities
- [ ] Tech stack table has new technologies
- [ ] API endpoints table has new routes
- [ ] Project structure tree shows new files/directories
- [ ] Configuration table has new properties
- [ ] Building & Testing section has new commands

## Commit Message Standards

Use conventional commit format:
- `feat:` — New feature
- `fix:` — Bug fix
- `docs:` — Documentation only
- `refactor:` — Code restructuring
- `test:` — Adding tests
- `chore:` — Maintenance tasks

## Code Quality

- Always run `./mvnw compile` to verify build before committing
- Fix lint warnings before committing
- Add Javadoc to all public classes and methods
- Add OpenAPI `@Operation` annotations to all REST endpoints
