package com.example.datasyncservice.config;

import com.example.datasyncservice.client.DatabricksClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Spring Boot Actuator health indicator for Databricks connectivity.
 *
 * <p>Contributes to the composite {@code /actuator/health} response:
 * <pre>
 * {
 *   "status": "UP",
 *   "components": {
 *     "databricks": { "status": "UP" },
 *     "db": { "status": "UP" },
 *     ...
 *   }
 * }
 * </pre>
 *
 * <p>An alert on {@code databricks.status = DOWN} in Grafana gives early warning
 * that the sync service can reach MySQL but not Databricks.
 */
@Component("databricks")
public class DatabricksHealthIndicator implements HealthIndicator {

    private final DatabricksClient databricksClient;

    public DatabricksHealthIndicator(DatabricksClient databricksClient) {
        this.databricksClient = databricksClient;
    }

    @Override
    public Health health() {
        try {
            boolean healthy = databricksClient.isHealthy();
            return healthy
                    ? Health.up().withDetail("workspace", "reachable").build()
                    : Health.down().withDetail("workspace", "unreachable").build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("error", ex.getMessage()).build();
        }
    }
}
