package com.example.notificationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.notificationservice.model.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}
