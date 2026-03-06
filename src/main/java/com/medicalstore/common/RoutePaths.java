package com.medicalstore.common;

import java.util.List;

/**
 * Application-wide URL path constants.
 *
 * <p>
 * Centralised here so that controllers, interceptors, and the security
 * configuration all refer to the same string literals, eliminating typo-induced
 * routing bugs.
 */
public final class RoutePaths {

    private RoutePaths() {
    }

    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String ERROR = "/error";
    public static final String SUBSCRIPTION_BILLING = "/subscription/billing";
    public static final String MEDICINES = "/medicines";
    public static final String CUSTOMERS = "/customers";
    public static final String SUPPLIERS = "/suppliers";
    public static final String ADMIN = "/admin";
    public static final String ADMIN_DASHBOARD = "/admin/dashboard";
    public static final String ADMIN_USERS = "/admin/users";
    public static final String ADMIN_USERS_CREATE = "/admin/users/create";
    public static final String ADMIN_BRANCHES = "/admin/branches";
    public static final String ADMIN_SUBSCRIPTIONS = "/admin/subscriptions";

    public static final String OWNER_DASHBOARD = "/owner/dashboard";
    public static final String SHOPKEEPER_DASHBOARD = "/dashboard";

    public static final String REDIRECT_PREFIX = "redirect:";

    private static final List<String> STATIC_PREFIX_LIST = List.of("/css", "/js", "/images");

    /** Returns a defensive copy of static prefixes for external use. */
    public static String[] getStaticPrefixes() {
        return STATIC_PREFIX_LIST.toArray(new String[0]);
    }

    private static final List<String> MVC_EXCLUDE_PATTERN_LIST = List.of(
            "/css/**",
            "/js/**",
            "/images/**",
            ERROR,
            LOGIN,
            LOGOUT,
            SUBSCRIPTION_BILLING);

    /** Returns a defensive copy of MVC exclude patterns for external use. */
    public static String[] getMvcExcludePatterns() {
        return MVC_EXCLUDE_PATTERN_LIST.toArray(new String[0]);
    }

    /**
     * Returns {@code true} if the URI targets a static resource or a public
     * endpoint.
     */
    public static boolean isPublicOrStatic(String uri) {
        if (uri == null || uri.isBlank()) {
            return true;
        }
        for (String prefix : STATIC_PREFIX_LIST) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return LOGIN.equals(uri)
                || LOGOUT.equals(uri)
                || ERROR.equals(uri)
                || uri.startsWith(SUBSCRIPTION_BILLING);
    }

    /**
     * Prepends {@code "redirect:"} to the given path for use in Spring MVC return
     * values.
     */
    public static String redirectTo(String path) {
        return REDIRECT_PREFIX + path;
    }
}
