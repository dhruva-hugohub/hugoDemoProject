package com.hugo.demo.config;

import java.time.Duration;
import java.util.HashMap;

import com.hugo.demo.constants.ResourceConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues();
    }

    @Bean(name = ResourceConstants.BEAN_CACHE_MANAGER_REDIS)
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        HashMap<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        RedisCacheConfiguration userCacheConfiguration = defaultCacheConfiguration();
        cacheConfigurations.put(ResourceConstants.CACHE_NAME_USER, userCacheConfiguration.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put(ResourceConstants.CACHE_NAME_LIVE_ITEM, userCacheConfiguration.entryTtl(Duration.ofMinutes(6)));
        cacheConfigurations.put(ResourceConstants.CACHE_NAME_DATE_ITEM, userCacheConfiguration.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(ResourceConstants.CACHE_NAME_PRODUCT, userCacheConfiguration.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultCacheConfiguration())
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }

    @Bean
    public RedisTemplate<String, Message> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Message> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new GenericToStringSerializer<>(String.class));
        template.setValueSerializer(new GenericToStringSerializer<>(String.class));

        template.afterPropertiesSet();
        return template;
    }
}
