package net.hearnsoft.tcm.compose.domain.repository

import android.net.Uri
import net.hearnsoft.tcm.compose.domain.model.user.User
import net.hearnsoft.tcm.compose.domain.model.auth.LoginCredential

interface UserRepository {

    /**
     * 获取当前登录用户的个人资料
     */
    suspend fun getMyProfile(): Result<User>

    /**
     * 获取指定用户名的用户个人资料
     */
    suspend fun getUserProfile(name: String): Result<User>

    /**
     * 用户登录
     */
    suspend fun signIn(credential: LoginCredential): Result<User>

    /**
     * 用户登出
     */
    suspend fun signOut(): Result<Unit>

    /**
     * 用户注册
     */
    suspend fun signUp(credential: LoginCredential): Result<User>

    /**
     * 上传用户头像
     */
    suspend fun uploadAvatar(avatarUri: Uri): Result<Unit>

    /**
     * 上传用户背景横幅图
     */
    suspend fun uploadProfileBanner(bannerUri: Uri): Result<Unit>

    /**
     * 更新个人简介
     */
    suspend fun updateBio(bio: String): Result<Unit>
}