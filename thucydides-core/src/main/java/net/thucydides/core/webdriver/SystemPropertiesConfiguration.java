package net.thucydides.core.webdriver;

import com.google.inject.Inject;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.util.EnvironmentVariables;
import net.thucydides.core.util.SystemEnvironmentVariables;

import java.io.File;

import static net.thucydides.core.ThucydidesSystemProperty.BASE_URL;

/**
 * Centralized configuration of the test runner. You can configure the output
 * directory, the browser to use, and the reports to generate. Most
 * configuration elements can be set using system properties.
 * 
 */
public class SystemPropertiesConfiguration implements Configuration {

    /**
     * Use the 'webdriver.driver' property to tell Thucydides what browser to
     * run the tests in.
     */
    public static final String WEBDRIVER_DRIVER = ThucydidesSystemProperty.DRIVER.getPropertyName();

    /**
     * The default browser is Firefox.
     */
    public static final String DEFAULT_WEBDRIVER_DRIVER = "firefox";

    /**
     * Default timeout when waiting for AJAX elements in pages, in milliseconds.
     */
    public static final int DEFAULT_ELEMENT_TIMEOUT_SECONDS = 5;

    public final Integer DEFAULT_ESTIMATED_AVERAGE_STEP_COUNT = 5;

    private String defaultBaseUrl;

    /**
     * Use this property to define the output directory in which reports will be
     * stored.
     */
    public static final String OUTPUT_DIRECTORY_PROPERTY = ThucydidesSystemProperty.OUTPUT_DIRECTORY.getPropertyName();

    /**
     * By default, when accepting untrusted SSL certificates, assume that these certificates will come from an
     * untrusted issuer or will be self signed. Due to limitation within Firefox, it is easy to find out if the
     * certificate has expired or does not match the host it was served for, but hard to find out if the issuer of
     * the certificate is untrusted. By default, it is assumed that the certificates were not be issued from a trusted
     * CA. If you are receive an "untrusted site" prompt on Firefox when using a certificate that was issued by valid
     * issuer, but has expired or is being served served for a different host (e.g. production certificate served in
     * a testing environment) set this to false.
     */
    public static final String ASSUME_UNTRUSTED_CERTIFICATE_ISSUER
            = ThucydidesSystemProperty.ASSUME_UNTRUSTED_CERTIFICATE_ISSUER.getPropertyName();

    /**
     * By default, reports will go here.
     */
    private static final String DEFAULT_OUTPUT_DIRECTORY = "target/site/thucydides";

    /**
     * HTML and XML reports will be generated in this directory.
     */
    private File outputDirectory;    

    private EnvironmentVariables environmentVariables;

    @Inject
    public SystemPropertiesConfiguration(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    protected EnvironmentVariables getEnvironmentVariables() {
        if (environmentVariables == null) {
            environmentVariables = Injectors.getInjector().getInstance(EnvironmentVariables.class);
        }
        return environmentVariables;
    }

    /**
     * Get the currently-configured browser type.
     */
    public SupportedWebDriver getDriverType() {
        String driverType = getEnvironmentVariables().getProperty(WEBDRIVER_DRIVER, DEFAULT_WEBDRIVER_DRIVER);
        return lookupSupportedDriverTypeFor(driverType);
    }

    /**
     * Where should the reports go?
     */
    public File loadOutputDirectoryFromSystemProperties() {
        String systemDefinedDirectory = getEnvironmentVariables().getProperty(OUTPUT_DIRECTORY_PROPERTY);
        if (systemDefinedDirectory == null) {
            systemDefinedDirectory = DEFAULT_OUTPUT_DIRECTORY;
        }
        return new File(systemDefinedDirectory);
    }

    public int getStepDelay() {
        int stepDelay = 0;

        String stepDelayValue = getEnvironmentVariables().getProperty(ThucydidesSystemProperty.STEP_DELAY.getPropertyName());
        if ((stepDelayValue != null) && (!stepDelayValue.isEmpty())) {
            stepDelay = Integer.valueOf(stepDelayValue);
        }
        return stepDelay;

    }

    public int getElementTimeout() {
        int elementTimeout = DEFAULT_ELEMENT_TIMEOUT_SECONDS;

        String stepDelayValue = getEnvironmentVariables().getProperty(ThucydidesSystemProperty.ELEMENT_TIMEOUT.getPropertyName());
        if ((stepDelayValue != null) && (!stepDelayValue.isEmpty())) {
            elementTimeout = Integer.valueOf(stepDelayValue);
        }
        return elementTimeout;

    }

    public boolean getUseUniqueBrowser() {
        boolean uniqueBrowser = false;
        String uniqueBrowserValue = getEnvironmentVariables().getProperty(ThucydidesSystemProperty.UNIQUE_BROWSER.getPropertyName());
        if (uniqueBrowserValue != null) {
            uniqueBrowser = Boolean.valueOf(uniqueBrowserValue);
        }
        return uniqueBrowser;
    }

    public void setOutputDirectory(final File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * The output directory is where the test runner writes the XML and HTML
     * reports to. By default, it will be in 'target/site/thucydides', but you can
     * override this value either programmatically or by providing a value in
     * the <b>thucydides.output.dir</b> system property.
     * 
     */
    public File getOutputDirectory() {
        outputDirectory = loadOutputDirectoryFromSystemProperties();
        outputDirectory.mkdirs();
        return outputDirectory;
    }

    public double getEstimatedAverageStepCount() {
        return getEnvironmentVariables().getPropertyAsInteger(ThucydidesSystemProperty.ESTIMATED_AVERAGE_STEP_COUNT.getPropertyName(),
                DEFAULT_ESTIMATED_AVERAGE_STEP_COUNT);
    }

    public boolean onlySaveFailingScreenshots() {
        return getEnvironmentVariables().getPropertyAsBoolean(ThucydidesSystemProperty.ONLY_SAVE_FAILING_SCREENSHOTS.getPropertyName(), false);
    }

    /**
     * Override the default base URL manually.
     * Normally only needed for testing.
     */
    public void setDefaultBaseUrl(final String defaultBaseUrl) {
        this.defaultBaseUrl = defaultBaseUrl;
    }

    public int getRestartFrequency() {
        return environmentVariables.getPropertyAsInteger(
                        ThucydidesSystemProperty.RESTART_BROWSER_FREQUENCY.getPropertyName(),3);

    }

    /**
     * This is the URL where test cases start.
     * The default value can be overriden using the webdriver.baseurl property.
     * It is also the base URL used to build relative paths.
     */
    public String getBaseUrl() {
        return environmentVariables.getProperty(BASE_URL.getPropertyName(), defaultBaseUrl);
    }
    /**
     * Transform a driver type into the SupportedWebDriver enum. Driver type can
     * be any case.
     * 
     * @throws UnsupportedDriverException
     */
    private SupportedWebDriver lookupSupportedDriverTypeFor(final String driverType) {
        SupportedWebDriver driver = null;
        try {
            driver = SupportedWebDriver.getDriverTypeFor(driverType);
        } catch (IllegalArgumentException iae) {
            throwUnsupportedDriverExceptionFor(driverType);
        }
        return driver;
    }

    private void throwUnsupportedDriverExceptionFor(final String driverType) {
        throw new UnsupportedDriverException(driverType
                + " is not a supported browser. Supported driver values are: "
                + SupportedWebDriver.listOfSupportedDrivers());
    }

}
