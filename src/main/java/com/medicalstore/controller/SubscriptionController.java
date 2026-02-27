package com.medicalstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SubscriptionController {

    @GetMapping("/subscription/billing")
    public String billingPage(Model model) {
        // Just a placeholder template for "Please renew"
        return "subscription/billing";
    }
}
