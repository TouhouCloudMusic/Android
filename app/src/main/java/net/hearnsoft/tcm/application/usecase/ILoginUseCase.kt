package net.hearnsoft.tcm.application.usecase

import io.vavr.concurrent.Future
import io.vavr.control.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import net.hearnsoft.tcm.application.dto.AuthCreds
import net.hearnsoft.tcm.domain.model.user.UserProfile

interface ILoginUseCase {
    suspend fun exec(creds: AuthCreds): Either<String, UserProfile>;

    fun execSync(creds: AuthCreds): Future<Either<String, UserProfile>> {
        val future = CoroutineScope(Dispatchers.IO).future {
            exec(creds)
        };

        return Future.fromJavaFuture(future)
    }
}