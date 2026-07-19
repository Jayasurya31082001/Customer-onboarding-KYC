package com.bank.onboarding.risk.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String RISK_RULES_CACHE = "risk-rules";
    public static final String COMPLIANCE_PENDING_CACHE = "compliance-pending";

    private final ConcurrentMapCacheManager cacheManager;

    public CacheConfig() {
        this.cacheManager = new ConcurrentMapCacheManager(RISK_RULES_CACHE, COMPLIANCE_PENDING_CACHE);
        this.cacheManager.setAllowNullValues(false);
    }

    @Bean
    public CacheManager cacheManager() {
        return cacheManager;
    }

    @Scheduled(fixedRateString = "${cache.eviction.interval-ms:300000}")
    public void evictCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }
}