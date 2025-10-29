package net.hearnsoft.tcm.compose.data.repository

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.google.gson.Gson
import net.hearnsoft.tcm.compose.data.mapper.UserMapper.toAuthCredential
import net.hearnsoft.tcm.compose.data.mapper.UserMapper.toUser
import net.hearnsoft.tcm.compose.data.remote.ApiService
import net.hearnsoft.tcm.compose.domain.model.auth.LoginCredential
import net.hearnsoft.tcm.compose.domain.model.user.User
import net.hearnsoft.tcm.compose.domain.repository.UserRepository
import net.hearnsoft.thcdb_api.model.BaseResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val contentResolver: ContentResolver
) : UserRepository {

    private val gson = Gson()

    override suspend fun getMyProfile(): Result<User> {
        return try {
            val response = apiService.userApi.getMyProfile()
            if (response.isSuccess() && response.data != null) {
                Result.success(response.data!!.toUser())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch user profile"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(name: String): Result<User> {
        return try {
            val response = apiService.userApi.getUserProfile(name)
            if (response.isSuccess() && response.data != null) {
                Result.success(response.data!!.toUser())
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch user profile"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(credential: LoginCredential): Result<User> {
        return try {
            val response = apiService.userApi.signIn(credential.toAuthCredential())
            if (response.isSuccess() && response.data != null) {
                Result.success(response.data!!.toUser())
            } else {
                Result.failure(Exception(response.message ?: "Sign in failed"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            val response = apiService.userApi.signOut()
            if (response.isSuccess()) {
                apiService.clearCookies()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Sign out failed"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(credential: LoginCredential): Result<User> {
        return try {
            val response = apiService.userApi.signUp(credential.toAuthCredential())
            if (response.isSuccess() && response.data != null) {
                Result.success(response.data!!.toUser())
            } else {
                Result.failure(Exception(response.message ?: "Sign up failed"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(avatarUri: Uri): Result<Unit> {
        return try {
            val fileName = getFileNameFromUri(avatarUri)
            val requestBody = createRequestBodyFromUri(avatarUri)
            val body = MultipartBody.Part.createFormData("data", fileName, requestBody)

            val response = apiService.userApi.uploadAvatar(body)
            if (response.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to upload avatar"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfileBanner(bannerUri: Uri): Result<Unit> {
        return try {
            val fileName = getFileNameFromUri(bannerUri)
            val requestBody = createRequestBodyFromUri(bannerUri)
            val body = MultipartBody.Part.createFormData("data", fileName, requestBody)

            val response = apiService.userApi.uploadProfileBanner(body)
            if (response.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to upload banner"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBio(bio: String): Result<Unit> {
        return try {
            val requestBody = bio.toRequestBody("text/plain".toMediaType())
            val response = apiService.userApi.updateBio(requestBody)
            if (response.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update bio"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception(parseErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parse error message from HTTP exception.
     * Priority: Server message > HTTP status message > Generic fallback
     */
    private fun parseErrorMessage(httpException: HttpException): String {
        return try {
            // Try to parse BaseResponse from error body (server's original message)
            val errorBody = httpException.response()?.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val errorResponse = gson.fromJson(errorBody, BaseResponse::class.java)
                // Use server's original message if available
                errorResponse.message?.takeIf { it.isNotBlank() }?.let { return it }
            }
            
            // Fallback to HTTP status message
            httpException.message()?.takeIf { it.isNotBlank() }?.let { return it }
            
            // Generic fallback based on HTTP code
            "HTTP ${httpException.code()}: ${httpException.message() ?: "Request failed"}"
        } catch (e: Exception) {
            // If parsing fails, use the original exception message
            httpException.message() ?: "Network request failed"
        }
    }

    /**
     * Create RequestBody from URI for file upload
     */
    private fun createRequestBodyFromUri(uri: Uri): RequestBody {
        return object : RequestBody() {
            override fun contentType() =
                contentResolver.getType(uri)?.toMediaTypeOrNull() ?: "image/*".toMediaTypeOrNull()

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val buffer = ByteArray(4096)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        sink.write(buffer, 0, read)
                    }
                } ?: throw IOException("Cannot open file stream")
            }
        }
    }

    /**
     * Get file name from URI
     */
    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null

        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }

        return result ?: "unknown_file"
    }
}