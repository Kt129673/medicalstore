package com.medicalstore.controller;

import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SecurityUtils securityUtils;

    @GetMapping("/")
    public String home(Model model) {

        // ── OWNER → redirect to owner dashboard ───────────────────────────
        if (securityUtils.isOwner()) {
            return "redirect:/owner";
        }

        // Dashboard is now loaded asynchronously via DashboardApiController
        return "index";
    }
}
