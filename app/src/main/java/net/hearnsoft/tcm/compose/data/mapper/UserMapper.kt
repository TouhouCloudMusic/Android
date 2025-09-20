package net.hearnsoft.tcm.compose.data.mapper

import net.hearnsoft.tcm.compose.domain.model.user.User
import net.hearnsoft.tcm.compose.domain.model.user.UserRole
import net.hearnsoft.tcm.compose.domain.model.auth.LoginCredential
import net.hearnsoft.thcdb_api.model.user.UserProfile
import net.hearnsoft.thcdb_api.model.user.UserRoleData
import net.hearnsoft.thcdb_api.model.AuthCredential

object UserMapper {

    fun UserProfile.toUser(): User {
         return User(
             name = this.name,
             avatarUrl = this.avatarUrl,
             bannerUrl = this.bannerUrl,
             lastLogin = this.lastLogin,
             roles = this.roles.map { it.toUserRole() },
             isFollowing = this.isFollowing ?: false,
             bio = this.bio
         )
    }

    fun UserRoleData.toUserRole(): UserRole {
        return UserRole(
            id = this.id,
            name = this.name
        )
    }

    fun LoginCredential.toAuthCredential(): AuthCredential {
        return AuthCredential(
            username = this.username,
            password = this.password
        )
    }

}