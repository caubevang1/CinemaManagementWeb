package com.cinemaweb.API.Cinema.Web.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {

    // ─────────────── 9xxx: System/Internal Errors ───────────────
    UNKNOWN_EXCEPTION(9000, "Unknown error!", HttpStatus.INTERNAL_SERVER_ERROR),
    RUNTIME_EXCEPTION(9001, "Runtime error!", HttpStatus.INTERNAL_SERVER_ERROR),

    // ─────────────── 1xxx: User-related Errors ───────────────
    USER_EXISTED(1001, "User already exists!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTS(1002, "User does not exist!", HttpStatus.BAD_REQUEST),
    LASTNAME_NULL(1003, "Last name must not be null!", HttpStatus.BAD_REQUEST),
    GENDER_NULL(1004, "Gender must not be null!", HttpStatus.BAD_REQUEST),
    PHONE_NUMBER_NULL(1005, "Phone number must not be null!", HttpStatus.BAD_REQUEST),
    POINT_IS_NULL(1006, "User's point must not be null!", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1007, "Email already exists!", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1008, "Invalid email format!", HttpStatus.BAD_REQUEST),

    // ─────────────── 2xxx: Authentication & Authorization ───────────────
    UNAUTHENTICATED(2001, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2002, "You do not have permission!", HttpStatus.FORBIDDEN),
    WAIT_OTP(2003, "Please wait 90s to resend OTP!", HttpStatus.BAD_REQUEST),
    TOKEN_IS_NULL(2004, "Token must not be null!", HttpStatus.BAD_REQUEST),
    INVALID_OTP(2005, "OTP does not exist or expired!", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(2006,"JWT Token expired or disabled" , HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN(2007, "Refresh token is invalid or expired!", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REUSED(2008, "Refresh token reuse detected, session revoked!", HttpStatus.UNAUTHORIZED),

    // ─────────────── 3xxx: Role & Permission Errors ───────────────
    ROLE_EXISTED(3001, "Role already exists!", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTS(3002, "Role does not exist!", HttpStatus.BAD_REQUEST),
    INVALID_ROLE(3003, "Invalid role!", HttpStatus.BAD_REQUEST),
    INVALID_PERMISSION(3004, "Invalid permission!", HttpStatus.BAD_REQUEST),
    PERMISSION_EXISTED(3005, "Permission already exists!", HttpStatus.BAD_REQUEST),
    PERMISSION_NAME_NULL(3006, "Permission name must not be null!", HttpStatus.BAD_REQUEST),
    ROLE_NAME_NULL(3007, "Role name must not be null!", HttpStatus.BAD_REQUEST),

    // ─────────────── 4xxx: Input Field Validation Errors ───────────────
    USERNAME_IS_NULL(4001, "Username must not be null!", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(4002, "Username must be at least 6 characters!", HttpStatus.BAD_REQUEST),
    PASSWORD_IS_NULL(4003, "Password must not be null!", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(4004, "Password must be at least 8 characters!", HttpStatus.BAD_REQUEST),
    EMAIL_IS_NULL(4005, "Email must not be null!", HttpStatus.BAD_REQUEST),
    DOB_IS_NULL(4006, "Date of birth must not be null!", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_FAIL(4007, "New password and confirm password do not match!", HttpStatus.BAD_REQUEST),

    // ─────────────── 5xxx: Movie-related Errors ───────────────
    MOVIE_EXISTED(5001, "Phim đã tồn tại (trùng tên và ngày phát hành)!", HttpStatus.BAD_REQUEST),
    TMDB_MOVIE_EXISTED(5002, "Phim này đã được import từ TMDB!", HttpStatus.BAD_REQUEST),
    RELEASE_DATE_NULL(5003, "Ngày phát hành không được để trống!", HttpStatus.BAD_REQUEST);

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
