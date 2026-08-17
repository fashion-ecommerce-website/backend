package com.spring.fit.backend.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
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
 * Redis Cache Configuration
 *
 * Cache strategy:
 * - newArrivals: 10 minutes TTL (products changed infrequently)
 * - bestSellers: 30 minutes TTL (ranking updated rarely)
 * - categories: 1 hour TTL (rarely changes)
 * - productDetail: 5 minutes TTL (price/promo changes possible)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * ObjectMapper configured to serialize Java time types and include type info
     * so Redis can deserialize back to correct types.
     */
    private ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Include type info so polymorphic types are deserialized correctly
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(cacheObjectMapper());

        // Default cache config
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // Per-cache TTL configurations
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // New arrivals: refresh every 10 minutes
        cacheConfigs.put("newArrivals",
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Best sellers: refresh every 30 minutes (ranking stable)
        cacheConfigs.put("bestSellers",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Categories: refresh every 1 hour (very rarely changes)
        cacheConfigs.put("categories",
                defaultConfig.entryTtl(Duration.ofHours(1)));

        // Product detail: 5 minutes (prices/promotions may change)
        cacheConfigs.put("productDetail",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
