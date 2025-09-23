package net.hearnsoft.tcm.compose.domain.model.auth

import net.hearnsoft.tcm.compose.domain.model.user.User

sealed class AuthState {
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    data class NetworkError(val message: String) : AuthState()
}