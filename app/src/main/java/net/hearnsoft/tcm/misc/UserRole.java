package net.hearnsoft.tcm.misc;

public enum UserRole {
    ADMIN(1, "Admin"),
    MODERATOR(2, "Moderator"),
    USER(3, "User");

    private final int id;
    private final String name;

    UserRole(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static UserRole fromId(int id) {
        for (UserRole role : values()) {
            if (role.getId() == id) {
                return role;
            }
        }
        return null;
    }
}
