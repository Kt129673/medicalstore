package com.medicalstore.controller;

import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.common.RoutePaths;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping(RoutePaths.PROFILE)
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    /** Redirect bare /profile to the change-password page (only profile action available). */
    @GetMapping
    public String profileHome() {
        return RoutePaths.redirectTo(RoutePaths.PROFILE_CHANGE_PASSWORD);
    }

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
        if (userId == null) {
            redirectAttrs.addFlashAttribute("errorMessage", "Session expired. Please log in again.");
            return RoutePaths.redirectTo(RoutePaths.LOGIN);
        }
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            redirectAttrs.addFlashAttribute("errorMessage", "User not found.");
            return RoutePaths.redirectTo(RoutePaths.PROFILE_CHANGE_PASSWORD);
        }

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttrs.addFlashAttribute("errorMessage", "Current password is incorrect.");
            return RoutePaths.redirectTo(RoutePaths.PROFILE_CHANGE_PASSWORD);
        }

        // Confirm new password match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttrs.addFlashAttribute("errorMessage", "New passwords do not match.");
            return RoutePaths.redirectTo(RoutePaths.PROFILE_CHANGE_PASSWORD);
        }

        // Minimum length check
        if (newPassword.length() < 6) {
            redirectAttrs.addFlashAttribute("errorMessage", "New password must be at least 6 characters.");
            return RoutePaths.redirectTo(RoutePaths.PROFILE_CHANGE_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttrs.addFlashAttribute("successMessage", "Password changed successfully. Please log in again if needed.");
        return RoutePaths.redirectTo(RoutePaths.PROFILE_CHANGE_PASSWORD);
    }
}
