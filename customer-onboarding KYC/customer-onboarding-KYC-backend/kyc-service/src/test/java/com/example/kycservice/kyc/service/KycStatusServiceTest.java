package com.example.kycservice.kyc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kycservice.common.exception.ResourceNotFoundException;
import com.example.kycservice.kyc.model.KycCase;
import com.example.kycservice.kyc.model.KycStatus;
import com.example.kycservice.kyc.repository.KycCaseRepository;

@ExtendWith(MockitoExtension.class)
class KycStatusServiceTest {

    @Mock
    private KycCaseRepository repository;

    private KycStatusService statusService;

    @BeforeEach
    void setUp() {
        statusService = new KycStatusService(repository);
    }

    @Test
    void getKycStatus_returnsLatestStatus() {
        UUID customerId = UUID.randomUUID();
        KycCase kycCase = KycCase.builder()
                .kycCaseId(UUID.randomUUID())
                .customerId(customerId)
                .documentId(UUID.randomUUID())
                .idempotencyKey("idem")
                .providedFirstName("Jane")
                .providedLastName("Doe")
                .providedDateOfBirth(java.time.LocalDate.of(1990, 1, 1))
                .status(KycStatus.PASS)
                .notes("ok")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        when(repository.findTopByCustomerIdOrderByUpdatedAtDesc(customerId)).thenReturn(Optional.of(kycCase));

        KycStatus status = statusService.getKycStatus(customerId);

        assertThat(status).isEqualTo(KycStatus.PASS);
    }

    @Test
    void getKycStatus_throwsWhenNotFound() {
        UUID customerId = UUID.randomUUID();
        when(repository.findTopByCustomerIdOrderByUpdatedAtDesc(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statusService.getKycStatus(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("KYC status not found");
    }
}
