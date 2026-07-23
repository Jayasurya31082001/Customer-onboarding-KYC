package com.example.documentservice.document.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.documentservice.document.model.CustomerDocument;

public interface DocumentRepository extends JpaRepository<CustomerDocument, UUID> {

    Optional<CustomerDocument> findFirstByCustomerIdOrderByCreatedAtDescDocumentIdDesc(UUID customerId);

    /** Incremental sync query — returns documents created or updated after the cursor. */
    Page<CustomerDocument> findByCreatedAtAfterOrUpdatedAtAfter(
            LocalDateTime createdAfter, LocalDateTime updatedAfter, Pageable pageable);
}
