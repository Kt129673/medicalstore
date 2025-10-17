# Welcome to My GitHub Profile Chat Instructions

**Note:** Always check end-to-end integration for both frontend and backend. Do not prompt the user about frontend integration—assume it is required for every feature or change.

## Standard Point

To efficiently manage both frontend and backend projects in this repository, follow these steps:

- Use separate terminals or scripts to start both the frontend and backend servers.
- Ensure dependencies are installed in both `medical-store-management-frontend` and `medical-store-management-backend` folders.
- For a single command experience, you can use a script or task runner (like npm scripts, concurrently, or VS Code tasks) to launch both servers together.

### Example (Manual)

1. Open two terminals.
2. In the first terminal:
	- Navigate to the backend folder: `cd medical-store-management-backend`
	- Run: `mvnw spring-boot:run` (or `mvn spring-boot:run`)
3. In the second terminal:
	- Navigate to the frontend folder: `cd medical-store-management-frontend`
	- Run: `npm start` or `npx ng serve --open`

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
