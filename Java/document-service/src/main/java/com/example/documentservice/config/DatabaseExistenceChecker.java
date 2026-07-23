package com.example.documentservice.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseExistenceChecker implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseExistenceChecker.class);

    private final DataSource dataSource;

    public DatabaseExistenceChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseProductVersion = metaData.getDatabaseProductVersion();

            log.info("==========================================================================");
            log.info("[DATABASE CHECK] Successfully connected to database!");
            log.info("[DATABASE CHECK] URL: {}", url);
            log.info("[DATABASE CHECK] Product: {} {}", databaseProductName, databaseProductVersion);
            log.info("==========================================================================");
        } catch (Exception e) {
            log.error("==========================================================================");
            log.error("[DATABASE CHECK FAILED] Could not verify database existence or connection.");
            log.error("[DATABASE CHECK FAILED] Error message: {}", e.getMessage());
            log.error("[DATABASE CHECK FAILED] If auto-create (createDatabaseIfNotExist=true) is disabled");
            log.error("[DATABASE CHECK FAILED] or MySQL is not running, please start MySQL and run:");
            log.error("[DATABASE CHECK FAILED] CREATE DATABASE IF NOT EXISTS documentdb;");
            log.error("==========================================================================");
        }
    }
}
