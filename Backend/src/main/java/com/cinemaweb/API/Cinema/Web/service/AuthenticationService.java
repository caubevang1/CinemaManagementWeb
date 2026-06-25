package com.cinemaweb.API.Cinema.Web.service;


import com.cinemaweb.API.Cinema.Web.dto.request.*;
import com.cinemaweb.API.Cinema.Web.dto.response.IntrospectResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.PasswordResetResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.TokenResult;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {

    UserRepository userRepository;
    EmailService emailService;
    UserService userService;
    StringRedisTemplate redisTemplate;

    // Redis key prefixes
    static final String REFRESH_WHITELIST_PREFIX = "refresh_token:"; // refresh_token:{userId}:{jti}
    static final String ACCESS_BLACKLIST_PREFIX = "blacklist_token:"; // blacklist_token:{jti}
    static final String REFRESH_ROTATED_PREFIX = "refresh_rotated:"; // refresh_rotated:{userId}:{oldJti} -> refresh token mới
    static final String ROTATION_PENDING = "PENDING"; // marker khi winner đang rotate

    // OTP quên mật khẩu lưu trong Redis (tự hết hạn theo TTL, dùng một lần bằng DEL).
    static final String PWD_OTP_PREFIX = "pwd-otp:";           // pwd-otp:{token} -> userId
    static final String PWD_OTP_USER_PREFIX = "pwd-otp-user:"; // pwd-otp-user:{userId} -> token hiện hành (để vô hiệu cái cũ)
    static final String PWD_OTP_CD_PREFIX = "pwd-otp-cd:";     // pwd-otp-cd:{userId} -> cooldown chống spam gửi lại
    static final long PWD_OTP_TTL = 300;    // 5 phút hiệu lực OTP
    static final long PWD_OTP_CD_TTL = 90;  // 90s chống spam gửi lại (khớp WAIT_OTP cũ)

    // Chống false-positive khi nhiều request refresh cùng token đến đồng thời
    static final long ROTATION_POLL_MAX_MS = 500;
    static final long ROTATION_POLL_INTERVAL_MS = 50;

    public static final String TOKEN_TYPE_ACCESS = "access";
    static final String TOKEN_TYPE_REFRESH = "refresh";

    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.access-token-ttl}")
    private long accessTokenTtl;

    @NonFinal
    @Value("${jwt.refresh-token-ttl}")
    private long refreshTokenTtl;

    @NonFinal
    @Value("${jwt.refresh-rotation-grace:60}")
    private long refreshRotationGrace;

    public TokenResult authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.LOGIN_FAILED);

        return generateTokenPair(user);
    }

    /**
     * Logout: blacklist access token trong khoảng thời gian còn lại của nó,
     * và xóa refresh token khỏi whitelist (thu hồi phiên).
     */
    public void logout(String accessToken, String refreshToken) {
        // Blacklist access token (TTL = thời gian còn lại của access token)
        if (accessToken != null) {
            try {
                SignedJWT signedJWT = verifyToken(accessToken, false);
                String jti = signedJWT.getJWTClaimsSet().getJWTID();
                long remaining = remainingSeconds(signedJWT.getJWTClaimsSet().getExpirationTime());
                if (remaining > 0) {
                    redisTemplate.opsForValue()
                            .set(ACCESS_BLACKLIST_PREFIX + jti, "1", remaining, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                // Access token có thể đã hết hạn/không hợp lệ -> không cần blacklist
                log.debug("Logout: skip blacklisting access token: {}", e.getMessage());
            }
        }

        // Xóa refresh token khỏi whitelist
        if (refreshToken != null) {
            try {
                SignedJWT signedJWT = verifyToken(refreshToken, true);
                String userId = signedJWT.getJWTClaimsSet().getSubject();
                String jti = signedJWT.getJWTClaimsSet().getJWTID();
                redisTemplate.delete(refreshKey(userId, jti));
            } catch (Exception e) {
                log.debug("Logout: skip removing refresh token: {}", e.getMessage());
            }
        }
    }


    public IntrospectResponse introspect(IntrospectRequest request) {
        boolean isValid = true;

        try {
            SignedJWT signedJWT = verifyToken(request.getToken(), false);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_BLACKLIST_PREFIX + jti))) {
                isValid = false;
            }
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public int getPasswordToken(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        String userId = user.getID();

        // Chống spam gửi lại: còn cooldown thì từ chối (khớp WAIT_OTP cũ, INTERVAL 1.5 phút).
        if (Boolean.TRUE.equals(redisTemplate.hasKey(PWD_OTP_CD_PREFIX + userId))) {
            throw new AppException(ErrorCode.WAIT_OTP);
        }

        String token = UUID.randomUUID().toString();

        // Gửi mail TRƯỚC khi ghi Redis: nếu MailException ném ra (xử lý ở GlobalExceptionHandler)
        // thì chưa có key nào được ghi -> tương đương rollback của bản @Transactional cũ.
        emailService.sendResetPasswordOtp(user, token);

        // Vô hiệu OTP cũ của user (nếu còn) trước khi cấp cái mới.
        String oldToken = redisTemplate.opsForValue().get(PWD_OTP_USER_PREFIX + userId);
        if (oldToken != null) {
            redisTemplate.delete(PWD_OTP_PREFIX + oldToken);
        }

        // OTP tự hết hạn theo TTL (5 phút), không cần scheduler dọn nữa.
        redisTemplate.opsForValue().set(PWD_OTP_PREFIX + token, userId, PWD_OTP_TTL, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(PWD_OTP_USER_PREFIX + userId, token, PWD_OTP_TTL, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(PWD_OTP_CD_PREFIX + userId, "1", PWD_OTP_CD_TTL, TimeUnit.SECONDS);

        return (int) PWD_OTP_CD_TTL;   // số giây cooldown để FE đếm ngược nút gửi lại
    }


    @Transactional(rollbackFor = Exception.class)
    public PasswordResetResponse resetPassword(PasswordResetRequest request, String OTP) {
        String userId = redisTemplate.opsForValue().get(PWD_OTP_PREFIX + OTP);
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.CONFIRM_PASSWORD_FAIL);
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        User user = userService.resetPassword(target, request.getNewPassword());

        // Dùng một lần: xóa OTP + index + cooldown sau khi đổi mật khẩu thành công.
        redisTemplate.delete(PWD_OTP_PREFIX + OTP);
        redisTemplate.delete(PWD_OTP_USER_PREFIX + userId);
        redisTemplate.delete(PWD_OTP_CD_PREFIX + userId);

        return PasswordResetResponse.builder()
                .token(generateAccessToken(user))
                .build();
    }


    /**
     * Cấp lại token theo cơ chế rotation. Refresh token cũ phải còn trong whitelist;
     * nếu không (đã bị rotation/thu hồi) thì coi là token bị tái sử dụng -> thu hồi cả phiên.
     */
    public TokenResult refreshToken(String refreshToken) {
        SignedJWT signedJWT;
        String userId;
        String oldJti;
        try {
            signedJWT = verifyToken(refreshToken, true);
            userId = signedJWT.getJWTClaimsSet().getSubject();
            oldJti = signedJWT.getJWTClaimsSet().getJWTID();
        } catch (AppException | JOSEException | ParseException e) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String oldKey = refreshKey(userId, oldJti);
        String graceKey = REFRESH_ROTATED_PREFIX + userId + ":" + oldJti;

        // Refresh token cũ không còn trong whitelist: hoặc đã rotate (refresh đồng thời lành tính),
        // hoặc bị tái sử dụng thật sự.
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(oldKey))) {
            String rotated = redisTemplate.opsForValue().get(graceKey);
            if (rotated != null && !ROTATION_PENDING.equals(rotated)) {
                // Trong grace window: trả lại đúng refresh token mới đã cấp cho request "thắng".
                return tokenResultFromRotated(userId, rotated);
            }
            // Reuse detection: chữ ký hợp lệ nhưng không còn whitelist và đã hết grace
            // -> nghi ngờ bị đánh cắp, thu hồi toàn bộ refresh token của user.
            Set<String> keys = redisTemplate.keys(REFRESH_WHITELIST_PREFIX + userId + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            throw new AppException(ErrorCode.REFRESH_TOKEN_REUSED);
        }

        // Tranh quyền rotate: chỉ một request "thắng" được rotate, các request còn lại nhận lại
        // cùng token mới thay vì bị coi là reuse.
        Boolean won = redisTemplate.opsForValue()
                .setIfAbsent(graceKey, ROTATION_PENDING, refreshRotationGrace, TimeUnit.SECONDS);

        if (!Boolean.TRUE.equals(won)) {
            // Loser: chờ winner ghi refresh token mới vào graceKey rồi trả về cùng token đó.
            String rotated = awaitRotatedToken(graceKey);
            if (rotated != null) {
                return tokenResultFromRotated(userId, rotated);
            }
            // Winner chưa kịp hoàn tất trong thời gian chờ (hiếm) -> yêu cầu refresh lại.
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Winner: cấp cặp token mới.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        TokenResult result = generateTokenPair(user);

        // Ghi token mới vào graceKey TRƯỚC khi xoá oldKey: đảm bảo mọi loser thấy oldKey biến mất
        // thì luôn đọc được token thật ở graceKey -> không bao giờ false-positive reuse.
        redisTemplate.opsForValue()
                .set(graceKey, result.refreshToken(), refreshRotationGrace, TimeUnit.SECONDS);
        redisTemplate.delete(oldKey);

        return result;
    }

    /**
     * Loser chờ winner ghi xong refresh token mới vào graceKey (giá trị khác PENDING).
     * Trả null nếu hết thời gian chờ mà winner vẫn chưa hoàn tất.
     */
    private String awaitRotatedToken(String graceKey) {
        long deadline = System.currentTimeMillis() + ROTATION_POLL_MAX_MS;
        while (System.currentTimeMillis() < deadline) {
            String rotated = redisTemplate.opsForValue().get(graceKey);
            if (rotated != null && !ROTATION_PENDING.equals(rotated)) {
                return rotated;
            }
            try {
                Thread.sleep(ROTATION_POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    /**
     * Cấp access token mới nhưng tái dùng refresh token đã được rotate cho các request refresh
     * đồng thời, để tất cả hội tụ về cùng một refresh token.
     */
    private TokenResult tokenResultFromRotated(String userId, String rotatedRefreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        String accessToken = generateAccessToken(user);
        return new TokenResult(accessToken, rotatedRefreshToken, accessTokenTtl, refreshTokenTtl);
    }


    /**
     * Verify chữ ký + hạn + đúng loại token. Không kiểm tra Redis ở đây
     * (whitelist/blacklist được kiểm tra ở từng luồng cụ thể).
     */
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(jwsVerifier);

        if (!verified || expiryTime == null || !expiryTime.after(new Date()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        String expectedType = isRefresh ? TOKEN_TYPE_REFRESH : TOKEN_TYPE_ACCESS;
        Object type = signedJWT.getJWTClaimsSet().getClaim("type");
        if (!expectedType.equals(type))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    /**
     * Dùng cho CustomJwtDecoder: kiểm tra access token (đã được decoder verify chữ ký + hạn)
     * có nằm trong blacklist (đã logout) hay không.
     */
    public boolean isAccessTokenBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_BLACKLIST_PREFIX + jti));
    }


    private TokenResult generateTokenPair(User user) {
        String accessToken = generateAccessToken(user);

        String refreshJti = UUID.randomUUID().toString();
        String refreshToken = generateRefreshToken(user, refreshJti);
        redisTemplate.opsForValue()
                .set(refreshKey(user.getID(), refreshJti), "1", refreshTokenTtl, TimeUnit.SECONDS);

        return new TokenResult(accessToken, refreshToken, accessTokenTtl, refreshTokenTtl);
    }


    public String generateAccessToken(User user) {
        return buildToken(user.getID(), buildScope(user), TOKEN_TYPE_ACCESS,
                accessTokenTtl, UUID.randomUUID().toString());
    }

    private String generateRefreshToken(User user, String jti) {
        return buildToken(user.getID(), null, TOKEN_TYPE_REFRESH, refreshTokenTtl, jti);
    }


    private String buildToken(String subject, String scope, String type, long ttlSeconds, String jti) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("API-Cinema-Web")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plusSeconds(ttlSeconds).toEpochMilli()))
                .jwtID(jti)
                .claim("type", type);

        if (scope != null) {
            builder.claim("scope", scope);
        }

        Payload payload = new Payload(builder.build().toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create Token!!", e);
            throw new RuntimeException(e);
        }
    }


    private String refreshKey(String userId, String jti) {
        return REFRESH_WHITELIST_PREFIX + userId + ":" + jti;
    }

    private long remainingSeconds(Date expiryTime) {
        if (expiryTime == null) return 0;
        long remainingMs = expiryTime.getTime() - System.currentTimeMillis();
        return remainingMs > 0 ? remainingMs / 1000 : 0;
    }


    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!role.getPermissions().isEmpty())
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));

            });
        }
        return stringJoiner.toString();
    }


}
