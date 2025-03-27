package net.hearnsoft.tcm.domain.model.user

import java.util.Date


data class UserProfile(
    var name: String?,
    var avatarUrl: String?,
    var lastLogin: Date?,
    var roles: IntArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserProfile

        if (name != other.name) return false
        if (avatarUrl != other.avatarUrl) return false
        if (lastLogin != other.lastLogin) return false
        if (!roles.contentEquals(other.roles)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (avatarUrl?.hashCode() ?: 0)
        result = 31 * result + (lastLogin?.hashCode() ?: 0)
        result = 31 * result + roles.contentHashCode()
        return result
    }
}
