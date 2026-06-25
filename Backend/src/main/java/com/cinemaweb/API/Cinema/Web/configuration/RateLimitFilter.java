package com.cinemaweb.API.Cinema.Web.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Atomic fixed-window counter via Lua: increment key, set TTL on first hit, return 0 if over limit
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>(
            "local c = redis.call('INCR', KEYS[1]) " +
            "if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[2]) end " +
            "if c > tonumber(ARGV[1]) then return 0 end " +
            "return 1",
            Long.class);

    // endpoint tag → {limit, windowSeconds}
    private static final Map<String, int[]> RULES = Map.of(
            "auth:login",           new int[]{10, 60},
            "auth:forget-password", new int[]{10, 60},
            "auth:reset-password",  new int[]{5,  300},
            "users:sign-up",        new int[]{3,  900},
            "booking",              new int[]{5,  60},
            "bookingSeat",          new int[]{10, 60}
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String tag = resolveTag(req);
        if (tag == null) {
            chain.doFilter(req, res);
            return;
        }

        int[] rule = RULES.get(tag);
        String id = tag.startsWith("booking") ? resolveUserId(req) : getClientIp(req);
        String key = "rl:" + tag + ":" + id;

        Long allowed = redisTemplate.execute(SCRIPT, List.of(key),
                String.valueOf(rule[0]), String.valueOf(rule[1]));

        if (allowed == null || allowed == 0L) {
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setHeader("Retry-After", String.valueOf(rule[1]));
            res.getWriter().write(objectMapper.writeValueAsString(
                    Map.of("code", 9002, "message", "Bạn thao tác quá nhanh, vui lòng thử lại sau giây lát!")));
            return;
        }

        chain.doFilter(req, res);
    }

    private String resolveTag(HttpServletRequest req) {
        if (!"POST".equalsIgnoreCase(req.getMethod())) return null;
        String p = req.getRequestURI();
        if ("/auth/login".equals(p))             return "auth:login";
        if ("/auth/forget-password".equals(p))   return "auth:forget-password";
        if (p.startsWith("/auth/reset-password")) return "auth:reset-password";
        if ("/users/sign-up".equals(p) || "/users".equals(p)) return "users:sign-up";
        if ("/booking".equals(p))                return "booking";
        if ("/bookingSeat".equals(p))            return "bookingSeat";
        return null;
    }

    // Extract userId from JWT payload (without full validation — only for bucketing)
    private String resolveUserId(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                String payload = auth.split("\\.")[1];
                String json = new String(Base64.getUrlDecoder().decode(payload));
                var node = objectMapper.readTree(json);
                if (node.has("sub")) return "user:" + node.get("sub").asText();
            } catch (Exception ignored) {}
        }
        return getClientIp(req);
    }

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}
