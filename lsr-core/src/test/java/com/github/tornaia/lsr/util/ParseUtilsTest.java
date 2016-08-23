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
    public void selectionOneLine() {
        Optional<Dependency> selectedDependency = ParseUtils.getSelectedDependency(SAMPLE_CONTENT, 2, 2);
        Dependency dependency = selectedDependency.get();

        assertThat(dependency, new DependencyMatcher().groupId("org.apache.httpcomponents").artifactId("httpclient").version("3.4"));
    }

    @Test
    public void selectionTwoLines() {
        Optional<Dependency> selectedDependency = ParseUtils.getSelectedDependency(SAMPLE_CONTENT, 2, 3);
        Dependency dependency = selectedDependency.get();

        assertThat(dependency, new DependencyMatcher().groupId("org.apache.httpcomponents").artifactId("httpclient").version("3.4"));
    }

    @Test
    public void selectFirstLine() {
        Optional<Dependency> selectedDependency = ParseUtils.getSelectedDependency(SAMPLE_CONTENT, 0, 0);
        Dependency dependency = selectedDependency.get();

        assertThat(dependency, new DependencyMatcher().groupId("org.apache.httpcomponents").artifactId("httpclient").version("3.4"));
    }
}
