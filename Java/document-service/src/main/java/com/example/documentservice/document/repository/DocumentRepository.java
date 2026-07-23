package com.example.documentservice.document.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.documentservice.document.model.CustomerDocument;

public interface DocumentRepository extends JpaRepository<CustomerDocument, UUID> {

	Optional<CustomerDocument> findFirstByCustomerIdOrderByCreatedAtDescDocumentIdDesc(UUID customerId);
}
