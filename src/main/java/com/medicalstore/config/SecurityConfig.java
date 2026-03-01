package com.medicalstore.config;

import com.medicalstore.config.RoutePaths;
import com.medicalstore.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
    public SecurityFilterChain filterChain(HttpSecurity http, TenantFilter tenantFilter) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        // Analytics - strict access control
                        .requestMatchers("/analytics/**").hasAnyRole("ADMIN", "OWNER", "SHOPKEEPER")
                        // Reports — all authenticated roles can view
                        .requestMatchers("/reports/**")
                        .hasAnyRole("ADMIN", "OWNER", "SHOPKEEPER")
                        // Operational endpoints — ADMIN and SHOPKEEPER only (OWNER uses /owner portal)
                        .requestMatchers("/medicines/**", "/sales/**", "/customers/**", "/returns/**",
                                "/suppliers/**", "/purchases/**")
                        .hasAnyRole("ADMIN", "SHOPKEEPER")
                        .anyRequest().authenticated())
                .csrf(csrf -> {
                    // Use non-deferred handler so the CSRF token is eagerly loaded.
                    // The default XorCsrfTokenRequestAttributeHandler defers token generation
                    // until CsrfToken.getToken() is called. On pages whose template exceeds
                    // Tomcat's 8 KB response buffer (e.g. login.html with 640 lines of CSS),
                    // the response is already committed by the time th:action triggers
                    // token resolution, causing:
                    //   IllegalStateException: Cannot create a session after the
                    //   response has been committed
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
                .requestCache(cache -> cache.requestCache(new org.springframework.security.web.savedrequest.NullRequestCache()))
                .rememberMe(rm -> rm
                        .key("medicalStoreRememberMeKey2024")
                        .tokenValiditySeconds(60 * 60 * 24 * 7)  // 7 days
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
                    response.sendRedirect(request.getContextPath() + RoutePaths.ADMIN + "?denied=true");
                    return;
                } else if (isOwner) {
                    response.sendRedirect(request.getContextPath() + "/owner?denied=true");
                    return;
                }
            }
            response.sendRedirect(request.getContextPath() + "/?denied=true");
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
                response.sendRedirect(RoutePaths.ADMIN);
            } else if (isOwner) {
                response.sendRedirect("/owner");
            } else {
                response.sendRedirect("/");
            }
        };
    }
}
