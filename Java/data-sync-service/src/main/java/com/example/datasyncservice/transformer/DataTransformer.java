package com.example.datasyncservice.transformer;

import com.example.datasyncservice.dto.databricks.DatabricksRecord;

/**
 * Strategy interface for transforming a domain sync record into a {@link DatabricksRecord}.
 *
 * <p>Each implementation handles one source type (Customer, Account, Kyc, etc.).
 * The {@link com.example.datasyncservice.processor.DataTransformProcessor} resolves
 * the correct implementation at runtime using a {@code Map<String, DataTransformer>}
 * keyed by {@link #supportedType()}.
 *
 * <p><b>SOLID — Open/Closed:</b> Adding a new microservice requires only adding a new
 * {@code @Component} that implements this interface — no changes to the processor.
 *
 * @param <T> the inbound domain sync record type
 */
public interface DataTransformer<T> {

    /**
     * Transforms an inbound sync record into the canonical Databricks shape.
     *
     * @param record the domain record from the upstream service
     * @return the normalised {@link DatabricksRecord} ready for writing
     */
    DatabricksRecord transform(T record);

    /**
     * Returns the simple class name of the record type this transformer handles.
     * Used as the key in the transformer registry map.
     *
     * <p>Example: {@code "CustomerSyncRecord"}
     */
    String supportedType();
}
