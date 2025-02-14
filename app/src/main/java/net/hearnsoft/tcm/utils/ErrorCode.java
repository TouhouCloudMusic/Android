package net.hearnsoft.tcm.utils;

public enum ErrorCode {
    // Local
    NonJSONResponse(10000),
    JSONParseError(10001),
    UnexpectedError(10002),
    NetworkError(10003),
    InvalidToken(10004),
    FileURINull(10005),
    FileReadError(10006),
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

    private int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
