package com.example.notificationservice.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.notificationservice.model.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /** Incremental sync query — returns notifications created or updated after the cursor. */
    Page<NotificationLog> findByCreatedAtAfterOrUpdatedAtAfter(
            LocalDateTime createdAfter, LocalDateTime updatedAfter, Pageable pageable);
}
