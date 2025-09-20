package net.hearnsoft.tcm.compose.domain.model.user

import java.time.OffsetDateTime

data class User(
    val name: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val lastLogin: OffsetDateTime?,
    val roles: List<UserRole>,
    val isFollowing: Boolean?,
    val bio: String?,
)
