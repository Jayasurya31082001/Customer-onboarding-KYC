package com.example.customerservice.customer.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.customerservice.customer.model.Customer;
import com.example.customerservice.customer.model.OnboardingStatus;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByEmail(String email);

    List<Customer> findAllByStatus(OnboardingStatus status);

    /**
     * Returns a page of customers whose record was created or updated after the given timestamp.
     * Used exclusively by {@code CustomerSyncController} for incremental data sync.
     *
     * @param createdAfter  cursor for newly created customers
     * @param updatedAfter  cursor for recently updated customers (same value, overlap already applied)
     * @param pageable      page + size + sort
     */
    Page<Customer> findByCreatedAtAfterOrUpdatedAtAfter(
            LocalDateTime createdAfter,
            LocalDateTime updatedAfter,
            Pageable pageable);
}

