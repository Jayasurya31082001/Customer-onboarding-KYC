package com.example.documentservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

class CacheConfigTest {

    @Test
    void cacheManager_containsDocumentsCache() {
        CacheConfig config = new CacheConfig();

        CacheManager manager = config.cacheManager();

        assertThat(manager.getCache(CacheConfig.DOCUMENTS_CACHE)).isNotNull();
    }
}
