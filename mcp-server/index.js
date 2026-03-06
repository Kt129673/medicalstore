#!/usr/bin/env node

/**
 * MedicalStore MCP Server
 *
 * An MCP (Model Context Protocol) server that exposes tools for testing
 * the MedicalStore Spring Boot application. It provides:
 *
 *   - Health & connectivity checks
 *   - Authentication (login/logout)
 *   - API endpoint testing (Dashboard KPIs, Medicine search)
 *   - Page accessibility verification
 *   - Database query via the app's APIs
 *
 * Usage:
 *   1. Start the MedicalStore Spring Boot app (port 8081)
 *   2. Run: node index.js
 *   3. Configure in your AI assistant's MCP settings
 */

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import fetch from "node-fetch";
import { CookieJar } from "./cookie-jar.js";

// ──────────────────────────────────────────────────────────────────────────────
// Configuration
// ──────────────────────────────────────────────────────────────────────────────
const BASE_URL = process.env.MEDICALSTORE_URL || "http://localhost:8081";

// ──────────────────────────────────────────────────────────────────────────────
// Session state (cookies for authenticated requests)
// ──────────────────────────────────────────────────────────────────────────────
const cookieJar = new CookieJar();

// ──────────────────────────────────────────────────────────────────────────────
// Helper — HTTP request with cookie support
// ──────────────────────────────────────────────────────────────────────────────
async function request(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const headers = {
    ...(options.headers || {}),
    Cookie: cookieJar.getCookieHeader(),
  };

  const res = await fetch(url, {
    ...options,
    headers,
    redirect: "manual", // handle redirects manually to capture cookies
  });

  // Store any Set-Cookie headers
  const setCookies = res.headers.raw()["set-cookie"];
  if (setCookies) {
    setCookies.forEach((c) => cookieJar.addCookie(c));
  }

  return res;
}

async function requestJSON(path, options = {}) {
  const res = await request(path, {
    ...options,
    headers: {
      ...(options.headers || {}),
      Accept: "application/json",
    },
  });

  const contentType = res.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return { status: res.status, data: await res.json(), redirected: false };
  }

  // If redirected (e.g., to login page), return redirect info
  if (res.status >= 300 && res.status < 400) {
    return {
      status: res.status,
      data: null,
      redirected: true,
      location: res.headers.get("location"),
    };
  }

  return {
    status: res.status,
    data: await res.text(),
    redirected: false,
  };
}

// ──────────────────────────────────────────────────────────────────────────────
// Create the MCP Server
// ──────────────────────────────────────────────────────────────────────────────
const server = new McpServer({
  name: "medicalstore-test",
  version: "1.0.0",
});

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: health_check
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "health_check",
  "Check if the MedicalStore application is running and reachable",
  {},
  async () => {
    try {
      const start = Date.now();
      const res = await request("/login");
      const elapsed = Date.now() - start;

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                status: "UP",
                httpStatus: res.status,
                responseTimeMs: elapsed,
                baseUrl: BASE_URL,
                message: `Application is reachable at ${BASE_URL} (${elapsed}ms)`,
              },
              null,
              2
            ),
          },
        ],
      };
    } catch (error) {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                status: "DOWN",
                baseUrl: BASE_URL,
                error: error.message,
                message: `Cannot reach application at ${BASE_URL}. Is Spring Boot running on port 8081?`,
              },
              null,
              2
            ),
          },
        ],
      };
    }
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: login
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "login",
  "Authenticate with the MedicalStore application using username and password. Must be called before accessing protected endpoints.",
  {
    username: z.string().describe("Login username"),
    password: z.string().describe("Login password"),
  },
  async ({ username, password }) => {
    try {
      // Step 1: GET the login page to obtain the CSRF token
      const loginPage = await request("/login");
      const html = await loginPage.text();

      // Extract CSRF token from the login form
      const csrfMatch = html.match(
        /name="_csrf"\s+value="([^"]+)"/
      );
      const csrfToken = csrfMatch ? csrfMatch[1] : null;

      // Step 2: POST the login form
      const body = new URLSearchParams({
        username,
        password,
        ...(csrfToken ? { _csrf: csrfToken } : {}),
      });

      const res = await request("/login", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: body.toString(),
      });

      const location = res.headers.get("location") || "";
      const isSuccess =
        res.status === 302 && !location.includes("error");

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                success: isSuccess,
                httpStatus: res.status,
                redirectTo: location,
                message: isSuccess
                  ? `Successfully logged in as '${username}'`
                  : `Login failed for '${username}'. Check credentials.`,
                sessionActive: cookieJar.hasCookie("JSESSIONID"),
              },
              null,
              2
            ),
          },
        ],
      };
    } catch (error) {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              { success: false, error: error.message },
              null,
              2
            ),
          },
        ],
      };
    }
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: logout
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "logout",
  "Log out from the MedicalStore application and clear the session",
  {},
  async () => {
    try {
      await request("/logout", { method: "POST" });
      cookieJar.clear();

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              { success: true, message: "Logged out and session cleared" },
              null,
              2
            ),
          },
        ],
      };
    } catch (error) {
      cookieJar.clear();
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                success: true,
                message: "Session cleared locally",
                warning: error.message,
              },
              null,
              2
            ),
          },
        ],
      };
    }
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: get_dashboard_kpis
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "get_dashboard_kpis",
  "Fetch dashboard KPIs (today's sales, monthly revenue, total medicines, customers, low stock count). Requires authentication first.",
  {},
  async () => {
    try {
      const result = await requestJSON("/api/v1/dashboard/kpis");

      if (result.redirected || result.status === 401 || result.status === 403) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  error: "Not authenticated",
                  message:
                    "Please call the 'login' tool first to authenticate.",
                },
                null,
                2
              ),
            },
          ],
        };
      }

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                success: true,
                httpStatus: result.status,
                kpis: result.data,
              },
              null,
              2
            ),
          },
        ],
      };
    } catch (error) {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              { success: false, error: error.message },
              null,
              2
            ),
          },
        ],
      };
    }
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: search_medicines
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "search_medicines",
  "Search for medicines by name using the POS API. Returns matching medicine names, prices, quantities, and batch info. Requires authentication.",
  {
    query: z
      .string()
      .describe("Search query (medicine name or partial name)"),
  },
  async ({ query }) => {
    try {
      const result = await requestJSON(
        `/api/v1/medicines/search?q=${encodeURIComponent(query)}`
      );

      if (result.redirected || result.status === 401 || result.status === 403) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  error: "Not authenticated or access denied",
                  message:
                    "Please login with ADMIN or SHOPKEEPER role first.",
                },
                null,
                2
              ),
            },
          ],
        };
      }

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                success: true,
                query,
                resultCount: Array.isArray(result.data)
                  ? result.data.length
                  : 0,
                medicines: result.data,
              },
              null,
              2
            ),
          },
        ],
      };
    } catch (error) {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              { success: false, error: error.message },
              null,
              2
            ),
          },
        ],
      };
    }
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: check_page
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "check_page",
  "Check if a specific page/route is accessible and returns a valid response. Useful for verifying route configurations and role-based access. Requires authentication for protected routes.",
  {
    path: z
      .string()
      .describe(
        "The URL path to check, e.g. '/medicines', '/admin', '/sales'"
      ),
  },
  async ({ path }) => {
    try {
      const normalizedPath = path.startsWith("/") ? path : `/${path}`;
      const start = Date.now();
      const res = await request(normalizedPath);
      const elapsed = Date.now() - start;
      const body = await res.text();

      // Determine page title from HTML
      const titleMatch = body.match(/<title[^>]*>([^<]+)<\/title>/i);
      const title = titleMatch ? titleMatch[1].trim() : null;

      // Check if redirected to login
      const location = res.headers.get("location") || "";
      const redirectedToLogin =
        location.includes("/login") ||
        (res.status === 200 && body.includes('action="/login"'));

      // Check for error pages
      const is403 = res.status === 403 || body.includes("403");
      const is404 = res.status === 404;

      let accessStatus = "ACCESSIBLE";
      if (redirectedToLogin) accessStatus = "REQUIRES_LOGIN";
      else if (is403) accessStatus = "FORBIDDEN";
      else if (is404) accessStatus = "NOT_FOUND";
      else if (res.status >= 500) accessStatus = "SERVER_ERROR";

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                path: normalizedPath,
                httpStatus: res.status,
                accessStatus,
                title,
                responseTimeMs: elapsed,
                contentLength: body.length,
                redirectLocation: location || undefined,
              },
              null,
              2
            ),
          },
        ],
      };
    } catch (error) {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              { path, error: error.message },
              null,
              2
            ),
          },
        ],
      };
    }
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: test_all_routes
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "test_all_routes",
  "Run a comprehensive accessibility test against all major application routes. Returns a summary of which routes are accessible, require login, are forbidden, or return errors. Requires prior authentication.",
  {},
  async () => {
    const routes = [
      // Public
      { path: "/login", expectedAccess: "PUBLIC" },
      // Shopkeeper routes
      { path: "/medicines", expectedRole: "SHOPKEEPER" },
      { path: "/sales", expectedRole: "SHOPKEEPER" },
      { path: "/customers", expectedRole: "SHOPKEEPER" },
      { path: "/returns", expectedRole: "SHOPKEEPER" },
      { path: "/suppliers", expectedRole: "SHOPKEEPER" },
      { path: "/purchases", expectedRole: "SHOPKEEPER" },
      { path: "/dashboard", expectedRole: "SHOPKEEPER" },
      // Owner routes
      { path: "/owner/dashboard", expectedRole: "OWNER" },
      // Admin routes
      { path: "/admin", expectedRole: "ADMIN" },
      { path: "/admin/users", expectedRole: "ADMIN" },
      { path: "/admin/branches", expectedRole: "ADMIN" },
      // Analytics & Reports (all roles)
      { path: "/analytics", expectedRole: "ANY" },
      { path: "/reports", expectedRole: "ANY" },
      // Profile
      { path: "/profile/change-password", expectedRole: "ANY" },
      // API
      { path: "/api/v1/dashboard/kpis", expectedRole: "ANY", isApi: true },
    ];

    const results = [];

    for (const route of routes) {
      try {
        const start = Date.now();
        const res = await request(route.path);
        const elapsed = Date.now() - start;
        const location = res.headers.get("location") || "";
        const body = await res.text();

        const redirectedToLogin =
          location.includes("/login") ||
          (res.status === 200 && body.includes('action="/login"'));

        let status = "✅ OK";
        if (redirectedToLogin) status = "🔒 REQUIRES_LOGIN";
        else if (res.status === 403) status = "🚫 FORBIDDEN";
        else if (res.status === 404) status = "❌ NOT_FOUND";
        else if (res.status >= 500) status = "💥 SERVER_ERROR";

        results.push({
          path: route.path,
          httpStatus: res.status,
          status,
          responseMs: elapsed,
        });
      } catch (error) {
        results.push({
          path: route.path,
          status: "💥 UNREACHABLE",
          error: error.message,
        });
      }
    }

    const summary = {
      totalRoutes: results.length,
      accessible: results.filter((r) => r.status === "✅ OK").length,
      requiresLogin: results.filter((r) => r.status === "🔒 REQUIRES_LOGIN")
        .length,
      forbidden: results.filter((r) => r.status === "🚫 FORBIDDEN").length,
      notFound: results.filter((r) => r.status === "❌ NOT_FOUND").length,
      errors: results.filter((r) =>
        r.status.includes("SERVER_ERROR") || r.status.includes("UNREACHABLE")
      ).length,
    };

    return {
      content: [
        {
          type: "text",
          text: JSON.stringify({ summary, routes: results }, null, 2),
        },
      ],
    };
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: test_role_access
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "test_role_access",
  "Test role-based access control by logging in with given credentials and checking access to role-specific routes. Verifies that the RBAC rules are correctly enforced.",
  {
    username: z.string().describe("Username to test with"),
    password: z.string().describe("Password for the user"),
  },
  async ({ username, password }) => {
    try {
      // Clear previous session
      cookieJar.clear();

      // Login
      const loginPage = await request("/login");
      const html = await loginPage.text();
      const csrfMatch = html.match(/name="_csrf"\s+value="([^"]+)"/);
      const csrfToken = csrfMatch ? csrfMatch[1] : null;

      const body = new URLSearchParams({
        username,
        password,
        ...(csrfToken ? { _csrf: csrfToken } : {}),
      });

      const loginRes = await request("/login", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: body.toString(),
      });

      const loginLocation = loginRes.headers.get("location") || "";
      if (loginLocation.includes("error")) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  success: false,
                  message: `Login failed for '${username}'`,
                },
                null,
                2
              ),
            },
          ],
        };
      }

      // Test access to each role's routes
      const roleRoutes = {
        ADMIN: ["/admin", "/admin/users", "/admin/branches"],
        OWNER: ["/owner/dashboard"],
        SHOPKEEPER: ["/medicines", "/sales", "/customers", "/purchases"],
        SHARED: ["/analytics", "/reports", "/profile/change-password"],
      };

      const accessResults = {};

      for (const [role, routes] of Object.entries(roleRoutes)) {
        accessResults[role] = [];
        for (const route of routes) {
          const res = await request(route);
          const loc = res.headers.get("location") || "";
          const pageBody = await res.text();

          let access = "GRANTED";
          if (res.status === 403 || pageBody.includes("403"))
            access = "DENIED";
          if (loc.includes("/login")) access = "NOT_AUTHENTICATED";

          accessResults[role].push({
            route,
            httpStatus: res.status,
            access,
          });
        }
      }

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                success: true,
                testedUser: username,
                accessMatrix: accessResults,
              },
              null,
              2
            ),
          },
        ],
      };
    } catch (error) {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              { success: false, error: error.message },
              null,
              2
            ),
          },
        ],
      };
    }
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: make_request
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "make_request",
  "Make a custom HTTP request to any endpoint of the MedicalStore application. Supports GET, POST, PUT, DELETE methods with optional request body.",
  {
    method: z
      .enum(["GET", "POST", "PUT", "DELETE"])
      .describe("HTTP method"),
    path: z.string().describe("URL path, e.g. '/api/v1/dashboard/kpis'"),
    body: z
      .string()
      .optional()
      .describe("Request body (JSON string for POST/PUT)"),
    contentType: z
      .string()
      .optional()
      .describe(
        "Content-Type header (default: application/json for POST/PUT)"
      ),
  },
  async ({ method, path, body, contentType }) => {
    try {
      const normalizedPath = path.startsWith("/") ? path : `/${path}`;
      const headers = {};

      if (method === "POST" || method === "PUT") {
        headers["Content-Type"] =
          contentType || "application/json";
      }

      const start = Date.now();
      const res = await request(normalizedPath, {
        method,
        headers,
        body: body || undefined,
      });
      const elapsed = Date.now() - start;

      const responseContentType = res.headers.get("content-type") || "";
      let responseBody;

      if (responseContentType.includes("application/json")) {
        responseBody = await res.json();
      } else {
        const text = await res.text();
        // Truncate large HTML responses
        responseBody =
          text.length > 2000
            ? text.substring(0, 2000) + "\n... [truncated]"
            : text;
      }

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                method,
                path: normalizedPath,
                httpStatus: res.status,
                responseTimeMs: elapsed,
                contentType: responseContentType,
                headers: {
                  location: res.headers.get("location"),
                },
                body: responseBody,
              },
              null,
              2
            ),
          },
        ],
      };
    } catch (error) {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              { method, path, error: error.message },
              null,
              2
            ),
          },
        ],
      };
    }
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// TOOL: session_status
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.tool(
  "session_status",
  "Check the current authentication session status — whether you're logged in and what cookies are active.",
  {},
  async () => {
    return {
      content: [
        {
          type: "text",
          text: JSON.stringify(
            {
              hasSession: cookieJar.hasCookie("JSESSIONID"),
              hasRememberMe: cookieJar.hasCookie("remember-me"),
              cookieCount: cookieJar.count(),
              baseUrl: BASE_URL,
            },
            null,
            2
          ),
        },
      ],
    };
  }
);

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Resources — Application info
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
server.resource("app-routes", "medicalstore://routes", async (uri) => ({
  contents: [
    {
      uri: uri.href,
      mimeType: "application/json",
      text: JSON.stringify(
        {
          description: "All MedicalStore application routes and their roles",
          routes: {
            public: ["/login"],
            admin: [
              "/admin",
              "/admin/users",
              "/admin/users/create",
              "/admin/branches",
              "/admin/subscriptions",
              "/admin/audit-logs",
            ],
            owner: [
              "/owner/dashboard",
              "/owner/shopkeepers",
              "/owner/compare",
              "/owner/subscription",
            ],
            shopkeeper: [
              "/medicines",
              "/medicines/new",
              "/medicines/low-stock",
              "/medicines/expiry-alerts",
              "/sales",
              "/sales/new",
              "/customers",
              "/customers/new",
              "/suppliers",
              "/suppliers/new",
              "/suppliers/credits",
              "/purchases",
              "/purchases/new",
              "/returns",
              "/returns/new",
              "/dashboard",
            ],
            shared: [
              "/analytics",
              "/analytics/fast-moving",
              "/analytics/dead-stock",
              "/analytics/profit-per-medicine",
              "/analytics/gst-summary",
              "/reports",
              "/reports/daily-detailed",
              "/reports/monthly-detailed",
              "/reports/sales-report",
              "/reports/gst-report",
              "/reports/profit-loss",
              "/reports/expiry-report",
              "/profile/change-password",
            ],
            api: [
              "GET /api/v1/dashboard/kpis",
              "GET /api/v1/medicines/search?q={query}",
            ],
          },
        },
        null,
        2
      ),
    },
  ],
}));

server.resource(
  "test-credentials",
  "medicalstore://test-credentials",
  async (uri) => ({
    contents: [
      {
        uri: uri.href,
        mimeType: "application/json",
        text: JSON.stringify(
          {
            description:
              "Default test credentials created by DataInitializer (only available if seed data is loaded)",
            note: "Update these if your DataInitializer uses different credentials",
            credentials: [
              {
                role: "ADMIN",
                username: "admin",
                password: "admin123",
                description: "Full platform access",
              },
              {
                role: "OWNER",
                username: "owner",
                password: "owner123",
                description: "Multi-branch portfolio management",
              },
              {
                role: "SHOPKEEPER",
                username: "shopkeeper",
                password: "shop123",
                description: "Store-level operations",
              },
            ],
          },
          null,
          2
        ),
      },
    ],
  })
);

// ──────────────────────────────────────────────────────────────────────────────
// Start the server
// ──────────────────────────────────────────────────────────────────────────────
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("MedicalStore MCP Server running on stdio");
}

main().catch((error) => {
  console.error("Fatal error:", error);
  process.exit(1);
});
