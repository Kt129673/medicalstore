package com.medicalstore.config;

import com.medicalstore.service.RoleAuditService;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * RoleBasedAccessInterceptor — intercepts all requests and logs role-based access.
 * 
 * This helps:
 * - Track which roles access which endpoints
 * - Identify unauthorized access attempts
 * - Generate access reports for compliance
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleBasedAccessInterceptor implements HandlerInterceptor {

    private final SecurityUtils securityUtils;
    private final RoleAuditService roleAuditService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip static resources
        if (path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/images")) {
            return true;
        }
        
        // Log API calls with role info
        if (path.startsWith("/api/")) {
            logApiAccess(path, method);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) 
            throws Exception {
        
        String path = request.getRequestURI();
        int status = response.getStatus();
        
        // Log 403 Forbidden and 401 Unauthorized attempts
        if (status == 403) {
            roleAuditService.logAccessDenied(path, "Forbidden - insufficient permissions");
        } else if (status == 401) {
            roleAuditService.logAccessDenied(path, "Unauthorized - not authenticated");
        }
        
        // Log exceptions
        if (ex != null) {
            log.error("Request exception for path: {} | Exception: {}", path, ex.getMessage());
        }
    }

    private void logApiAccess(String path, String method) {
        String currentUser = securityUtils.getCurrentUser() != null 
            ? securityUtils.getCurrentUser().getUsername() 
            : "ANONYMOUS";
        
        // Only log sensitive API operations (POST, PUT, DELETE)
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            log.debug("API_ACCESS | User: {} | Method: {} | Path: {}", currentUser, method, path);
        }
    }
}
