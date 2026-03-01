package com.medicalstore.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {
    
    @GetMapping("/login")
    public ModelAndView login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            HttpServletResponse response,
            Model model) {
        
        // Disable caching for login page to prevent stale content
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
            model.addAttribute("logoutSuccess", true);
        }
        
        ModelAndView mav = new ModelAndView("login");
        mav.addAllObjects(model.asMap());
        return mav;
    }
}
