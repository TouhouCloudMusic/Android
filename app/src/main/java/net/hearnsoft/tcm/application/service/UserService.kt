package net.hearnsoft.tcm.application.service


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import org.openapitools.client.models.AuthCredential
import org.openapitools.client.models.Message
import org.openapitools.client.models.UserProfile
import java.io.File
import java.util.concurrent.CompletableFuture

interface UserService {
    suspend fun signIn(authCreds: AuthCredential): UserProfile;
    suspend fun signUp(authCreds: AuthCredential): UserProfile;
    suspend fun signOut(): Message;
    suspend fun uploadAvatar(data: File): Message;
}

interface SyncUserService : UserService {
    private val scope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    fun signInSync(authCreds: AuthCredential): CompletableFuture<UserProfile> {
        return scope.future {
            signIn(authCreds)
        }
    }

    fun signUpSync(authCreds: AuthCredential): CompletableFuture<UserProfile> {
        return scope.future {
            signUp(authCreds)
        }
    }

    fun signOutSync(): CompletableFuture<Message> {
        return scope.future {
            signOut()
        }
    }

    fun uploadAvatarSync(data: File): CompletableFuture<Message> {
        return scope.future {
            uploadAvatar(data)
        }
    }
}