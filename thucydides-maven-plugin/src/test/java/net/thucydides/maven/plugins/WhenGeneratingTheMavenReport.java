package net.thucydides.maven.plugins;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.XhtmlBaseSink;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.StringWriter;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

public class WhenGeneratingTheMavenReport {

    ThucydidesReportMojo plugin;

    File outputDirectory;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    StringWriter writer;

    @Mock
    MavenProject project;

    @Before
    public void setupPlugin() {

        MockitoAnnotations.initMocks(this);

        writer = new StringWriter();

        plugin = new ThucydidesReportMojo() {
            public Sink getSink() {
                return new XhtmlBaseSink(writer);
            }
        };

        when(project.getArtifactId()).thenReturn("test-project");
        when(project.getGroupId()).thenReturn("test-project-group");

        plugin.sourceDirectory = new File((getClass().getClassLoader().getResource("sampleresults")).getPath());
        plugin.project = project;

        outputDirectory = temporaryFolder.newFolder("out");
        plugin.outputDirectory = outputDirectory.getAbsolutePath();

    }

    @Test
    public void the_maven_report_should_generate_a_summary_page() throws Exception {

        plugin.executeReport(Locale.getDefault());
        String htmlReport = writer.toString();

        assertThat(htmlReport, is(notNullValue()));
    }

    @Test
    public void the_maven_report_should_generate_a_link_to_the_thucydides_report_index() throws Exception {

        plugin.executeReport(Locale.getDefault());
        String htmlReport = writer.toString();

        assertThat(htmlReport, containsString("<a href=\"thucydides/index.html\""));
    }

    @Test
    public void the_maven_report_should_generate_a_list_of_features() throws Exception {

        plugin.executeReport(Locale.getDefault());
        String htmlReport = writer.toString();

        assertThat(htmlReport, containsString("Features"));
        assertThat(htmlReport, containsString("<a href=\"thucydides/stories_make_widgets.html\">Make widgets</a>"));
        assertThat(htmlReport, containsString("<a href=\"thucydides/stories_sell_widgets.html\">Sell widgets</a>"));
    }

    @Test
    public void the_maven_report_should_generate_a_list_of_stories() throws Exception {

        plugin.executeReport(Locale.getDefault());
        String htmlReport = writer.toString();

        assertThat(htmlReport, containsString("Stories"));
        assertThat(htmlReport, containsString("<a href=\"thucydides/make_small_widgets.html\">Make small widgets</a>"));
        assertThat(htmlReport, containsString("<a href=\"thucydides/sell_widgets_online.html\">Sell widgets online</a>"));
    }

    @Test
    public void the_maven_report_should_contain_story_coverage() throws Exception {

        plugin.executeReport(Locale.getDefault());
        String htmlReport = writer.toString();

        assertThat(htmlReport, containsString("Step Coverage"));
        assertThat(htmlReport, containsString("50.0%"));
    }

}
