package com.cinemaweb.API.Cinema.Web.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình Spring Cache backed by Redis cho nhóm dữ liệu "catalog"
 * (đọc nhiều, thay đổi ít). KHÔNG dùng cho seat map / booking (biến động cao).
 *
 * <p>Mọi key cache dùng prefix riêng {@code cache:} để không đụng các prefix
 * token đang dùng trong AuthenticationService (refresh_token:, blacklist_token:, refresh_rotated:).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // Cache names (dùng làm value trong @Cacheable/@CacheEvict)
    public static final String MOVIES = "movies";
    public static final String MOVIE = "movie";
    public static final String CINEMAS = "cinemas";
    public static final String CINEMA = "cinema";
    public static final String ROOMS = "rooms";
    public static final String ROOM = "room";
    public static final String FOODS = "foods";
    public static final String FOOD = "food";
    public static final String ROLES = "roles";
    public static final String PERMISSIONS = "permissions";

    private RedisCacheConfiguration baseConfig(Duration ttl) {
        // ObjectMapper riêng cho cache: cần JavaTimeModule (LocalDate trong các Response)
        // và default typing để GenericJackson2 lưu/khôi phục đúng kiểu (kể cả List<...>).
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .prefixCacheNameWith("cache:")
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Duration oneHour = Duration.ofHours(1);
        Duration halfDay = Duration.ofHours(12);
        Duration oneDay = Duration.ofHours(24);

        Map<String, RedisCacheConfiguration> caches = new HashMap<>();
        caches.put(MOVIES, baseConfig(oneHour));
        caches.put(MOVIE, baseConfig(oneHour));
        caches.put(CINEMAS, baseConfig(oneDay));
        caches.put(CINEMA, baseConfig(oneDay));
        caches.put(ROOMS, baseConfig(halfDay));
        caches.put(ROOM, baseConfig(halfDay));
        caches.put(FOODS, baseConfig(oneHour));
        caches.put(FOOD, baseConfig(oneHour));
        caches.put(ROLES, baseConfig(oneDay));
        caches.put(PERMISSIONS, baseConfig(oneDay));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig(oneHour))
                .withInitialCacheConfigurations(caches)
                .enableStatistics() // cho phép Micrometer/Actuator thu hit/miss của RedisCache
                .build();
    }
}
