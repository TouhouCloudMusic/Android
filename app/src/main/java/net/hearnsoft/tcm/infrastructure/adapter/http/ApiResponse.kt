package net.hearnsoft.tcm.infrastructure.adapter.http

import android.annotation.SuppressLint
import io.vavr.control.Either
import lombok.Getter

sealed interface ApiResponse<out T>;
sealed interface ApiMessage;

data class Data<out T>(
    val status: String,
    val data: T,
) : ApiResponse<T>;

data class Message(
    val status: String,
    val message: String,
) : ApiMessage

data class Error(
    val status: String, val message: String, val errorCode: ErrorCode
) : ApiResponse<Nothing>, ApiMessage

@Getter
enum class ErrorCode(private val code: Int) {
    // Local
    NonJSONResponse(10000),
    JSONParseError(10001),
    UnexpectedError(10002),
    NetworkError(10003),
    InvalidToken(10004),
    FileURINull(10005),
    FileReadError(10006),
    RiskControlError(10007),

    // Common
    InvalidField(40000),
    IncorrectCorrectionType(40001),
    Unauthorized(40100),
    AuthenticationFailed(40101),
    EntityNotFound(40400),
    UnknownError(50000),
    TokioError(50001),
    DatabaseError(50002),
    RedisError(50003),
    UnExpRelatedEntityNotFound(50004),

    // User
    InvalidUserName(140000),
    InvalidPassword(140001),
    PasswordTooWeak(140002),
    InvalidImageType(140003),
    AlreadySignedIn(140900),
    UsernameAlreadyInUse(140901),
    SessionMiddlewareError(150000),
    ParsePasswordFailed(150001),
    HashPasswordFailed(150002),

    // Artist
    UnknownTypeArtistOwnedMember(240000);

    companion object {
        @Deprecated("")
        fun fromCode(code: Int): ErrorCode {
            for (e in entries) {
                if (e.code == code) {
                    return e
                }
            }
            return UnknownError // 处理未匹配情况
        }

        @JvmStatic
        fun tryFromInt(num: Int): Either<ParseErrorCodeException, ErrorCode> {
            for (e in entries) {
                if (e.code == num) {
                    return Either.right(e)
                }
            }
            return Either.left(ParseErrorCodeException(num))
        }
    }
}

class ParseErrorCodeException @SuppressLint("DefaultLocale") internal constructor(accepted: Int) :
    RuntimeException(String.format("Invalid code: %d", accepted))