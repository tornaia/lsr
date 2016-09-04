package com.github.tornaia.lsr.plugin.idea;

import com.github.tornaia.lsr.matcher.DependencyMatcher;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.textarea.TextComponentEditorImpl;
import com.intellij.openapi.project.Project;
import org.apache.maven.model.Dependency;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.assertThat;

public class PomEditorTest extends AbstractPluginIdeaTest {

    private final static String SAMPLE_CONTENT =
            "<dependency>" + NEW_LINE +
                    "  <groupId>org.apache.httpcomponents</groupId>" + NEW_LINE +
                    "  <artifactId>httpclient</artifactId>" + NEW_LINE +
                    "  <version>3.4</version>" + NEW_LINE +
                    "</dependency>";

    private JTextArea jTextArea;
    private PomEditor pomEditor;

    @Before
    public void setUp() {
        Project nullProject = null;
        jTextArea = new JTextArea(SAMPLE_CONTENT);

        Editor editor = new TextComponentEditorImpl(nullProject, jTextArea);
        pomEditor = new PomEditor(editor);
    }

    @Test
    public void rightClickInTheMiddle() {
        jTextArea.select(60, 60);

        Dependency dependency = pomEditor.getSelectedDependency().orElseThrow(() -> new IllegalStateException("Dependency selected but not found"));
        assertThat(dependency, new DependencyMatcher().groupId("org.apache.httpcomponents").artifactId("httpclient").version("3.4"));
    }

    @Test
    public void selectOneLine() {
        jTextArea.select(10, 12);

        Dependency dependency = pomEditor.getSelectedDependency().orElseThrow(() -> new IllegalStateException("Dependency selected but not found"));
        assertThat(dependency, new DependencyMatcher().groupId("org.apache.httpcomponents").artifactId("httpclient").version("3.4"));
    }

    @Test
    public void selectionTwoLines() {
        jTextArea.select(10, 60);

        Dependency dependency = pomEditor.getSelectedDependency().orElseThrow(() -> new IllegalStateException("Dependency selected but not found"));
        assertThat(dependency, new DependencyMatcher().groupId("org.apache.httpcomponents").artifactId("httpclient").version("3.4"));
    }
}
