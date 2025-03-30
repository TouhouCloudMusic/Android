package net.hearnsoft.tcm.infrastructure.adapter.http


import net.hearnsoft.tcm.application.dto.AuthCreds
import net.hearnsoft.tcm.application.usecase.ILoginUseCase
import net.hearnsoft.tcm.domain.repository.ArtistRepository
import net.hearnsoft.tcm.domain.repository.ReleaseRepository
import org.openapitools.client.models.ArtistCorrection
import org.openapitools.client.models.AuthCredential
import org.openapitools.client.models.ReleaseCorrection
import org.openapitools.client.models.UserProfile
import org.openapitools.client.apis.ArtistApi as ArtistOpenApi
import org.openapitools.client.apis.ReleaseApi as ReleaseOpenApi
import org.openapitools.client.apis.UserApi as UserOpenApi


class OpenApiAdapter(basePath: String) {
    val artist by lazy { ArtistApi(basePath) }
    val release by lazy { ReleaseApi(basePath) }
    val user by lazy { UserApi(basePath) }
}


class ArtistApi(basePath: String) : ArtistRepository {
    private val api = ArtistOpenApi(basePath)

    override suspend fun findById(id: Int) = api.findArtistById(id).data

    override suspend fun findByKeyword(keyword: String) = api.findArtistByKeyword(keyword).data

    suspend fun create(data: ArtistCorrection) = api.createArtist(data)

    suspend fun upsertCorrection(id: Int, data: ArtistCorrection) =
        api.upsertArtistCorrection(id, data)
}

class ReleaseApi(basePath: String) : ReleaseRepository {
    private val api = ReleaseOpenApi(basePath)

    override suspend fun findById(id: Int) = api.findReleaseById(id).data

    override suspend fun findByKeyword(keyword: String) = api.findReleaseByKeyword(keyword).data

    suspend fun create(data: ReleaseCorrection) = api.createRelease(data)

    suspend fun upsertCorrection(id: Int, data: ReleaseCorrection) = api.updateRelease(id, data)
}

class SongApi {

}

class TagApi {

}

class UserApi(basePath: String) {
    private val api = UserOpenApi(basePath)

    val login: ILoginUseCase = LoginUseCase(api)
}

private class LoginUseCase(private val api: UserOpenApi) : ILoginUseCase {
    override suspend fun exec(creds: AuthCreds): UserProfile {
        val ret = api.signIn(AuthCredential(creds.username, creds.password)).data

        return ret
    }
}




