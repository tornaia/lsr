package com.github.tornaia.lsr;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public abstract class AbstractTest {

    // TODO for some reason the directories these temp directories are not deleted after tests have been finished
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected File createCopy(String classpathDirectory) {
        try {
            File original = new File(Thread.currentThread().getContextClassLoader().getResource(classpathDirectory).toURI());

            File copyRoot = temporaryFolder.getRoot();
            FileUtils.copyDirectory(original, copyRoot);

            return copyRoot;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertPathEqualsRecursively(String expectedClasspathDirectory, File actualRootDirectory) {
        Path expected;
        try {
            expected = new File(Thread.currentThread().getContextClassLoader().getResource(expectedClasspathDirectory).toURI()).toPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        AssertFile.assertPathEqualsRecursively(actualRootDirectory.toPath(), expected);
    }
}
