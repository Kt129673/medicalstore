package com.medicalstore.service;

import com.medicalstore.exception.BusinessException;
import com.medicalstore.exception.ResourceNotFoundException;
import com.medicalstore.model.Branch;
import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Centralises all user lifecycle operations — creation, update, password management,
 * soft delete, restore, enable/disable, and role-based lookup.
 *
 * <p>Both {@code AdminController} and {@code OwnerController} must use this service
 * rather than injecting {@code UserRepository} directly.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManagementService {

    /** Roles an admin may assign when creating users. */
    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "OWNER", "SHOPKEEPER");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;

    // ── Read ────────────────────────────────────────────────────────────────

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }

    public long count() {
        return userRepository.count();
    }

    public long countByRole(String role) {
        return userRepository.countByRole(role);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> findDeletedUsers() {
        return userRepository.findDeletedUsers();
    }

    public List<User> findShopkeepersByOwnerId(Long ownerId) {
        return userRepository.findShopkeepersByOwnerId(ownerId);
    }

    public List<User> findShopkeepersByBranchId(Long branchId) {
        return userRepository.findShopkeepersByBranchId(branchId);
    }

    // ── Writes ──────────────────────────────────────────────────────────────

    /**
     * Creates a new user with the given fields.
     *
     * <p>Business rules enforced here:
     * <ul>
     *   <li>Username uniqueness</li>
     *   <li>Email uniqueness</li>
     *   <li>Role must be one of ADMIN, OWNER, SHOPKEEPER</li>
     *   <li>New OWNER users automatically receive a FREE 1-year subscription plan</li>
     * </ul>
     * </p>
     *
     * @param branch Optional — required when role is SHOPKEEPER
     */
    @Transactional
    public User createUser(String username, String password, String fullName, String email,
                           String role, Branch branch) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("USERNAME_TAKEN", "Username already exists: " + username);
        }
        if (!ALLOWED_ROLES.contains(role)) {
            throw new BusinessException("INVALID_ROLE", "Invalid role: " + role);
        }
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new BusinessException("EMAIL_TAKEN", "Email already registered: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setRoles(Set.of(role));

        if ("SHOPKEEPER".equals(role) && branch != null) {
            user.setBranch(branch);
        }

        User saved = userRepository.save(user);

        // Auto-provision FREE subscription plan for new OWNERs
        if ("OWNER".equals(role)) {
            subscriptionService.createOrUpdatePlan(
                    saved.getId(), "FREE",
                    LocalDate.now().plusYears(1),
                    5,  // maxUsers
                    3   // maxBranches
            );
        }

        return saved;
    }

    /**
     * Updates mutable fields on an existing user.
     *
     * @param branch Only applied when the user is a SHOPKEEPER
     */
    @Transactional
    public User updateUser(Long id, String fullName, String email,
                           boolean enabled, boolean accountNonLocked, Branch branch) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setFullName(fullName);
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        user.setEnabled(enabled);
        user.setAccountNonLocked(accountNonLocked);
        if (branch != null && user.getRoles().contains("SHOPKEEPER")) {
            user.setBranch(branch);
        }

        return userRepository.save(user);
    }

    /**
     * Encodes and sets a new password. Requires minimum 6 characters.
     */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("WEAK_PASSWORD", "Password must be at least 6 characters.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Flips the enabled flag for a user.
     */
    @Transactional
    public void toggleEnabled(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
    }

    /**
     * Soft-deletes a user (triggers {@code @SQLDelete} on the User entity).
     *
     * @param id            User to delete
     * @param requesterId   ID of the admin performing the action — cannot delete self
     */
    @Transactional
    public void deleteUser(Long id, Long requesterId) {
        if (id.equals(requesterId)) {
            throw new BusinessException("SELF_DELETE", "You cannot delete your own account.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user); // triggers @SQLDelete — soft delete
    }

    /**
     * Restores a soft-deleted user by clearing the {@code is_deleted} flag.
     */
    @Transactional
    public void restoreUser(Long id) {
        userRepository.restoreUser(id);
    }

    /**
     * Toggles a shopkeeper's enabled state, verifying ownership.
     *
     * @param shopkeeperId  ID of the shopkeeper to toggle
     * @param ownerBranchIds Branch IDs owned by the requesting owner
     */
    @Transactional
    public void toggleShopkeeper(Long shopkeeperId, Set<Long> ownerBranchIds) {
        User shopkeeper = userRepository.findById(shopkeeperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shopkeeper", shopkeeperId));

        if (!shopkeeper.getRoles().contains("SHOPKEEPER")
                || shopkeeper.getBranch() == null
                || !ownerBranchIds.contains(shopkeeper.getBranch().getId())) {
            throw new BusinessException("ACCESS_DENIED",
                    "Access denied: shopkeeper not in your branches.");
        }

        shopkeeper.setEnabled(!shopkeeper.getEnabled());
        userRepository.save(shopkeeper);
    }
}
