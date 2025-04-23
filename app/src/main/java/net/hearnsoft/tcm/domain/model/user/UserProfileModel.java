package net.hearnsoft.tcm.domain.model.user;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileModel {
    private String name;
    private String avatarUrl;
    private String bannerUrl;
    private OffsetDateTime lastLogin;
    private List<UserRole> roles = new ArrayList<>();
    private boolean isFollowing;
    private String bio;

    public UserProfileModel() {
    }

    public UserProfileModel(
        String name,
        String avatarUrl,
        String bannerUrl,
        OffsetDateTime lastLogin,
        List<UserRole> roles,
        boolean isFollowing,
        String bio
    ) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.bannerUrl = bannerUrl;
        this.lastLogin = lastLogin;
        this.roles = roles;
        this.isFollowing = isFollowing;
        this.bio = bio;
    }

    // 用于从API数据模型转换为领域模型
    public static UserProfileModel fromApiModel(net.hearnsoft.thcdb_sdk.model.UserProfile apiProfile) {
        List<UserRole> domainRoles = apiProfile.getRoles().stream()
            .map(role -> UserRole.parse(role.getName()).getOrElse(UserRole.USER))
            .collect(Collectors.toList());

        return new UserProfileModel(
            apiProfile.getName(),
            apiProfile.getAvatarUrl(),
            apiProfile.getBannerUrl(),
            apiProfile.getLastLogin(),
            domainRoles,
            apiProfile.isFollowing(),
            apiProfile.getBio()
        );
    }

    // 判断用户是否有特定权限的辅助方法
    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    // 获取最高权限的辅助方法
    public UserRole getHighestRole() {
        if (roles.contains(UserRole.ADMIN)) return UserRole.ADMIN;
        if (roles.contains(UserRole.MODERATOR)) return UserRole.MODERATOR;
        return UserRole.USER;
    }
}