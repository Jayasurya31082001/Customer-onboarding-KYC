package com.example.notificationservice;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class NotificationServiceApplicationTest {

    @Test
    void notificationTaskExecutor_hasExpectedConfiguration() {
        NotificationServiceApplication app = new NotificationServiceApplication();

        Executor executor = app.notificationTaskExecutor();

        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor pool = (ThreadPoolTaskExecutor) executor;
        assertThat(pool.getCorePoolSize()).isEqualTo(4);
        assertThat(pool.getMaxPoolSize()).isEqualTo(20);
        assertThat(pool.getThreadNamePrefix()).isEqualTo("notification-async-");
        pool.shutdown();
    }
}
