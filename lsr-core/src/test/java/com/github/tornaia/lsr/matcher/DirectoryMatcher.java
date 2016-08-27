package com.github.tornaia.lsr.matcher;

import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Assertion for recursively testing directories.
 */
public class DirectoryMatcher {

    private DirectoryMatcher() {
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
    public static void assertPathEqualsRecursively(final Path expected, final Path actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        final Path absoluteExpected = expected.toAbsolutePath();
        final Path absoluteActual = actual.toAbsolutePath();
        try {
            Files.walkFileTree(expected, new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path actualDir, BasicFileAttributes attrs) throws IOException {
                    Path relativeExpectedDir = absoluteExpected.relativize(actualDir.toAbsolutePath());
                    Path expectedDir = absoluteActual.resolve(relativeExpectedDir);

                    if (!Files.exists(expectedDir)) {
                        fail(String.format("Directory not found. Expected \'%s\', actual \'%s\'.", expectedDir.toFile().getAbsolutePath(), actualDir.toFile().getAbsolutePath()));
                    }

                    String expectedAbsPath = expectedDir.toFile().getAbsolutePath();
                    String actualAbsPath = absoluteExpected.resolve(relativeExpectedDir).toFile().getAbsolutePath();
                    String errorMessage = String.format("Directory size mismatch. Expected \'%s\', actual \'%s\'", expectedDir, actualAbsPath);
                    assertEquals(errorMessage, new File(expectedAbsPath).list().length, new File(actualAbsPath).list().length);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path expectedFile, BasicFileAttributes attrs) throws IOException {
                    Path relativeExpectedFile = absoluteExpected.relativize(expectedFile.toAbsolutePath());
                    Path actualFile = absoluteActual.resolve(relativeExpectedFile);

                    if (!Files.exists(actualFile)) {
                        fail(String.format("File \'%s\' missing in target.", expectedFile.getFileName()));
                    }

                    String expectedFileAbsolutePath = expectedFile.toFile().getAbsolutePath();
                    String actualFileAbsolutePath = actualFile.toFile().getAbsolutePath();
                    byte[] expectedFileContent = filterCarriageReturn(Files.readAllBytes(expectedFile));
                    byte[] actualFileContent = filterCarriageReturn(Files.readAllBytes(actualFile));
                    long expectedFileSize = expectedFileContent.length;
                    long actualFileSize = actualFileContent.length;

                    assertEquals(String.format("File size of \'%s\' and \'%s\' differ. ", expectedFileAbsolutePath, actualFileAbsolutePath), expectedFileSize, actualFileSize);
                    assertArrayEquals(String.format("File content of \'%s\' and \'%s\' differ. ", expectedFileAbsolutePath, actualFileAbsolutePath), expectedFileContent, actualFileContent);
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
            fail(e.getMessage());
        }
    }

    private static byte[] filterCarriageReturn(byte[] bytes) {
        List<Byte> expectedFileContentAsByteList = Lists.newArrayList(ArrayUtils.toObject(bytes));
        List<Byte> filteredFileContentAsByteList = expectedFileContentAsByteList.stream().filter(b -> b != 13).collect(Collectors.toList());
        return ArrayUtils.toPrimitive(filteredFileContentAsByteList.toArray(new Byte[0]));
    }
}