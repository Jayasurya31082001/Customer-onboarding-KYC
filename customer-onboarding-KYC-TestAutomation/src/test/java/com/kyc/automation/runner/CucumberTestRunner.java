package com.kyc.automation.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * JUnit 5 Suite runner for the Customer Onboarding KYC Cucumber BDD test suite.
 *
 * <p>Runs all feature files under {@code src/test/resources/features/}.
 * Uses the JUnit Platform Suite API with the Cucumber engine.
 *
 * <p>Reporting:
 * <ul>
 *   <li>HTML report – {@code target/cucumber-reports/cucumber.html}</li>
 *   <li>JSON report – {@code target/cucumber-reports/cucumber.json}</li>
 *   <li>JUnit XML  – {@code target/surefire-reports/} (picked up by GitHub Actions)</li>
 * </ul>
 *
 * <p>No WebDriver or browser configuration is present; this is a pure API test runner.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty," +
                "html:target/cucumber-reports/cucumber.html," +
                "json:target/cucumber-reports/cucumber.json," +
                "junit:target/cucumber-reports/cucumber.xml")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.kyc.automation.steps,com.kyc.automation.hooks")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME,
        value = "not @wip")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME,
        value = "src/test/resources/features")
public class CucumberTestRunner {
    // This class is intentionally empty – it serves only as the JUnit Platform Suite entry point.
}
