package net.hearnsoft.tcm.domain.repository

import org.openapitools.client.models.UserProfile

interface UserRepository {
    suspend fun getProfile(username: String): UserProfile;
}