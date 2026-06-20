package com.cinemaweb.API.Cinema.Web.dto.response;

/**
 * Kết quả cấp token cho tầng controller: access token trả trong body,
 * refresh token được controller đặt vào HttpOnly cookie (không nằm trong body).
 */
public record TokenResult(
        String accessToken,
        String refreshToken,
        long accessTtlSeconds,
        long refreshTtlSeconds
) {
}
