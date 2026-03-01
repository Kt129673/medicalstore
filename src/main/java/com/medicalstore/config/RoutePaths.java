package com.medicalstore.config;

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
    public static final String ADMIN_USERS = "/admin/users";
    public static final String ADMIN_USERS_CREATE = "/admin/users/create";
    public static final String ADMIN_BRANCHES = "/admin/branches";

    public static final String REDIRECT_PREFIX = "redirect:";

    public static final String[] STATIC_PREFIXES = { "/css", "/js", "/images" };

    public static final String[] MVC_EXCLUDE_PATTERNS = {
            "/css/**",
            "/js/**",
            "/images/**",
            ERROR,
            LOGIN,
            LOGOUT,
            SUBSCRIPTION_BILLING
    };

    public static boolean isPublicOrStatic(String uri) {
        if (uri == null || uri.isBlank()) {
            return true;
        }

        for (String prefix : STATIC_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }

        return uri.equals(LOGIN)
                || uri.equals(LOGOUT)
                || uri.equals(ERROR)
                || uri.startsWith(SUBSCRIPTION_BILLING);
    }

    public static String redirectTo(String path) {
        return REDIRECT_PREFIX + path;
    }
}