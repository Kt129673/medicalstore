package com.medicalstore.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j.
 * 
 * Rate limits:
 * - SHOPKEEPER: 500 requests per hour
 * - OWNER: 1000 requests per hour  
 * - ADMIN: Unlimited
 * 
 * Applies per-user rate limiting using in-memory buckets (suitable for monolith).
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // In-memory bucket cache: userId -> Bucket
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();
    
    // Rate limit configurations
    private static final long SHOPKEEPER_LIMIT = 500;
    private static final long OWNER_LIMIT = 1000;
    private static final Duration REFILL_DURATION = Duration.ofHours(1);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Skip rate limiting for unauthenticated users and static resources
        if (authentication == null || !authentication.isAuthenticated() 
                || authentication.getPrincipal().equals("anonymousUser")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String username = authentication.getName();
        String role = extractRole(authentication);
        
        // ADMIN has unlimited access
        if ("ROLE_ADMIN".equals(role)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get or create bucket for this user
        Bucket bucket = bucketCache.computeIfAbsent(username, k -> createBucket(role));
        
        // Try to consume 1 token
        if (bucket.tryConsume(1)) {
            // Request allowed
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long availableTokens = bucket.getAvailableTokens();
            log.warn("RATE_LIMIT_EXCEEDED - User: {}, Role: {}, Limit: {}/hour, Path: {}", 
                    username, role, getRateLimit(role), request.getRequestURI());
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(String.format(
                "{\"error\": \"Rate limit exceeded\", " +
                "\"message\": \"You have exceeded your rate limit of %d requests per hour. Please try again later.\", " +
                "\"limit\": %d, " +
                "\"retryAfter\": \"1 hour\"}",
                getRateLimit(role), getRateLimit(role)
            ));
        }
    }
    
    /**
     * Extract the primary role from authentication.
     */
    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_SHOPKEEPER"); // Default to most restrictive
    }
    
    /**
     * Create a rate limit bucket based on role.
     */
    private Bucket createBucket(String role) {
        long capacity = getRateLimit(role);
        
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, REFILL_DURATION));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    /**
     * Get rate limit for a given role.
     */
    private long getRateLimit(String role) {
        return switch (role) {
            case "ROLE_OWNER" -> OWNER_LIMIT;
            case "ROLE_SHOPKEEPER" -> SHOPKEEPER_LIMIT;
            default -> SHOPKEEPER_LIMIT; // Most restrictive as default
        };
    }
    
    /**
     * Skip rate limiting for static resources and login pages.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") ||
               path.startsWith("/login") ||
               path.startsWith("/error");
    }
}
