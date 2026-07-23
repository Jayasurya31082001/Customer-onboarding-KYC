package com.example.notificationservice.e2e;

import java.time.Duration;
import java.util.concurrent.Executor;

import static org.awaitility.Awaitility.await;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notificationservice.model.Template;
import com.example.notificationservice.provider.client.NotificationSenderClient;
import com.example.notificationservice.repository.TemplateRepository;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.datasource.url=jdbc:h2:mem:notificationdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.liquibase.enabled=false"
})
@AutoConfigureMockMvc
@Import(NotificationFlowE2ETest.SyncAsyncConfig.class)
class NotificationFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TemplateRepository templateRepository;

    @MockBean
    private NotificationSenderClient notificationSenderClient;

    @BeforeEach
    void setUp() {
        templateRepository.deleteAll();
        templateRepository.save(Template.builder()
                .templateKey("ACCOUNT_CREATED")
                .subject("Account Created")
                .content("Dear customer ${customerId}, your account ${accountNumber} with sort code ${sortCode} has been created.")
                .active(true)
                .build());
        templateRepository.save(Template.builder()
                .templateKey("APPLICATION_REJECTED")
                .subject("Application Rejected")
                .content("Dear customer ${customerId}, your onboarding application was rejected. Reason: ${reason}.")
                .active(true)
                .build());
    }

    @Test
    @DisplayName("E2E: account-created ingress dispatches a templated notification")
    void accountCreatedFlowShouldDispatchTemplatedNotification() throws Exception {
        String requestBody = """
                {
                  "customerId": "cust-1001",
                                    "customerEmail": "cust-1001@example.com",
                  "accountNumber": "12345678",
                  "sortCode": "10-20-30"
                }
                """;

        mockMvc.perform(post("/api/internal/events/account-created")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isAccepted());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(notificationSenderClient).send(
                    eq("cust-1001@example.com"),
                    eq("Dear customer cust-1001, your account 12345678 with sort code 10-20-30 has been created."),
                    isNull());
        });
    }

    @TestConfiguration
    static class SyncAsyncConfig {

        @Bean("notificationTaskExecutor")
        @Primary
        Executor notificationTaskExecutor() {
            return new ConcurrentTaskExecutor(Runnable::run);
        }
    }
}
