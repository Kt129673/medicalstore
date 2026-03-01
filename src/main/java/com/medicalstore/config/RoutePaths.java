package com.medicalstore.config;

public final class RoutePaths {

    private RoutePaths() {
    }

    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String ERROR = "/error";
    public static final String SUBSCRIPTION_BILLING = "/subscription/billing";

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
}