package com.github.tornaia.lsr;

import org.junit.Assert;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Assertion for recursively testing directories.
 */
public class AssertFile {

    private AssertFile() {
    }

    /**
     * Asserts that two directories are recursively equal. If they are not, an {@link AssertionError} is thrown with the
     * given message.<br/>
     * There will be a binary comparison of all files under expected with all files under actual. File attributes will
     * not be considered.<br/>
     * Missing or additional files are considered an error.<br/>
     *
     * @param expected Path expected directory
     * @param actual   Path actual directory
     */
    public static final void assertPathEqualsRecursively(final Path expected, final Path actual) {
        Assert.assertNotNull(expected);
        Assert.assertNotNull(actual);
        final Path absoluteExpected = expected.toAbsolutePath();
        final Path absoluteActual = actual.toAbsolutePath();
        try {
            Files.walkFileTree(expected, new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path expectedDir, BasicFileAttributes attrs)
                        throws IOException {
                    Path relativeExpectedDir = absoluteExpected.relativize(expectedDir.toAbsolutePath());
                    Path actualDir = absoluteActual.resolve(relativeExpectedDir);

                    if (!Files.exists(actualDir)) {
                        Assert.fail(String.format("Directory \'%s\' missing in target.", expectedDir.getFileName()));
                    }

                    Assert.assertEquals(String.format("Directory size of \'%s\' differ. ", relativeExpectedDir),
                            expectedDir.toFile().list().length, actualDir.toFile().list().length);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path expectedFile, BasicFileAttributes attrs) throws IOException {
                    Path relativeExpectedFile = absoluteExpected.relativize(expectedFile.toAbsolutePath());
                    Path actualFile = absoluteActual.resolve(relativeExpectedFile);

                    if (!Files.exists(actualFile)) {
                        Assert.fail(String.format("File \'%s\' missing in target.", expectedFile.getFileName()));
                    }
                    Assert.assertEquals(String.format("File size of \'%s\' differ. ", relativeExpectedFile),
                            Files.size(expectedFile), Files.size(actualFile));
                    Assert.assertArrayEquals(String.format("File content of \'%s\' differ. ", relativeExpectedFile),
                            Files.readAllBytes(expectedFile), Files.readAllBytes(actualFile));

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    Assert.fail(exc.getMessage());
                    return FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}