package com.medicalstore.controller;

import com.medicalstore.model.Branch;
import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.BranchService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

/**
 * Platform Admin panel — manage owners, branches, shopkeepers.
 * Access restricted to ROLE_ADMIN only (via SecurityConfig).
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final BranchService branchService;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    // ─── Admin Dashboard ───────────────────────────────────────────────────
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("title", "Admin Panel");
        model.addAttribute("page", "admin");
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalBranches", branchService.getAllBranches().size());
        model.addAttribute("owners", userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("OWNER")).toList());
        model.addAttribute("branches", branchService.getAllBranches());
        return "admin/index";
    }

    // ─── User Management ───────────────────────────────────────────────────
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("title", "Manage Users");
        model.addAttribute("page", "admin");
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("branches", branchService.getAllBranches());
        return "admin/users";
    }

    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("title", "Create User");
        model.addAttribute("page", "admin");
        model.addAttribute("branches", branchService.getAllBranches());
        model.addAttribute("newUser", new User());
        return "admin/create-user";
    }

    @PostMapping("/users/create")
    public String createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam(required = false) Long branchId,
            RedirectAttributes ra) {

        if (userRepository.existsByUsername(username)) {
            ra.addFlashAttribute("error", "Username already exists: " + username);
            return "redirect:/admin/users/create";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setRoles(Set.of(role));

        if ("SHOPKEEPER".equals(role) && branchId != null) {
            branchService.getBranchById(branchId).ifPresent(user::setBranch);
        }

        userRepository.save(user);
        ra.addFlashAttribute("success", "User '" + username + "' created with role " + role);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        userRepository.findById(id).ifPresent(u -> {
            u.setEnabled(!u.getEnabled());
            userRepository.save(u);
        });
        ra.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        Long currentId = securityUtils.getCurrentUserId();
        if (id.equals(currentId)) {
            ra.addFlashAttribute("error", "You cannot delete your own account.");
            return "redirect:/admin/users";
        }
        userRepository.deleteById(id);
        ra.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin/users";
    }

    // ─── Branch Management ─────────────────────────────────────────────────
    @GetMapping("/branches")
    public String listBranches(Model model) {
        model.addAttribute("title", "Manage Branches");
        model.addAttribute("page", "admin");
        model.addAttribute("branches", branchService.getAllBranches());
        model.addAttribute("owners", userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("OWNER")).toList());
        return "admin/branches";
    }

    @PostMapping("/branches/create")
    public String createBranch(
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gstNumber,
            @RequestParam(required = false) String licenceNumber,
            @RequestParam Long ownerId,
            RedirectAttributes ra) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Branch branch = new Branch();
        branch.setName(name);
        branch.setAddress(address);
        branch.setPhone(phone);
        branch.setGstNumber(gstNumber);
        branch.setLicenceNumber(licenceNumber);
        branch.setOwner(owner);
        branch.setIsActive(true);
        branchService.saveBranch(branch);

        ra.addFlashAttribute("success", "Branch '" + name + "' created successfully.");
        return "redirect:/admin/branches";
    }

    @GetMapping("/branches/toggle/{id}")
    public String toggleBranch(@PathVariable Long id, RedirectAttributes ra) {
        branchService.toggleActive(id);
        ra.addFlashAttribute("success", "Branch status updated.");
        return "redirect:/admin/branches";
    }
}
