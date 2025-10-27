package net.hearnsoft.tcm.compose.domain.model.user

sealed class UserOperationState {
    data object Idle : UserOperationState()
    data object Loading : UserOperationState()
    
    sealed class Success : UserOperationState() {
        data object AvatarUploaded : Success()
        data object BannerUploaded : Success()
        data object BioUpdated : Success()
        data object ProfileFetched : Success()
        data object ProfileUpdated : Success()
        data object LoggedOut : Success()
    }
    
    data class Error(val throwable: Throwable) : UserOperationState()
}
