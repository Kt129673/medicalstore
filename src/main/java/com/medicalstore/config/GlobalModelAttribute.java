package com.medicalstore.config;

import com.medicalstore.model.SubscriptionPlan;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.SubscriptionService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
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
            return "Your subscription expired on " + plan.getExpiryDate()
                    + ". Please renew immediately to avoid service interruption.";
        }

        long daysLeft = plan.getDaysUntilExpiry();
        if (daysLeft <= 7 && daysLeft >= 0) {
            return "Warning: Your subscription expires in " + daysLeft + " days (" + plan.getExpiryDate() + ").";
        }

        return null;
    }
}