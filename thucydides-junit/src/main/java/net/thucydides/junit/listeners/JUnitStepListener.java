package net.thucydides.junit.listeners;

import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.steps.BaseStepListener;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.steps.StepListener;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Intercepts JUnit events and reports them to Thucydides.
 */
public class JUnitStepListener extends RunListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(JUnitStepListener.class);

    private BaseStepListener baseStepListener;
    private boolean testStarted;

    public static JUnitStepListenerBuilder withOutputDirectory(File outputDirectory) {
        return new JUnitStepListenerBuilder(outputDirectory);     
    }
    
    protected JUnitStepListener(BaseStepListener baseStepListener, StepListener... listeners) {
        testStarted = false;
        this.baseStepListener = baseStepListener;
        StepEventBus.getEventBus().registerListener(baseStepListener);

        for(StepListener listener : listeners) {
            StepEventBus.getEventBus().registerListener(listener);
        }
    }

    public BaseStepListener getBaseStepListener() {
        return baseStepListener;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);

    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        StepEventBus.getEventBus().testSuiteFinished();
        super.testRunFinished(result);
    }

    /**
     * Called when a test starts. We also need to start the test suite the first
     * time, as the testRunStarted() method is not invoked for some reason.
     */
    @Override
    public void testStarted(final Description description) {
        StepEventBus.getEventBus().clear();
        StepEventBus.getEventBus().testStarted(description.getMethodName(),
                                               description.getTestClass());
        startTest();
    }

    @Override
    public void testFinished(final Description description) throws Exception {
        endTest();
    }

    @Override
    public void testFailure(final Failure failure) throws Exception {
        startTestIfNotYetStarted(failure.getDescription());
        StepEventBus.getEventBus().testFailed(failure.getException());
        endTest();
    }

    private void startTestIfNotYetStarted(Description description) {
        if (!testStarted) {
           testStarted(description);
        }
    }

    @Override
    public void testIgnored(final Description description) throws Exception {
        StepEventBus.getEventBus().testIgnored();
        endTest();
    }

    public List<TestOutcome> getTestOutcomes() {
        return baseStepListener.getTestOutcomes();
    }

    public Throwable getError() {
        return baseStepListener.getTestFailureCause();
    }

    public boolean hasRecordedFailures() {
        return baseStepListener.aStepHasFailed();
    }

    public void close() {
        StepEventBus.getEventBus().dropListener(baseStepListener);
    }

    private void startTest() {
        testStarted = true;
    }
    private void endTest() {
        testStarted = false;
    }
}
