package com.medicalstore.config;

import com.medicalstore.model.SubscriptionPlan;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.SubscriptionService;
import com.medicalstore.common.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalModelAttribute {

    private final SubscriptionService subscriptionService;
    private final MedicineService medicineService;
    private final SecurityUtils securityUtils;

    @ModelAttribute("lowStockCount")
    public long lowStockCount() {
        try {
            return medicineService.countLowStockMedicines(10);
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("expiringCount")
    public long expiringCount() {
        try {
            // COUNT query — avoids loading full Medicine entities just to call .size()
            return medicineService.countExpiringSoonMedicines(30);
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("subscriptionWarning")
    public String subscriptionWarning() {
        try {
            if (!securityUtils.isOwnerOrShopkeeper()) {
                return null;
            }

            Long ownerId = securityUtils.getCurrentOwnerId();
            if (ownerId == null) {
                return null;
            }

            SubscriptionPlan plan = subscriptionService.getPlanForOwner(ownerId).orElse(null);

            if (plan == null) {
                return "No active subscription found. Please contact administration.";
            }

            if (plan.isExpired()) {
                return "Subscription expired on " + plan.getExpiryDate()
                        + ". Please renew to restore full access.";
            }

            long daysLeft = plan.getDaysUntilExpiry();
            if (daysLeft <= 7 && daysLeft >= 0) {
                return "Warning: Your subscription expires in " + daysLeft + " days (" + plan.getExpiryDate() + ").";
            }

            return null;
        } catch (Exception e) {
            log.warn("Failed to load subscription warning: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Exposes the subscription-expired flag set by {@link SubscriptionInterceptor}
     * so that Thymeleaf templates can show an inline "read-only mode" banner.
     */
    @ModelAttribute("subscriptionExpired")
    public boolean subscriptionExpired(HttpServletRequest request) {
        Object flag = request.getAttribute(SubscriptionInterceptor.ATTR_SUBSCRIPTION_EXPIRED);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * Exposes the set of feature codes available for the current owner's subscription tier.
     * Templates can gate UI elements with: {@code th:if="${availableFeatures.contains('EXCEL_EXPORT')}"}.
     * Returns an empty set for ADMIN users (they bypass subscription checks) and
     * unauthenticated requests.
     */
    @ModelAttribute("availableFeatures")
    public java.util.Set<String> availableFeatures() {
        try {
            Long ownerId = securityUtils.getCurrentOwnerId();
            if (ownerId == null) {
                // ADMINs are not bound by subscriptions — grant everything
                if (securityUtils.isAdmin()) {
                    return java.util.Set.of("INVOICE_PRINT", "BASIC_REPORTS",
                            "ADVANCED_ANALYTICS", "EXCEL_EXPORT", "BULK_EXPORT", "API_ACCESS");
                }
                return java.util.Collections.emptySet();
            }
            return subscriptionService.getAvailableFeatures(ownerId);
        } catch (Exception e) {
            return java.util.Collections.emptySet();
        }
    }
}