package com.medicalstore.model;

/**
 * Compile-time safe role constants used throughout the system.
 * The raw string values ("ADMIN", "OWNER", "SHOPKEEPER") are the values
 * persisted in the user_roles table and used for Spring Security authority
 * comparison with the "ROLE_" prefix.
 */
public enum Role {

    ADMIN("ADMIN"),
    OWNER("OWNER"),
    SHOPKEEPER("SHOPKEEPER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    /** Raw string stored in DB / used in Set<String> roles */
    public String getValue() {
        return value;
    }

    /** Spring Security authority name, e.g. "ROLE_ADMIN" */
    public String getAuthority() {
        return "ROLE_" + value;
    }

    @Override
    public String toString() {
        return value;
    }

    /** Safely parse a string to a Role, returns null if unknown */
    public static Role from(String s) {
        if (s == null) return null;
        for (Role r : values()) {
            if (r.value.equalsIgnoreCase(s.trim())) return r;
        }
        return null;
    }
}
