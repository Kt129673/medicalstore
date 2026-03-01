package com.medicalstore.controller;

import com.medicalstore.config.RoutePaths;
import com.medicalstore.model.Branch;
import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.SubscriptionService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Platform Admin panel — manage owners, branches, shopkeepers.
 * Access restricted to ROLE_ADMIN only (via SecurityConfig).
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    /** Allowed role values an admin can assign to a new user. */
    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "OWNER", "SHOPKEEPER");

    private final UserRepository userRepository;
    private final BranchService branchService;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;

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
            return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS_CREATE);
        }

        if (!ALLOWED_ROLES.contains(role)) {
            ra.addFlashAttribute("error", "Invalid role: " + role);
            return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS_CREATE);
        }

        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            ra.addFlashAttribute("error", "Email already registered: " + email);
            return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS_CREATE);
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
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        userRepository.findById(id).ifPresent(u -> {
            u.setEnabled(!u.getEnabled());
            userRepository.save(u);
        });
        ra.addFlashAttribute("success", "User status updated.");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        Long currentId = securityUtils.getCurrentUserId();
        if (id.equals(currentId)) {
            ra.addFlashAttribute("error", "You cannot delete your own account.");
            return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
        }
        userRepository.deleteById(id);
        ra.addFlashAttribute("success", "User deleted.");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
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
        return RoutePaths.redirectTo(RoutePaths.ADMIN_BRANCHES);
    }

    @PostMapping("/branches/toggle/{id}")
    public String toggleBranch(@PathVariable Long id, RedirectAttributes ra) {
        branchService.toggleActive(id);
        ra.addFlashAttribute("success", "Branch status updated.");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_BRANCHES);
    }

    // ─── Subscription Management ───────────────────────────────────────────
    @GetMapping("/subscriptions")
    public String listSubscriptions(Model model) {
        List<User> owners = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains("OWNER")).toList();

        Map<Long, com.medicalstore.model.SubscriptionPlan> plansByOwner = owners.stream()
                .map(o -> Map.entry(o.getId(), subscriptionService.getPlanForOwner(o.getId())))
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

        model.addAttribute("title", "Subscription Management");
        model.addAttribute("page", "admin");
        model.addAttribute("owners", owners);
        model.addAttribute("plansByOwner", plansByOwner);
        return "admin/subscriptions";
    }

    @PostMapping("/subscriptions/update")
    public String updateSubscription(
            @RequestParam Long ownerId,
            @RequestParam String planType,
            @RequestParam String expiryDate,
            @RequestParam(defaultValue = "10") int maxUsers,
            @RequestParam(defaultValue = "5") int maxBranches,
            RedirectAttributes ra) {
        try {
            if (expiryDate == null || expiryDate.isBlank()) {
                ra.addFlashAttribute("error", "Expiry date is required.");
                return RoutePaths.redirectTo(RoutePaths.ADMIN_SUBSCRIPTIONS);
            }
            LocalDate parsedDate = LocalDate.parse(expiryDate);
            subscriptionService.createOrUpdatePlan(ownerId, planType, parsedDate, maxUsers, maxBranches);
            String ownerName = userRepository.findById(ownerId).map(User::getUsername).orElse("Unknown");
            ra.addFlashAttribute("success", "Subscription updated for owner: " + ownerName);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update subscription: " + e.getMessage());
        }
        return RoutePaths.redirectTo(RoutePaths.ADMIN_SUBSCRIPTIONS);
    }
}
