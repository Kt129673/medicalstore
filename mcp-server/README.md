# MedicalStore MCP Testing Server

An [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) server that exposes tools for testing the MedicalStore Spring Boot application through AI assistants.

## Quick Start

```bash
# 1. Install dependencies
cd mcp-server
npm install

# 2. Start the MedicalStore Spring Boot app (in another terminal)
cd ..
./mvnw spring-boot:run

# 3. The MCP server runs automatically when configured in your AI assistant
```

## Configuration

### Gemini Code Assist / VS Code

The server is already configured in `.gemini/settings.json`:

```json
{
  "mcpServers": {
    "medicalstore-test": {
      "command": "node",
      "args": ["e:/projects/medicalstore/mcp-server/index.js"],
      "env": {
        "MEDICALSTORE_URL": "http://localhost:8081"
      }
    }
  }
}
```

### Claude Desktop

Add to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "medicalstore-test": {
      "command": "node",
      "args": ["C:/path/to/medicalstore/mcp-server/index.js"],
      "env": {
        "MEDICALSTORE_URL": "http://localhost:8081"
      }
    }
  }
}
```

## Available Tools

| Tool | Description |
|---|---|
| `health_check` | Check if the application is running and reachable |
| `login` | Authenticate with username/password |
| `logout` | End the session and clear cookies |
| `get_dashboard_kpis` | Fetch dashboard KPIs (sales, revenue, stock) |
| `search_medicines` | Search medicines by name via POS API |
| `check_page` | Verify a specific route is accessible |
| `test_all_routes` | Test all major routes in one call |
| `test_role_access` | Login and verify RBAC for all role-specific routes |
| `make_request` | Make custom HTTP requests (GET/POST/PUT/DELETE) |
| `session_status` | Check current session/cookie state |

## Available Resources

| Resource | URI | Description |
|---|---|---|
| App Routes | `medicalstore://routes` | Complete list of all routes grouped by role |
| Test Credentials | `medicalstore://test-credentials` | Default test usernames/passwords |

## Usage Examples

### Check if the app is running
```
> Use the health_check tool
```

### Login and fetch dashboard data
```
> Login with username "admin" and password "admin123", then get dashboard KPIs
```

### Test role-based access
```
> Test role access for user "shopkeeper" with password "shop123"
```

### Verify a specific page
```
> Check if the /medicines page is accessible
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MEDICALSTORE_URL` | `http://localhost:8081` | Base URL of the running Spring Boot app |
