package com.medicalstore.config;

import com.medicalstore.common.RoutePaths;
import com.medicalstore.security.MediStorePermissionEvaluator;
import com.medicalstore.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Value("${remember.me.key:medicalStoreRememberMeKey2024}")
    private String rememberMeKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─── Role Hierarchy: ADMIN > OWNER (OWNER does NOT inherit SHOPKEEPER) ────
    // ADMIN inherits OWNER permissions only — enabling admin impersonation of
    // /owner/** routes (view-as-owner drill-down). OWNER does NOT inherit
    // SHOPKEEPER so that OWNER users cannot access or modify operational
    // endpoints. This enforces the 3-layer SaaS separation:
    // ADMIN → Platform governance (/admin/**)
    // OWNER → Portfolio management (/owner/**)
    // SHOPKEEPER → Store operations (/medicines/**, /sales/**, etc.)
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("OWNER")
                .build();
    }

    // Apply role hierarchy and custom permission evaluator to method-level security
    // (@PreAuthorize annotations)
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy,
            MediStorePermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }

    @SuppressWarnings("deprecation")
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TenantFilter tenantFilter,
            RateLimitFilter rateLimitFilter) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        // Actuator & Swagger/OpenAPI — accessible without login
                        .requestMatchers("/actuator/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/swagger-resources/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // ADMIN explicitly granted for impersonation (view-as-owner drill-down)
                        .requestMatchers("/owner/**").hasAnyRole("OWNER", "ADMIN")
                        // Analytics — all roles can view; export endpoints restricted via @PreAuthorize
                        .requestMatchers("/analytics/**").hasAnyRole("ADMIN", "OWNER", "SHOPKEEPER")
                        // Reports — all authenticated roles can view
                        .requestMatchers("/reports/**").hasAnyRole("ADMIN", "OWNER", "SHOPKEEPER")
                        // Operational endpoints — ADMIN (read-only monitoring) and SHOPKEEPER (full
                        // operations)
                        // Write operations are further blocked at method level via
                        // @PreAuthorize("hasRole('SHOPKEEPER')")
                        .requestMatchers("/medicines/**", "/sales/**", "/customers/**", "/returns/**",
                                "/suppliers/**", "/purchases/**")
                        .hasAnyRole("ADMIN", "SHOPKEEPER")
                        // OWNER included for view-as-shopkeeper impersonation flow
                        .requestMatchers("/dashboard").hasAnyRole("ADMIN", "OWNER", "SHOPKEEPER")
                        .anyRequest().authenticated())
                .csrf(csrf -> {
                    // Use non-deferred handler so the CSRF token is eagerly loaded.
                    // The default XorCsrfTokenRequestAttributeHandler defers token generation
                    // until CsrfToken.getToken() is called. On pages whose template exceeds
                    // Tomcat's 8 KB response buffer (e.g. login.html with 640 lines of CSS),
                    // the response is already committed by the time th:action triggers
                    // token resolution, causing:
                    // IllegalStateException: Cannot create a session after the
                    // response has been committed
                    csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
                    csrf.ignoringRequestMatchers("/api/**");
                })
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .clearAuthentication(true)
                        .permitAll())
                .requestCache(cache -> cache
                        .requestCache(new org.springframework.security.web.savedrequest.NullRequestCache()))
                .rememberMe(rm -> rm
                        .key(rememberMeKey)
                        .tokenValiditySeconds(60 * 60 * 24 * 7) // 7 days
                        .userDetailsService(userDetailsService)
                        .rememberMeParameter("remember-me"))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler()))
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .maxSessionsPreventsLogin(false))
                .authenticationProvider(authenticationProvider())
                .addFilterAfter(tenantFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                // Add rate limit filter after tenant filter
                .addFilterAfter(rateLimitFilter, TenantFilter.class)
                // Eagerly materialise the CSRF token before any template bytes are flushed
                .addFilterAfter(new CsrfCookieFilter(),
                        org.springframework.security.web.csrf.CsrfFilter.class);

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            var auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null) {
                boolean isOwner = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).anyMatch("ROLE_OWNER"::equals);
                boolean isAdmin = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
                if (isAdmin) {
                    response.sendRedirect(request.getContextPath() + RoutePaths.ADMIN_DASHBOARD + "?denied=true");
                    return;
                } else if (isOwner) {
                    response.sendRedirect(request.getContextPath() + RoutePaths.OWNER_DASHBOARD + "?denied=true");
                    return;
                }
            }
            response.sendRedirect(request.getContextPath() + RoutePaths.SHOPKEEPER_DASHBOARD + "?denied=true");
        };
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            userDetailsService.updateLastLogin(authentication.getName());

            boolean isOwner = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).anyMatch("ROLE_OWNER"::equals);
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);

            if (isAdmin) {
                response.sendRedirect(RoutePaths.ADMIN_DASHBOARD);
            } else if (isOwner) {
                response.sendRedirect(RoutePaths.OWNER_DASHBOARD);
            } else {
                response.sendRedirect(RoutePaths.SHOPKEEPER_DASHBOARD);
            }
        };
    }
}
