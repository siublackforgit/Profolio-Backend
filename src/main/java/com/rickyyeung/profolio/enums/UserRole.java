package com.rickyyeung.profolio.enums;

import java.util.Arrays;

public enum UserRole {
    ROOT(1, "ROOT"),
    ADMIN(2, "ADMIN"),
    USER(3, "USER");

    private final int code;
    private final String roleName;

    UserRole(int code, String roleName) {
        this.code = code;
        this.roleName = roleName;
    }

    // Getter
    public int getCode() { return code; }
    public String getRoleName() { return roleName; }

    public static UserRole fromCode(int code) {
        return Arrays.stream(UserRole.values())
                .filter(role -> role.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("cannot find the Role: " + code));
    }

    public static UserRole fromRoleName(String roleName) {
        return Arrays.stream(UserRole.values())
                .filter(role -> role.roleName.equalsIgnoreCase(roleName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("cannot find the Role: " + roleName));
    }

    public static String getNameByCode(int code) {
        return fromCode(code).getRoleName();
    }

    public static int getCodeByName(String roleName) {
        return fromRoleName(roleName).getCode();
    }
}