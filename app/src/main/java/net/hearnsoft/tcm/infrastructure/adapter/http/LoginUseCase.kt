package net.hearnsoft.tcm.infrastructure.adapter.http

import io.vavr.control.Either
import net.hearnsoft.tcm.application.dto.AuthCreds
import net.hearnsoft.tcm.application.usecase.ILoginUseCase
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class LoginUseCase(
    private val authService: AuthService,
) : ILoginUseCase {
    override suspend fun exec(creds: AuthCreds): Either<String, net.hearnsoft.tcm.domain.model.user.UserProfile> {
        val res = this.authService.login(creds);

        return when (val body = res.body()) {
            is Data<net.hearnsoft.tcm.domain.model.user.UserProfile> -> Either.right(body.data)
            is Error -> Either.left(body.message)
            null -> Either.left("No body returned")
        }
    }
}

interface AuthService {
    @POST(ApiEndpoints.LOGIN)
    suspend fun login(@Body creds: AuthCreds): Response<ApiResponse<net.hearnsoft.tcm.domain.model.user.UserProfile>>;
}
