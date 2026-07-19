package com.example.customerservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

class CacheConfigTest {

    @Test
    void cacheManager_containsCustomersCache() {
        CacheConfig config = new CacheConfig();

        CacheManager cacheManager = config.cacheManager();

        assertThat(cacheManager.getCache(CacheConfig.CUSTOMERS_CACHE)).isNotNull();
    }
}
