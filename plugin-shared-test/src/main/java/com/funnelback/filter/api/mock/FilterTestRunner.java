package com.funnelback.filter.api.mock;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Utility class for running JUnit tests programmatically.
 * Updated to use JUnit 5 (JUnit Platform) APIs.
 */
public class FilterTestRunner {

    /**
     * Runs junit tests that are found on the given class
     * 
     * @param classWithJunitTests the test class to run
     * @return a summary of the test execution
     */
    public static TestExecutionSummary runTests(Class<?> classWithJunitTests) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(classWithJunitTests))
            .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        
        // Print summary similar to JUnit 4 behavior
        System.out.println("Tests run: " + summary.getTestsStartedCount() + 
            ", Failures: " + summary.getTestsFailedCount() + 
            ", Skipped: " + summary.getTestsSkippedCount());
        
        summary.getFailures().forEach(failure -> {
            System.out.println("Failure in: " + failure.getTestIdentifier().getDisplayName());
            if (failure.getException() instanceof AssertionError) {
                System.out.println("\t" + failure.getException().getMessage());
            } else {
                failure.getException().printStackTrace();
            }
        });
        
        return summary;
    }
}
