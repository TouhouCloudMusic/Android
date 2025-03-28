package net.hearnsoft.tcm.application.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import net.hearnsoft.tcm.application.dto.AuthCreds
import org.openapitools.client.models.UserProfile
import java.util.concurrent.CompletableFuture


interface ILoginUseCase {
    suspend fun exec(creds: AuthCreds): Result<UserProfile>;

    fun execSync(creds: AuthCreds): CompletableFuture<Result<UserProfile>> {
        val future = CoroutineScope(Dispatchers.IO).future {
            exec(creds)
        };

        return future
    }
}