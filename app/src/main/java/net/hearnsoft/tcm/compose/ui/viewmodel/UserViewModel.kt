package net.hearnsoft.tcm.compose.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.hearnsoft.tcm.compose.domain.model.auth.AuthState
import net.hearnsoft.tcm.compose.domain.model.auth.LoginCredential
import net.hearnsoft.tcm.compose.domain.model.user.User
import net.hearnsoft.tcm.compose.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // 当前登录用户的状态
    private val _user = MutableStateFlow<User?>(null)
    val user : StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading : StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error : StateFlow<String?> = _error.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 查看其他用户资料时的状态
    private val _profileUser = MutableStateFlow<User?>(null)
    val profileUser: StateFlow<User?> = _profileUser.asStateFlow()

    private val _profileLoading = MutableStateFlow(false)
    val profileLoading: StateFlow<Boolean> = _profileLoading.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun signIn(credential: LoginCredential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            userRepository.signIn(credential)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                    _user.value = user
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "登录失败")
                }
        }
    }

    fun signUp(credential: LoginCredential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            userRepository.signUp(credential)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                    _user.value = user
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "注册失败")
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userRepository.signOut()
                .onSuccess {
                    _successMessage.value = "已登出"
                    _authState.value = AuthState.NotAuthenticated
                    _user.value = null
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "登出失败")
                }
        }
    }

    fun uploadAvatar(avatarUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            userRepository.uploadAvatar(avatarUri)
                .onSuccess {
                    _successMessage.value = "头像更新成功"
                    // 成功后重新获取用户信息
                    getMyProfile()
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    fun uploadProfileBanner(bannerUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            userRepository.uploadProfileBanner(bannerUri)
                .onSuccess {
                    _successMessage.value = "横幅更新成功"
                    // 成功后重新获取用户信息
                    getMyProfile()
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    fun updateBioText(bio: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            userRepository.updateBio(bio)
                .onSuccess {
                    _successMessage.value = "简介更新成功"
                    // 成功后重新获取用户信息
                    getMyProfile()
                }
                .onFailure {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    /**
     * 获取指定用户名的用户资料
     */
    fun getUserProfile(username: String) {
        viewModelScope.launch {
            _profileLoading.value = true
            _profileError.value = null

            userRepository.getUserProfile(username)
                .onSuccess { user ->
                    _successMessage.value = "资料获取成功"
                    _profileUser.value = user
                }
                .onFailure { exception ->
                    _profileError.value = exception.message ?: "获取用户资料失败"
                }
                .also {
                    _profileLoading.value = false
                }
        }
    }

    private fun getMyProfile() {
        viewModelScope.launch {
            userRepository.getMyProfile()
                .onSuccess { profile ->
                    _successMessage.value = "资料更新成功"
                    _user.value = profile
                    _authState.value = AuthState.Authenticated(profile)
                }
                .onFailure { throwable ->
                    _error.value = throwable.message
                }
                .also {
                    _isLoading.value = false
                }
        }
    }

    // 检查当前认证状态
    fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            userRepository.getMyProfile()
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                    _user.value = user
                }
                .onFailure { exception ->
                    // 区分网络错误和认证错误
                    when {
                        // 网络超时或连接错误，保持之前的认证状态或设为错误状态
                        isNetworkError(exception) -> {
                            _authState.value = AuthState.NetworkError(exception.message ?: "网络连接失败")
                        }
                        // 401/403等认证错误才设为未认证
                        isAuthenticationError(exception) -> {
                            _authState.value = AuthState.NotAuthenticated
                            _user.value = null
                        }
                        // 其他错误
                        else -> {
                            _authState.value = AuthState.Error(exception.message ?: "检查认证状态失败")
                        }
                    }
                }
        }
    }

    private fun isNetworkError(exception: Throwable): Boolean {
        return exception is java.net.SocketTimeoutException ||
                exception is java.net.UnknownHostException ||
                exception is java.net.ConnectException ||
                exception is java.io.IOException ||
                exception.message?.contains("timeout", ignoreCase = true) == true ||
                exception.message?.contains("network", ignoreCase = true) == true
    }

    private fun isAuthenticationError(exception: Throwable): Boolean {
        return exception.message?.contains("401", ignoreCase = true) == true ||
                exception.message?.contains("403", ignoreCase = true) == true ||
                exception.message?.contains("unauthorized", ignoreCase = true) == true ||
                exception.message?.contains("forbidden", ignoreCase = true) == true
    }

    /**
     * 清除用户资料相关错误
     */
    fun clearProfileError() {
        _profileError.value = null
    }

    /**
     * 清除用户资料数据
     */
    fun clearProfileUser() {
        _profileUser.value = null
        _profileError.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * 清除认证错误状态
     */
    fun clearAuthError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.NotAuthenticated
        }
    }

}