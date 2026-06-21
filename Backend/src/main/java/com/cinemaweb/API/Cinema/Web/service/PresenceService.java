package com.cinemaweb.API.Cinema.Web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Theo dõi người dùng đang online qua Redis. Đếm số phiên (tab) mỗi user để
 * nhiều tab không khiến user bị coi là offline khi đóng một tab.
 * Set "chat:online" giữ các userId đang online để truy vấn nhanh.
 */
@Service
public class PresenceService {

    private static final String ONLINE_KEY = "chat:online";
    private static final String SESSION_PREFIX = "chat:sessions:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /** Tăng số phiên. Trả về true nếu user vừa chuyển sang online (0 → 1). */
    public boolean markOnline(String userId) {
        Long count = redisTemplate.opsForValue().increment(SESSION_PREFIX + userId);
        if (count != null && count == 1L) {
            redisTemplate.opsForSet().add(ONLINE_KEY, userId);
            return true;
        }
        return false;
    }

    /** Giảm số phiên. Trả về true nếu user vừa chuyển sang offline (1 → 0). */
    public boolean markOffline(String userId) {
        Long count = redisTemplate.opsForValue().decrement(SESSION_PREFIX + userId);
        if (count == null || count <= 0L) {
            redisTemplate.delete(SESSION_PREFIX + userId);
            redisTemplate.opsForSet().remove(ONLINE_KEY, userId);
            return true;
        }
        return false;
    }

    public boolean isOnline(String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_KEY, userId));
    }

    public Set<String> onlineAmong(Collection<String> userIds) {
        return userIds.stream().filter(this::isOnline).collect(Collectors.toSet());
    }
}
