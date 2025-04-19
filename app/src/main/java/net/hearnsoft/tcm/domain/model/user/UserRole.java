package net.hearnsoft.tcm.domain.model.user;

import io.vavr.control.Option;
import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("Admin"),
    MODERATOR("Moderator"),
    USER("User");

    private final String name;

    UserRole(String name) {
        this.name = name;
    }

    public static Option<UserRole> parse(String name) {
        for (UserRole role : values()) {
            if (role.name.equalsIgnoreCase(name)) {
                return Option.some(role);
            }
        }
        return Option.none();
    }

    public int getIndex() {
        return ordinal() + 1;
    }
}
