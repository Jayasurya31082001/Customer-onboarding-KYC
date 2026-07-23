package com.example.notificationservice.cache;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class CacheConfigTest {

    @Test
    void cacheManager_containsTemplateCacheAndEvictClearsEntries() {
        CacheConfig config = new CacheConfig();
        CacheManager manager = config.cacheManager();

        Cache cache = manager.getCache("notification-templates");
        assertThat(cache).isNotNull();
        cache.put("ACCOUNT_CREATED", "template");
        assertThat(cache.get("ACCOUNT_CREATED")).isNotNull();

        config.evictAllCaches();

        assertThat(cache.get("ACCOUNT_CREATED")).isNull();
    }
}
