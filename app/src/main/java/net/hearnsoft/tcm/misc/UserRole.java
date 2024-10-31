package net.hearnsoft.tcm.misc;

public enum UserRole {
    ADMIN("Admin"),
    MODERATOR("Moderator"),
    USER("User");

    private final String name;

    UserRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static UserRole fromString(String name) {
        for (UserRole role : values()) {
            if (role.getName().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null; // 如果没有匹配的角色，返回 null
    }
}
