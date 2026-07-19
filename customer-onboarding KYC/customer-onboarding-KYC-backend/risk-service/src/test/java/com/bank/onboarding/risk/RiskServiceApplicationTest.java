package com.bank.onboarding.risk;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class RiskServiceApplicationTest {

    @Test
    void riskTaskExecutor_hasExpectedConfiguration() {
        RiskServiceApplication app = new RiskServiceApplication();

        Executor executor = app.riskTaskExecutor();

        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) executor;
        assertThat(pool.getCorePoolSize()).isEqualTo(4);
        assertThat(pool.getMaxPoolSize()).isEqualTo(20);
        assertThat(pool.getThreadNamePrefix()).isEqualTo("risk-async-");
        pool.shutdown();
    }
}
