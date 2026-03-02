package com.medicalstore.common;

import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Central utility for resolving the current user's role and branch context
 * from Spring Security's {@code SecurityContextHolder}.
 *
 * <p>Injected as a Spring bean rather than using static helpers so that the
 * {@code UserRepository} can be lazily resolved without circular-dependency
 * issues during application start-up.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final @Lazy UserRepository userRepository;

    /** Returns the full {@link User} entity for the currently authenticated principal, or {@code null} for anonymous. */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isAdmin()       { return hasRole("ADMIN"); }
    public boolean isOwner()       { return hasRole("OWNER"); }
    public boolean isShopkeeper()  { return hasRole("SHOPKEEPER"); }

    public boolean isOwnerOrShopkeeper() { return isOwner() || isShopkeeper(); }

    /** Branch ID — only non-null for SHOPKEEPER users. */
    public Long getCurrentBranchId() {
        User user = getCurrentUser();
        return (user != null && user.getBranch() != null) ? user.getBranch().getId() : null;
    }

    /** Current user's database ID. */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Resolves the canonical owner ID for the current principal:
     * <ul>
     *   <li>OWNER → own user ID</li>
     *   <li>SHOPKEEPER → branch owner's user ID</li>
     *   <li>ADMIN or unauthenticated → {@code null}</li>
     * </ul>
     */
    public Long getCurrentOwnerId() {
        if (isOwner()) {
            return getCurrentUserId();
        }
        if (!isShopkeeper()) {
            return null;
        }
        return Optional.ofNullable(getCurrentUser())
                .map(User::getBranch)
                .map(branch -> branch.getOwner() != null ? branch.getOwner().getId() : null)
                .orElse(null);
    }
}
