package com.medicalstore.controller;

import com.medicalstore.model.SubscriptionPlan;
import com.medicalstore.service.SubscriptionService;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@Slf4j
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
        try {
            Long ownerId = securityUtils.getCurrentOwnerId();
            if (ownerId != null) {
                SubscriptionPlan plan = subscriptionService.getPlanForOwner(ownerId).orElse(null);
                model.addAttribute("plan", plan);
                if (plan != null && plan.isExpired()) {
                    long daysExpired = plan.getExpiryDate().until(LocalDate.now(),
                            java.time.temporal.ChronoUnit.DAYS);
                    model.addAttribute("daysExpired", daysExpired);
                }
            } else {
                model.addAttribute("plan", null);
            }
        } catch (Exception e) {
            log.warn("Failed to load subscription plan for billing page: {}", e.getMessage());
            model.addAttribute("plan", null);
        }
        return "subscription/billing";
    }
}
