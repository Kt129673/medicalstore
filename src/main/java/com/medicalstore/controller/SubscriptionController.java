package com.medicalstore.controller;

import com.medicalstore.model.SubscriptionPlan;
import com.medicalstore.service.SubscriptionService;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SecurityUtils securityUtils;

    /**
     * Subscription expired / billing page.
     * Accessible by all authenticated users (SubscriptionInterceptor redirects here).
     * Loads current plan details so the owner/shopkeeper can see what expired.
     */
    @GetMapping("/subscription/billing")
    public String billingPage(Model model) {
        // For owners, load their actual plan details
        Long ownerId = securityUtils.getCurrentOwnerId();
        if (ownerId != null) {
            SubscriptionPlan plan = subscriptionService.getPlanForOwner(ownerId).orElse(null);
            model.addAttribute("plan", plan);
            if (plan != null && plan.isExpired()) {
                long daysExpired = plan.getExpiryDate().until(LocalDate.now(),
                        java.time.temporal.ChronoUnit.DAYS);
                model.addAttribute("daysExpired", daysExpired);
            }
        }
        // For shopkeepers, also try to load plan via their owner chain
        else if (securityUtils.isOwnerOrShopkeeper()) {
            // Shopkeeper without resolved ownerId — plan can't be loaded
            model.addAttribute("plan", null);
        }
        return "subscription/billing";
    }
}
