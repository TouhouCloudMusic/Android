package net.hearnsoft.tcm.infrastructure.adapter.http


import net.hearnsoft.tcm.application.service.SyncUserService
import net.hearnsoft.tcm.application.service.UserService
import net.hearnsoft.tcm.domain.repository.ArtistRepository
import net.hearnsoft.tcm.domain.repository.ReleaseRepository
import net.hearnsoft.tcm.domain.repository.UserRepository
import org.openapitools.client.models.ArtistCorrection
import org.openapitools.client.models.AuthCredential
import org.openapitools.client.models.Message
import org.openapitools.client.models.ReleaseCorrection
import org.openapitools.client.models.UserProfile
import java.io.File
import org.openapitools.client.apis.ArtistApi as ArtistOpenApi
import org.openapitools.client.apis.ReleaseApi as ReleaseOpenApi


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

class UserApi(basePath: String) : UserRepository, UserService, SyncUserService {
    private val api = org.openapitools.client.apis.UserApi(basePath)

    override suspend fun getProfile(username: String): UserProfile {
        val res = api.profileWithName(username)

        return res.data
    }

    override suspend fun signIn(authCreds: AuthCredential): UserProfile {
        return api.signIn(authCreds).data
    }

    override suspend fun signUp(authCreds: AuthCredential): UserProfile {
        return api.signUp(authCreds).data
    }

    override suspend fun signOut(): Message {
        return api.signOut()
    }

    override suspend fun uploadAvatar(data: File): Message {
        return api.uploadAvatar(data)
    }
}



