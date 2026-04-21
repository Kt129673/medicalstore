package com.medicalstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Dashboard controller for SHOPKEEPER and OWNER roles (Operational Level).
 * Provides the main dashboard and POS entry point.
 *
 * Routes:
 *   GET /          → redirect to /dashboard
 *   GET /dashboard → renders index.html (KPIs loaded async via DashboardApiController)
 *
 * Role access: ADMIN (read-only monitoring), OWNER (view-as-shopkeeper), SHOPKEEPER (full access)
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'SHOPKEEPER')")
public class DashboardController {

    /** Redirect legacy root URL to canonical /dashboard. */
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("page", "home");
        return "index";
    }
}

