package com.example.accountservice.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
public class CacheConfig {

    private final ConcurrentMapCacheManager cacheManager;

    public CacheConfig() {
        this.cacheManager = new ConcurrentMapCacheManager("accounts", "account-audit");
        this.cacheManager.setAllowNullValues(false);
    }

    @Bean
    public CacheManager cacheManager() {
        return cacheManager;
    }

    @Scheduled(fixedRateString = "${cache.eviction.interval-ms:300000}")
    public void evictAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }
}
