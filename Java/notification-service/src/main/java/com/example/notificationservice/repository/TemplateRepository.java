package com.example.notificationservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.notificationservice.model.Template;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByTemplateKeyAndActiveTrue(String templateKey);
}
