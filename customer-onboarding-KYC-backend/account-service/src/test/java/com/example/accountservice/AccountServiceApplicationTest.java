package com.example.accountservice;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class AccountServiceApplicationTest {

    @Test
    void accountTaskExecutor_hasExpectedConfiguration() {
        AccountServiceApplication app = new AccountServiceApplication();

        Executor executor = app.accountTaskExecutor();

        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) executor;
        assertThat(pool.getCorePoolSize()).isEqualTo(4);
        assertThat(pool.getMaxPoolSize()).isEqualTo(20);
        assertThat(pool.getThreadNamePrefix()).isEqualTo("account-async-");
        pool.shutdown();
    }
}
