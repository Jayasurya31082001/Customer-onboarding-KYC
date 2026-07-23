package com.example.datasyncservice.processor;

import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import com.example.datasyncservice.transformer.DataTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Spring Batch {@link ItemProcessor} that routes each inbound sync record to the
 * correct {@link DataTransformer} implementation using the <b>Strategy Pattern</b>.
 *
 * <p>The transformer registry is built from all {@code @Component} implementations
 * of {@link DataTransformer} discovered by Spring's component scan. Adding a new
 * service requires only a new transformer — this class never changes (Open/Closed).
 *
 * <p>Returns {@code null} for any record whose type has no registered transformer,
 * which causes Spring Batch to silently skip that item (logged as WARN).
 */
@Component
public class DataTransformProcessor implements ItemProcessor<Object, DatabricksRecord> {

    private static final Logger log = LoggerFactory.getLogger(DataTransformProcessor.class);

    /** Registry keyed by the simple class name of the record type (e.g. "CustomerSyncRecord"). */
    private final Map<String, DataTransformer<?>> transformerRegistry;

    /**
     * Spring injects all {@link DataTransformer} beans as a list.
     * We convert them to a Map keyed by {@link DataTransformer#supportedType()}.
     */
    public DataTransformProcessor(List<DataTransformer<?>> transformers) {
        this.transformerRegistry = transformers.stream()
                .collect(Collectors.toMap(DataTransformer::supportedType, Function.identity()));

        log.info("DataTransformProcessor initialised with {} transformers: {}",
                transformerRegistry.size(), transformerRegistry.keySet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public DatabricksRecord process(Object record) {
        if (record == null) {
            return null;
        }

        String typeName = record.getClass().getSimpleName();
        DataTransformer<Object> transformer =
                (DataTransformer<Object>) transformerRegistry.get(typeName);

        if (transformer == null) {
            log.warn("No transformer registered for record type: {}. Skipping.", typeName);
            return null;  // Spring Batch skips null-returning processors
        }

        try {
            DatabricksRecord result = transformer.transform(record);
            log.debug("Transformed {} [id={}] → DatabricksRecord [checksum={}]",
                    typeName, result.recordId(), result.checksum());
            return result;
        } catch (Exception ex) {
            log.error("Transformation failed for {} — skipping record. Cause: {}",
                    typeName, ex.getMessage(), ex);
            return null;  // Skip malformed records; counted in skip limit
        }
    }
}
