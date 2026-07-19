package com.bank.onboarding.risk.cache;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class CacheConfigTest {

    @Test
    void cacheManager_containsExpectedCachesAndEvictClearsEntries() {
        CacheConfig config = new CacheConfig();
        CacheManager manager = config.cacheManager();

        Cache riskRules = manager.getCache(CacheConfig.RISK_RULES_CACHE);
        assertThat(riskRules).isNotNull();
        riskRules.put("rule-key", "rule-value");
        assertThat(riskRules.get("rule-key")).isNotNull();

        config.evictCaches();

        assertThat(riskRules.get("rule-key")).isNull();
        assertThat(manager.getCache(CacheConfig.COMPLIANCE_PENDING_CACHE)).isNotNull();
    }
}
