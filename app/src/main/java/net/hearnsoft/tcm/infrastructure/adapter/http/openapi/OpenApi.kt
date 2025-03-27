package net.hearnsoft.tcm.infrastructure.adapter.http.openapi


import net.hearnsoft.tcm.infrastructure.adapter.http.ApiEndpoints
import org.openapitools.client.apis.ArtistApi
import org.openapitools.client.models.Artist
import org.openapitools.client.models.Message
import org.openapitools.client.models.NewArtist

object ArtistApi {
    private val api = ArtistApi(basePath = ApiEndpoints.BASE_URL)

    suspend fun findById(id: Int): Artist {
        return api.findArtistById(id).data
    }

    suspend fun findByKeyword(keyword: String): List<Artist> {
        return api.findArtistByKeyword(keyword).data
    }

    suspend fun create(data: NewArtist): Message {
        return api.createArtist(data)
    }

    suspend fun upsertCorrection(id: Int, data: NewArtist): Message {
        return api.upsertArtistCorrection(id, data)
    }
}

