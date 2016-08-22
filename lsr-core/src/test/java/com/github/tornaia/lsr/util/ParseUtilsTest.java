package com.github.tornaia.lsr.util;

import com.github.tornaia.lsr.matcher.DependencyMatcher;
import org.apache.maven.model.Dependency;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertThat;

public class ParseUtilsTest {

    private final static String SAMPLE_CONTENT =
            "<dependency>\n" +
                    "  <groupId>org.apache.httpcomponents</groupId>\n" +
                    "  <artifactId>httpclient</artifactId>\n" +
                    "  <version>3.4</version>\n" +
                    "</dependency>";

    @Test
    public void selectionIsWithinArtifactId() {
        Optional<Dependency> selectedDependency = ParseUtils.getSelectedDependency(SAMPLE_CONTENT, 2, 15, 2, 17);
        Dependency dependency = selectedDependency.get();

        assertThat(dependency, new DependencyMatcher().groupId("org.apache.httpcomponents").artifactId("httpclient").version("3.4"));
    }

    @Test
    public void selectionIsFromArtifactIdToVersion() {
        Optional<Dependency> selectedDependency = ParseUtils.getSelectedDependency(SAMPLE_CONTENT, 2, 15, 3, 12);
        Dependency dependency = selectedDependency.get();

        assertThat(dependency, new DependencyMatcher().groupId("org.apache.httpcomponents").artifactId("httpclient").version("3.4"));
    }

    @Test
    public void selectFirstChar() {
        Optional<Dependency> selectedDependency = ParseUtils.getSelectedDependency(SAMPLE_CONTENT, 0, 0, 0, 1);
        Dependency dependency = selectedDependency.get();

        assertThat(dependency, new DependencyMatcher().groupId("org.apache.httpcomponents").artifactId("httpclient").version("3.4"));
    }
}
