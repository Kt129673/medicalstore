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

## Dynamic Problem Solving & Troubleshooting

**MANDATORY**: If you encounter an error (build failure, bug, or unexpected behavior) while working, follow this dynamic troubleshooting process:

1. **Analyze First**: Do not guess or repeatedly apply identical fixes. Read execution logs, stack traces, and the actual error output carefully before modifying any code.
2. **Consult Previous Context (KIs)**: The system "remembers" past problem resolutions. Before spending time debugging a complex and unusual issue, check the Knowledge Items (KIs) or previous conversations for similar issues previously solved either by yourself or the user.
3. **Ask the User if Truly Stuck**: If you have tried to resolve a bug dynamically and failed twice, or if the solution requires business logic clarification, **STOP**. Clearly state what you attempted, the outcome, and ask the user for input or clarification to prevent making destructive changes.
4. **Document New Solutions**: If you or the user successfully debug and resolve a tricky or recurring issue that previously blocked progress, note the solution down (perhaps in a temporary artifact or mention it explicitly so the Knowledge System can capture it) so the identical problem can be avoided in the future.
