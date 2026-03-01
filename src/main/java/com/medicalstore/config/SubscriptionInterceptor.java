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
        if (RoutePaths.isPublicOrStatic(uri)) {
            return true;
        }

        // Only enforce for Owners and Shopkeepers
        if (securityUtils.isOwnerOrShopkeeper()) {
            Long userId = securityUtils.getCurrentUserId();
            if (userId != null) {
                Long ownerId = securityUtils.getCurrentOwnerId();
                if (ownerId != null) {
                    var plan = subscriptionService.getPlanForOwner(ownerId);
                    if (plan.isPresent() && plan.get().isExpired()) {
                        // Redirect to billing wall
                        response.sendRedirect(RoutePaths.SUBSCRIPTION_BILLING);
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
