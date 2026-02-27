package com.medicalstore.config;

import com.medicalstore.service.SubscriptionService;
import com.medicalstore.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class SubscriptionInterceptor implements HandlerInterceptor {

    private final SubscriptionService subscriptionService;
    private final SecurityUtils securityUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();

        // Skip static resources and unprotected/billing routes
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images") ||
                uri.equals("/login") || uri.startsWith("/subscription/billing") || uri.equals("/logout")
                || uri.equals("/error")) {
            return true;
        }

        // Only enforce for Owners and Shopkeepers
        if (securityUtils.isOwner() || securityUtils.isShopkeeper()) {
            Long userId = securityUtils.getCurrentUserId();
            if (userId != null) {
                Long ownerId = getOwnerId();
                if (ownerId != null) {
                    var plan = subscriptionService.getPlanForOwner(ownerId);
                    if (plan.isPresent() && plan.get().isExpired()) {
                        // Redirect to billing wall
                        response.sendRedirect("/subscription/billing");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private Long getOwnerId() {
        if (securityUtils.isOwner()) {
            return securityUtils.getCurrentUserId();
        }
        return java.util.Optional.ofNullable(securityUtils.getCurrentUser()).map(u -> {
            if (u.getBranch() != null && u.getBranch().getOwner() != null) {
                return u.getBranch().getOwner().getId();
            }
            return null;
        }).orElse(null);
    }
}
