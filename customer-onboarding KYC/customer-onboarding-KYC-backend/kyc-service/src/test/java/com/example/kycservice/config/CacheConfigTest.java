package com.example.kycservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

class CacheConfigTest {

    @Test
    void cacheManager_containsKycStatusCache() {
        CacheConfig config = new CacheConfig();

        CacheManager manager = config.cacheManager();

        assertThat(manager.getCache("kyc-status")).isNotNull();
    }
}
