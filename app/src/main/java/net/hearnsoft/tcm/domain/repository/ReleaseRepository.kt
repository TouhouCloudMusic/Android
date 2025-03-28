package net.hearnsoft.tcm.domain.repository


import org.openapitools.client.models.Release

interface ReleaseRepository {
    suspend fun findById(id: Int): Release
    suspend fun findByKeyword(keyword: String): List<Release>
}