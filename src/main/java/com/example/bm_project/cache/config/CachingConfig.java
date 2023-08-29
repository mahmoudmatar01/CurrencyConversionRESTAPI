package com.example.bm_project.cache.config;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CachingConfig {
    private final long expireAfterDuration=1;
    private final String expireAfterTimeUnit="HOURS";
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(expireAfterDuration, TimeUnit.valueOf(expireAfterTimeUnit)) // Cache entries expire after 1 hour
                .maximumSize(100));
        return cacheManager;
}

}
