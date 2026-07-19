package com.example.kycservice.kyc.provider;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.example.kycservice.customer.client.CustomerProfile;
import com.example.kycservice.document.client.DocumentDetails;
import com.example.kycservice.kyc.model.KycCase;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
public class KycProviderService {

    @CircuitBreaker(name = "kycProvider", fallbackMethod = "kycFallback")
    @Retry(name = "kycProvider")
    public KycResult callKycProvider(KycCase kycCase, CustomerProfile customer, DocumentDetails documentDetails) {
        // Validate provided information matches customer profile
        boolean profileMatched = normalize(kycCase.getProvidedFirstName()).equals(normalize(customer.firstName()))
                && normalize(kycCase.getProvidedLastName()).equals(normalize(customer.lastName()))
                && kycCase.getProvidedDateOfBirth().equals(customer.dateOfBirth());

        if (!profileMatched) {
            return new KycResult(KycResult.Status.FAIL,
                    "KYC failed. Uploaded document details do not match customer profile");
        }

        // Validate document name matches expected format: first_name_lastName
        boolean documentNameMatched = validateDocumentName(documentDetails.fileName(), customer);

        if (!documentNameMatched) {
            return new KycResult(KycResult.Status.FAIL,
                    "KYC failed. Document file name does not match customer profile (expected format: firstname_lastName)");
        }

        return new KycResult(KycResult.Status.PASS,
                "KYC passed. Customer profile and document name match");
    }

    private boolean validateDocumentName(String fileName, CustomerProfile customer) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }

        // Extract file name without extension
        String fileNameWithoutExtension = removeFileExtension(fileName);

        // Create expected document name format: first_name_lastName
        String expectedDocumentName = normalize(customer.firstName()) + "_" + normalize(customer.lastName());

        // Compare normalized versions
        return normalize(fileNameWithoutExtension).equals(expectedDocumentName);
    }

    private String removeFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }

    public KycResult kycFallback(KycCase kycCase, CustomerProfile customer, DocumentDetails documentDetails, Exception exception) {
        return new KycResult(KycResult.Status.REFER,
                "Provider unavailable — manual review required");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}

