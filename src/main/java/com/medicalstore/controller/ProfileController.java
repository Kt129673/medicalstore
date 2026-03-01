package com.medicalstore.controller;

import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Profile management available to all authenticated roles.
 * Allows every user to change their own password.
 */
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("title", "Change Password");
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttrs) {

        Long userId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            redirectAttrs.addFlashAttribute("error", "User not found.");
            return "redirect:/profile/change-password";
        }

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttrs.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/profile/change-password";
        }

        // Confirm new password match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttrs.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/profile/change-password";
        }

        // Minimum length check
        if (newPassword.length() < 6) {
            redirectAttrs.addFlashAttribute("error", "New password must be at least 6 characters.");
            return "redirect:/profile/change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttrs.addFlashAttribute("success", "Password changed successfully. Please log in again if needed.");
        return "redirect:/profile/change-password";
    }
}
