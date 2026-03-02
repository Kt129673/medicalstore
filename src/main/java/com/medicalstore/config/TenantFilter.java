package com.medicalstore.config;

import com.medicalstore.model.Branch;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.common.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to extract tenant (branch/owner) context from the authenticated user
 * and set it in the TenantContext for the lifecycle of the request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    private final @org.springframework.context.annotation.Lazy UserRepository userRepository;
    private final @org.springframework.context.annotation.Lazy SecurityUtils securityUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            userRepository.findByUsernameWithBranch(username).ifPresent(user -> {
                try {
                    // Set Owner Context
                    if (securityUtils.isOwner()) {
                        TenantContext.setOwnerId(user.getId());
                    }

                    // Set Branch (Tenant) Context
                    Branch branch = user.getBranch();
                    if (branch != null) {
                        TenantContext.setTenantId(branch.getId());
                    }

                } catch (Exception e) {
                    log.error("Error setting tenant context for user: {}", username, e);
                }
            });
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clean up thread-local variables to prevent memory leaks and
            // contamination
            TenantContext.clear();
        }
    }
}
