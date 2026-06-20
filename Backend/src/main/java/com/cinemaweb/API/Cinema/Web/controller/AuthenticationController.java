package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.*;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.AuthenticationResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.IntrospectResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.PasswordResetResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.TokenResult;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.refresh-cookie.name}")
    @NonFinal
    String refreshCookieName;

    @Value("${jwt.refresh-cookie.path}")
    @NonFinal
    String refreshCookiePath;

    @Value("${jwt.refresh-cookie.secure}")
    @NonFinal
    boolean refreshCookieSecure;

    @Value("${jwt.refresh-cookie.same-site}")
    @NonFinal
    String refreshCookieSameSite;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request,
                                                            HttpServletResponse response) {
        TokenResult result = authenticationService.authenticate(request);
        setRefreshCookie(response, result.refreshToken(), result.refreshTtlSeconds());

        return ApiResponse.<AuthenticationResponse>builder()
                .body(toAuthResponse(result))
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return ApiResponse.<IntrospectResponse>builder()
                .body(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                    HttpServletRequest httpRequest,
                                    HttpServletResponse response) {
        String accessToken = extractBearer(authHeader);
        String refreshToken = readRefreshCookie(httpRequest);

        authenticationService.logout(accessToken, refreshToken);
        clearRefreshCookie(response);

        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh-Token")
    public ApiResponse<AuthenticationResponse> refreshToken(HttpServletRequest httpRequest,
                                                            HttpServletResponse response) {
        String refreshToken = readRefreshCookie(httpRequest);
        if (refreshToken == null)
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);

        TokenResult result = authenticationService.refreshToken(refreshToken);
        setRefreshCookie(response, result.refreshToken(), result.refreshTtlSeconds());

        return ApiResponse.<AuthenticationResponse>builder()
                .body(toAuthResponse(result))
                .build();
    }

    @PostMapping("/forget-password")
    public ApiResponse<String> getPasswordOTP(@RequestBody PasswordOtpRequest request) {
        return ApiResponse.<String>builder()
                .message(authenticationService.getPasswordToken(request.getEmail()))
                .build();
    }

    @PostMapping("/reset-password/{otp}")
    public ApiResponse<PasswordResetResponse> resetPassword(@RequestBody @Valid PasswordResetRequest request, @PathVariable("otp") String OTP) {
        return ApiResponse.<PasswordResetResponse>builder()
                .body(authenticationService.resetPassword(request, OTP))
                .build();
    }


    // ───────────────────────── Helpers ─────────────────────────

    private AuthenticationResponse toAuthResponse(TokenResult result) {
        return AuthenticationResponse.builder()
                .token(result.accessToken())
                .authenticated(true)
                .expiresIn(result.accessTtlSeconds())
                .build();
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(refreshCookiePath)
                .maxAge(maxAgeSeconds)
                .sameSite(refreshCookieSameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(refreshCookiePath)
                .maxAge(0)
                .sameSite(refreshCookieSameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (refreshCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String extractBearer(String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
