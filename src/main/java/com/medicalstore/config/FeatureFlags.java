package com.medicalstore.config;

/**
 * FeatureFlags — Central configuration for role-based feature access.
 * 
 * Allows toggling features per role without code changes.
 * Useful for:
 * - A/B testing features with specific roles
 * - Soft-launching features to admins first
 * - Gradual rollout to all roles
 * 
 * Example:
 * if (FeatureFlags.isFeatureEnabled("ADVANCED_ANALYTICS", userRole)) {
 * // Show advanced analytics to this role
 * }
 */
public class FeatureFlags {

    // ───── FEATURE DEFINITIONS ─────
    // Format: "FEATURE_NAME" → {ADMIN, OWNER, SHOPKEEPER, ...}

    public static final String ADVANCED_ANALYTICS = "ADVANCED_ANALYTICS";
    public static final String BULK_OPERATIONS = "BULK_OPERATIONS";
    public static final String EXPORT_REPORTS = "EXPORT_REPORTS";
    public static final String API_ACCESS = "API_ACCESS";
    public static final String MULTI_BRANCH_COMPARISON = "MULTI_BRANCH_COMPARISON";
    public static final String SUBSCRIPTION_MANAGEMENT = "SUBSCRIPTION_MANAGEMENT";
    public static final String USER_MANAGEMENT = "USER_MANAGEMENT";
    public static final String CUSTOM_REPORTS = "CUSTOM_REPORTS";

    /**
     * Check if a feature is enabled for a specific role.
     * 
     * @param featureName The feature identifier
     * @param role        The user's role
     * @return true if feature is enabled for this role
     */
    public static boolean isFeatureEnabled(String featureName, String role) {
        return switch (featureName) {
            case ADVANCED_ANALYTICS -> "ADMIN".equals(role) || "OWNER".equals(role);
            case BULK_OPERATIONS -> "ADMIN".equals(role);
            case EXPORT_REPORTS -> !"SHOPKEEPER".equals(role); // Enabled for ADMIN and OWNER
            case API_ACCESS -> "ADMIN".equals(role);
            case MULTI_BRANCH_COMPARISON -> "OWNER".equals(role) || "ADMIN".equals(role);
            case SUBSCRIPTION_MANAGEMENT -> "OWNER".equals(role) || "ADMIN".equals(role);
            case USER_MANAGEMENT -> "ADMIN".equals(role);
            case CUSTOM_REPORTS -> "ADMIN".equals(role) || "OWNER".equals(role);
            default -> false;
        };
    }

    /**
     * Get all enabled features for a role.
     */
    public static java.util.List<String> getEnabledFeaturesForRole(String role) {
        java.util.List<String> features = new java.util.ArrayList<>();

        // Check all features
        if (isFeatureEnabled(ADVANCED_ANALYTICS, role))
            features.add(ADVANCED_ANALYTICS);
        if (isFeatureEnabled(BULK_OPERATIONS, role))
            features.add(BULK_OPERATIONS);
        if (isFeatureEnabled(EXPORT_REPORTS, role))
            features.add(EXPORT_REPORTS);
        if (isFeatureEnabled(API_ACCESS, role))
            features.add(API_ACCESS);
        if (isFeatureEnabled(MULTI_BRANCH_COMPARISON, role))
            features.add(MULTI_BRANCH_COMPARISON);
        if (isFeatureEnabled(SUBSCRIPTION_MANAGEMENT, role))
            features.add(SUBSCRIPTION_MANAGEMENT);
        if (isFeatureEnabled(USER_MANAGEMENT, role))
            features.add(USER_MANAGEMENT);
        if (isFeatureEnabled(CUSTOM_REPORTS, role))
            features.add(CUSTOM_REPORTS);

        return features;
    }
}
