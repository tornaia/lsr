package com.github.tornaia.lsr;

import com.github.tornaia.lsr.matcher.DirectoryMatcher;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.fail;

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
        deleteTargetFoldersRecursively(actualRootDirectory);
        Path expected;
        try {
            expected = new File(Thread.currentThread().getContextClassLoader().getResource(expectedClasspathDirectory).toURI()).toPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        DirectoryMatcher.assertPathEqualsRecursively(actualRootDirectory.toPath(), expected);
    }

    private static void deleteTargetFoldersRecursively(File dir) {
        try {
            Files.walkFileTree(dir.toPath(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path actualDir, BasicFileAttributes attrs) throws IOException {
                    File target = actualDir.resolve("target").toFile();
                    FileUtils.deleteDirectory(target);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path expectedFile, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    fail(exc.getMessage());
                    return FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
