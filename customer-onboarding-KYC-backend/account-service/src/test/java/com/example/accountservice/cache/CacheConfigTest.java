package com.example.accountservice.cache;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class CacheConfigTest {

    @Test
    void cacheManager_containsExpectedCachesAndEvictClearsEntries() {
        CacheConfig config = new CacheConfig();
        CacheManager manager = config.cacheManager();

        Cache accounts = manager.getCache("accounts");
        assertThat(accounts).isNotNull();
        accounts.put("cust-1", "value-1");
        assertThat(accounts.get("cust-1")).isNotNull();

        config.evictAllCaches();

        assertThat(accounts.get("cust-1")).isNull();
        assertThat(manager.getCache("account-audit")).isNotNull();
    }
}
