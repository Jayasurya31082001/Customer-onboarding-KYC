package com.example.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.notificationservice.exception.ResourceNotFoundException;
import com.example.notificationservice.model.Template;
import com.example.notificationservice.repository.TemplateRepository;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Test
    void shouldLoadTemplateOnCacheMiss() {
        NotificationTemplateService service = new NotificationTemplateService(templateRepository);
        Template template = Template.builder()
                .templateId(1L)
                .templateKey("ACCOUNT_CREATED")
                .subject("subject")
                .content("content")
                .active(true)
                .build();

        when(templateRepository.findByTemplateKeyAndActiveTrue("ACCOUNT_CREATED")).thenReturn(Optional.of(template));

        Template resolved = service.getActiveTemplate("ACCOUNT_CREATED");

        assertThat(resolved.getTemplateKey()).isEqualTo("ACCOUNT_CREATED");
        verify(templateRepository, times(1)).findByTemplateKeyAndActiveTrue("ACCOUNT_CREATED");
    }

    @Test
    void shouldFailWhenTemplateMissingOnCacheMiss() {
        NotificationTemplateService service = new NotificationTemplateService(templateRepository);
        when(templateRepository.findByTemplateKeyAndActiveTrue("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActiveTemplate("MISSING"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Template not found");
    }
}
