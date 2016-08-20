package com.github.tornaia.lsr.utils;

import com.github.tornaia.lsr.model.MavenCoordinates;
import org.apache.maven.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class FileUtils {

    private FileUtils() {
    }

    public static File getModuleDirectory(File rootDirectory, MavenCoordinates mavenCoordinates) {
        AtomicReference<File> fromModuleDirectory = new AtomicReference<>();
        try {
            Files.walkFileTree(rootDirectory.toPath(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Objects.equals("pom.xml", file.toFile().getName())) {
                        Model model = ParseUtils.parsePom(file.toFile());
                        boolean found = Objects.equals(mavenCoordinates.groupId, model.getGroupId()) && Objects.equals(mavenCoordinates.artifactId, model.getArtifactId());
                        if (found) {
                            fromModuleDirectory.set(new File(file.toFile().getParentFile().getAbsolutePath()));
                            return FileVisitResult.TERMINATE;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    throw new RuntimeException(exc);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fromModuleDirectory.get();
    }
}
