package com.example.notificationservice.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.notificationservice.exception.ResourceNotFoundException;
import com.example.notificationservice.model.Template;
import com.example.notificationservice.repository.TemplateRepository;

@Service
public class NotificationTemplateService {

    private final TemplateRepository templateRepository;

    public NotificationTemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "notification-templates", key = "#templateKey")
    public Template getActiveTemplate(String templateKey) {
        return templateRepository.findByTemplateKeyAndActiveTrue(templateKey)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found for key=" + templateKey));
    }
}
