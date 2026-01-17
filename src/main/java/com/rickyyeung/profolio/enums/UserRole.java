package com.rickyyeung.profolio.enums;
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

    public String getRoleName() { return roleName; }
    public int getCode() { return code; }

    public static UserRole fromCode(int code) {
        for (UserRole role : UserRole.values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("無效的角色代碼: " + code);
    }
}