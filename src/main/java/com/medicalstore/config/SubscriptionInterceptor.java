package com.medicalstore.config;

import com.medicalstore.common.RoutePaths;
import com.medicalstore.common.SecurityUtils;
import com.medicalstore.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionInterceptor implements HandlerInterceptor {

    /**
     * Request attribute name to signal templates that the subscription is expired.
     */
    public static final String ATTR_SUBSCRIPTION_EXPIRED = "subscriptionExpired";

    private static final java.util.Set<String> WRITE_METHODS = java.util.Set.of("POST", "PUT", "DELETE", "PATCH");

    private final SubscriptionService subscriptionService;
    private final SecurityUtils securityUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();

        // Skip static resources and unprotected/billing routes
        if (RoutePaths.isPublicOrStatic(uri)) {
            return true;
        }

        // Admin routes are never subject to subscription checks
        if (uri.startsWith("/admin")) {
            return true;
        }

        // Only enforce for Owners and Shopkeepers
        if (securityUtils.isOwnerOrShopkeeper()) {
            try {
                Long userId = securityUtils.getCurrentUserId();
                if (userId != null) {
                    Long ownerId = securityUtils.getCurrentOwnerId();
                    if (ownerId == null) {
                        // Orphan shopkeeper with no owning branch — treat as expired
                        response.sendRedirect(RoutePaths.SUBSCRIPTION_BILLING + "?expired=true");
                        return false;
                    }
                    var plan = subscriptionService.getPlanForOwner(ownerId);
                    if (plan.isEmpty() || plan.get().isExpired()) {
                        // Block ALL requests (read and write) when subscription is expired
                        // Only allow access to subscription billing page itself
                        if (!uri.startsWith(RoutePaths.SUBSCRIPTION_BILLING)) {
                            response.sendRedirect(RoutePaths.SUBSCRIPTION_BILLING + "?expired=true");
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Subscription check failed for URI {}: {}", uri, e.getMessage());
                // Allow the request through to avoid a 500 error on subscription lookup failure
            }
        }

        return true;
    }
}
