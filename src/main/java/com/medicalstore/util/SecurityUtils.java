package com.medicalstore.util;

import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Central utility for resolving the current user's role and branch context
 * from Spring Security's SecurityContextHolder.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

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

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isOwner() {
        return hasRole("OWNER");
    }

    public boolean isShopkeeper() {
        return hasRole("SHOPKEEPER");
    }

    /** Branch ID — only non-null for SHOPKEEPER */
    public Long getCurrentBranchId() {
        User user = getCurrentUser();
        return (user != null && user.getBranch() != null) ? user.getBranch().getId() : null;
    }

    /** Current user's DB id */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
}
