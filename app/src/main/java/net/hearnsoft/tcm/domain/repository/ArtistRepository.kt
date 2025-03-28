package net.hearnsoft.tcm.domain.repository

import org.openapitools.client.models.Artist

interface ArtistRepository {
    suspend fun findById(id: Int): Artist
    suspend fun findByKeyword(keyword: String): List<Artist>
}