package com.medicalstore.controller;

import com.medicalstore.config.RoutePaths;
import com.medicalstore.model.Branch;
import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.DashboardService;
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
import com.medicalstore.service.RoleAuditService;

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
    private final DashboardService dashboardService;
    private final RoleAuditService roleAuditService;

    // ─── Admin Dashboard ───────────────────────────────────────────────────
    @GetMapping
    public String dashboard(Model model) {
        List<User> owners = userRepository.findByRole("OWNER");
        List<Branch> branches = branchService.getAllBranches();

        // Platform KPIs from DashboardService (global, unscoped)
        Map<String, Object> kpis = dashboardService.buildAdminDashboard();

        // Subscription summary
        long activeSubscriptions = owners.stream()
                .filter(o -> subscriptionService.isSubscriptionActive(o.getId()))
                .count();
        long expiredSubscriptions = owners.size() - activeSubscriptions;

        // Plan type breakdown
        long freePlans = owners.stream()
                .flatMap(o -> subscriptionService.getPlanForOwner(o.getId()).stream())
                .filter(p -> "FREE".equals(p.getPlanType()))
                .count();
        long proPlans = owners.stream()
                .flatMap(o -> subscriptionService.getPlanForOwner(o.getId()).stream())
                .filter(p -> "PRO".equals(p.getPlanType()))
                .count();
        long enterprisePlans = owners.stream()
                .flatMap(o -> subscriptionService.getPlanForOwner(o.getId()).stream())
                .filter(p -> "ENTERPRISE".equals(p.getPlanType()))
                .count();

        model.addAttribute("title", "Admin Panel");
        model.addAttribute("page", "admin");

        // User counts by role
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalOwners", owners.size());
        model.addAttribute("totalShopkeepers", userRepository.countByRole("SHOPKEEPER"));
        model.addAttribute("totalAdmins", userRepository.countByRole("ADMIN"));

        // Branch stats
        model.addAttribute("totalBranches", branches.size());
        model.addAttribute("activeBranches", branches.stream().filter(Branch::getIsActive).count());

        // Subscription stats
        model.addAttribute("activeSubscriptions", activeSubscriptions);
        model.addAttribute("expiredSubscriptions", expiredSubscriptions);
        model.addAttribute("freePlans", freePlans);
        model.addAttribute("proPlans", proPlans);
        model.addAttribute("enterprisePlans", enterprisePlans);

        // Revenue KPIs from platform-wide dashboard
        model.addAttribute("todaySales", kpis.getOrDefault("todaySales", 0.0));
        model.addAttribute("monthlyRevenue", kpis.getOrDefault("monthlyRevenue", 0.0));
        model.addAttribute("todayTransactions", kpis.getOrDefault("todayTransactions", 0L));

        model.addAttribute("owners", owners);
        model.addAttribute("branches", branches);
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
        model.addAttribute("owners", userRepository.findByRole("OWNER"));
        model.addAttribute("newUser", new User());
        return "admin/create-user";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "User not found.");
            return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
        }
        model.addAttribute("title", "Edit User");
        model.addAttribute("page", "admin");
        model.addAttribute("editUser", user);
        model.addAttribute("branches", branchService.getAllBranches());
        return "admin/edit-user";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "true") boolean enabled,
            @RequestParam(defaultValue = "true") boolean accountNonLocked,
            RedirectAttributes ra) {
        userRepository.findById(id).ifPresent(user -> {
            user.setFullName(fullName);
            if (email != null && !email.isBlank()) user.setEmail(email);
            user.setEnabled(enabled);
            user.setAccountNonLocked(accountNonLocked);
            if (branchId != null && user.getRoles().contains("SHOPKEEPER")) {
                branchService.getBranchById(branchId).ifPresent(user::setBranch);
            }
            userRepository.save(user);
        });
        ra.addFlashAttribute("success", "User updated successfully.");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
    }

    @PostMapping("/users/reset-password/{id}")
    public String resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            RedirectAttributes ra) {
        if (newPassword == null || newPassword.length() < 6) {
            ra.addFlashAttribute("error", "Password must be at least 6 characters.");
            return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
        }
        userRepository.findById(id).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            // Log password reset for audit trail
            roleAuditService.logPasswordReset(user.getUsername());
        });
        ra.addFlashAttribute("success", "Password reset successfully.");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
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

        User saved = userRepository.save(user);

        // Auto-provision FREE subscription plan for new OWNERs so they can log in
        // without being immediately blocked by the SubscriptionInterceptor.
        if ("OWNER".equals(role)) {
            subscriptionService.createOrUpdatePlan(
                    saved.getId(), "FREE",
                    LocalDate.now().plusYears(1), // 1-year trial by default
                    5,  // maxUsers
                    3   // maxBranches
            );
        }

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
        model.addAttribute("owners", userRepository.findByRole("OWNER"));
        return "admin/branches";
    }

    @GetMapping("/branches/edit/{id}")
    public String showEditBranchForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Branch branch = branchService.getBranchById(id).orElse(null);
        if (branch == null) {
            ra.addFlashAttribute("error", "Branch not found.");
            return RoutePaths.redirectTo(RoutePaths.ADMIN_BRANCHES);
        }
        model.addAttribute("title", "Edit Branch");
        model.addAttribute("page", "admin");
        model.addAttribute("editBranch", branch);
        model.addAttribute("owners", userRepository.findByRole("OWNER"));
        return "admin/edit-branch";
    }

    @PostMapping("/branches/edit/{id}")
    public String updateBranch(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gstNumber,
            @RequestParam(required = false) String licenceNumber,
            @RequestParam Long ownerId,
            RedirectAttributes ra) {
        branchService.getBranchById(id).ifPresent(branch -> {
            branch.setName(name);
            branch.setAddress(address);
            branch.setPhone(phone);
            branch.setGstNumber(gstNumber);
            branch.setLicenceNumber(licenceNumber);
            userRepository.findById(ownerId).ifPresent(branch::setOwner);
            branchService.saveBranch(branch);
        });
        ra.addFlashAttribute("success", "Branch updated successfully.");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_BRANCHES);
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
