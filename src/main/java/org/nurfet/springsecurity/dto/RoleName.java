package org.nurfet.springsecurity.dto;

public enum RoleName {
    ROLE_USER,
    ROLE_ADMIN;

    public static boolean isValid(String roleName) {
        for (RoleName r : values()) {
            if (r.name().equals(roleName)) {
                return true;
            }
        }
        return false;
    }
}
