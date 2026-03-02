package com.medicalstore.controller;

import com.medicalstore.common.RoutePaths;
import com.medicalstore.model.Branch;
import com.medicalstore.model.User;
import com.medicalstore.service.AuditLogService;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.DashboardService;
import com.medicalstore.service.RoleAuditService;
import com.medicalstore.service.SubscriptionService;
import com.medicalstore.service.UserManagementService;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    private final UserManagementService userManagementService;
    private final BranchService branchService;
    private final SecurityUtils securityUtils;
    private final SubscriptionService subscriptionService;
    private final DashboardService dashboardService;
    private final RoleAuditService roleAuditService;
    private final AuditLogService auditLogService;

    // ─── Admin Dashboard ───────────────────────────────────────────────────
    /** Canonical dashboard URL — redirect legacy /admin to /admin/dashboard. */
    @GetMapping
    public String dashboardRedirect() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> owners = userManagementService.findByRole("OWNER");
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
        model.addAttribute("totalUsers", userManagementService.count());
        model.addAttribute("totalOwners", owners.size());
        model.addAttribute("totalShopkeepers", userManagementService.countByRole("SHOPKEEPER"));
        model.addAttribute("totalAdmins", userManagementService.countByRole("ADMIN"));

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
        model.addAttribute("users", userManagementService.findAll());
        model.addAttribute("branches", branchService.getAllBranches());
        return "admin/users";
    }

    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("title", "Create User");
        model.addAttribute("page", "admin");
        model.addAttribute("branches", branchService.getAllBranches());
        model.addAttribute("owners", userManagementService.findByRole("OWNER"));
        model.addAttribute("newUser", new User());
        return "admin/create-user";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        User user = userManagementService.findById(id).orElse(null);
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
        Branch branch = (branchId != null) ? branchService.getBranchById(branchId).orElse(null) : null;
        userManagementService.updateUser(id, fullName, email, enabled, accountNonLocked, branch);
        ra.addFlashAttribute("success", "User updated successfully.");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
    }

    @PostMapping("/users/reset-password/{id}")
    public String resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            RedirectAttributes ra) {
        userManagementService.resetPassword(id, newPassword);
        userManagementService.findById(id).ifPresent(u ->
                roleAuditService.logPasswordReset(u.getUsername()));
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
        Branch branch = (branchId != null) ? branchService.getBranchById(branchId).orElse(null) : null;
        User saved = userManagementService.createUser(username, password, fullName, email, role, branch);
        ra.addFlashAttribute("success", "User '" + saved.getUsername() + "' created with role " + role);
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        userManagementService.toggleEnabled(id);
        ra.addFlashAttribute("success", "User status updated.");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
    }

    @PostMapping("/users/delete/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'USER_DELETE')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        Long currentId = securityUtils.getCurrentUserId();
        userManagementService.deleteUser(id, currentId);
        roleAuditService.logAction("USER_DELETED", "Soft-deleted user ID: " + id);
        ra.addFlashAttribute("success", "User deleted (soft delete — restorable).");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
    }

    // ─── Deleted Users (soft-delete recycle-bin view) ──────────────────────
    @GetMapping("/users/deleted")
    public String deletedUsers(Model model) {
        model.addAttribute("title", "Deleted Users");
        model.addAttribute("page", "admin");
        model.addAttribute("deletedUsers", userManagementService.findDeletedUsers());
        return "admin/deleted-users";
    }

    @PostMapping("/users/restore/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasPermission(null, 'USER_RESTORE')")
    public String restoreUser(@PathVariable Long id, RedirectAttributes ra) {
        userManagementService.restoreUser(id);
        roleAuditService.logAction("USER_RESTORED",
                "Restored soft-deleted user ID: " + id);
        ra.addFlashAttribute("success", "User restored successfully.");
        return RoutePaths.redirectTo(RoutePaths.ADMIN_USERS);
    }

    // ─── Branch Management ─────────────────────────────────────────────────
    @GetMapping("/branches")
    public String listBranches(Model model) {
        model.addAttribute("title", "Manage Branches");
        model.addAttribute("page", "admin");
        model.addAttribute("branches", branchService.getAllBranches());
        model.addAttribute("owners", userManagementService.findByRole("OWNER"));
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
        model.addAttribute("owners", userManagementService.findByRole("OWNER"));
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
            userManagementService.findById(ownerId).ifPresent(branch::setOwner);
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

        User owner = userManagementService.findById(ownerId)
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
        List<User> owners = userManagementService.findAll().stream()
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
            String ownerName = userManagementService.findById(ownerId)
                    .map(User::getUsername).orElse("Unknown");
            ra.addFlashAttribute("success", "Subscription updated for owner: " + ownerName);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update subscription: " + e.getMessage());
        }
        return RoutePaths.redirectTo(RoutePaths.ADMIN_SUBSCRIPTIONS);
    }

    // ─── Admin → Owner Impersonation (Section 4.1) ─────────────────────────
    /**
     * Enter Owner View mode: admin sees the Owner Dashboard for a specific owner,
     * with a prominent banner — "Viewing as Owner: [Name] (Admin Mode)".
     *
     * The owner's data is fetched directly; the admin's authentication is unchanged.
     * This is a read-only view so the admin retains full admin rights.
     *
     * Flow:
     *   1. Admin clicks "View as Owner" next to an owner's name.
     *   2. System renders the Owner Dashboard for that owner.
     *   3. A top banner identifies the impersonation context.
     *   4. Admin can exit back to /admin.
     */
    @GetMapping("/view-as-owner/{ownerId}")
    public String viewAsOwner(@PathVariable Long ownerId, Model model, RedirectAttributes ra) {
        User owner = userManagementService.findById(ownerId).orElse(null);
        if (owner == null || !owner.getRoles().contains("OWNER")) {
            ra.addFlashAttribute("error", "Owner not found.");
            return RoutePaths.redirectTo(RoutePaths.ADMIN);
        }

        List<Branch> branches = branchService.getBranchesByOwner(ownerId);
        Map<String, Object> kpis = dashboardService.buildOwnerDashboard(ownerId);

        model.addAttribute("title", "Viewing as Owner: " + owner.getFullName());
        model.addAttribute("page", "owner");
        model.addAttribute("branches", branches);
        model.addAttribute("branchCount", branches.size());
        model.addAttribute("shopkeeperCount",
                userManagementService.findShopkeepersByOwnerId(ownerId).size());
        model.addAttribute("lowStock", kpis.get("lowStockCount"));
        kpis.forEach(model::addAttribute);

        // Impersonation context — rendered as a banner in owner/dashboard.html
        model.addAttribute("impersonationMode", true);
        model.addAttribute("viewingOwnerName", owner.getFullName());
        model.addAttribute("exitOwnerViewUrl", RoutePaths.ADMIN);

        roleAuditService.logAction("ADMIN_VIEW_AS_OWNER",
                "Admin viewing Owner dashboard for: " + owner.getUsername());

        return "owner/dashboard";
    }

    // ─── Audit Logs ────────────────────────────────────────────────────────
    @GetMapping("/audit-logs")
    public String auditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime fromDt = from != null && !from.isBlank()
                ? LocalDate.parse(from).atStartOfDay() : null;
        LocalDateTime toDt = to != null && !to.isBlank()
                ? LocalDate.parse(to).atTime(23, 59, 59) : null;

        String usernameFilter = (username != null && !username.isBlank()) ? username : null;
        String actionFilter   = (action   != null && !action.isBlank())   ? action   : null;

        Page<com.medicalstore.model.AuditLog> logs = auditLogService.search(
                usernameFilter, actionFilter, fromDt, toDt, pageable);

        model.addAttribute("title", "Audit Logs");
        model.addAttribute("page", "admin");
        model.addAttribute("logs", logs);
        model.addAttribute("filterUsername", username);
        model.addAttribute("filterAction", action);
        model.addAttribute("filterFrom", from);
        model.addAttribute("filterTo", to);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logs.getTotalPages());
        model.addAttribute("totalElements", logs.getTotalElements());

        return "admin/audit-logs";
    }
}
