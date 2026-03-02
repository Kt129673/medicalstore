package com.medicalstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Dedicated dashboard controller for SHOPKEEPER role (Operational Level).
 * Provides the POS / Billing Dashboard and Daily Operations View.
 *
 * Architecture note (Section 6 – Dashboard Separation Strategy):
 *   Each role has a separate controller:
 *     - AdminController      → /admin
 *     - OwnerController      → /owner
 *     - ShopkeeperDashboardController → /  (this class)
 *
 * SHOPKEEPER responsibilities:
 *   - Perform sales / billing / POS
 *   - Update limited stock
 *   - View daily sales summary
 *
 * SHOPKEEPER restrictions:
 *   - Cannot create users
 *   - Cannot view financial analytics
 *   - Cannot access admin or owner features
 */
@Controller
@RequiredArgsConstructor
// OWNER included to support view-as-shopkeeper impersonation drill-down from OwnerController.
// ADMIN included for read-only operational monitoring (writes are blocked at method level on SaleController etc.)
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'SHOPKEEPER')")
public class ShopkeeperDashboardController {

    /**
     * Renders the shopkeeper's operational dashboard.
     * Dashboard data is loaded asynchronously via {@link com.medicalstore.controller.api.DashboardApiController}.
     */
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
