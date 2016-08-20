package com.github.tornaia.lsr.action;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.tornaia.lsr.model.MavenCoordinates;
import com.github.tornaia.lsr.utils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.objectweb.asm.ClassReader;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.apache.commons.io.FileUtils.moveFile;

public class MoveJavaSourcesToAnAnotherModuleAction implements Action {

    private File rootDirectory;
    private MavenCoordinates from;
    private MavenCoordinates to;
    private MavenCoordinates what;

    public MoveJavaSourcesToAnAnotherModuleAction(File rootDirectory, MavenCoordinates from, MavenCoordinates to, MavenCoordinates what) {
        this.rootDirectory = rootDirectory;
        this.from = from;
        this.to = to;
        this.what = what;
    }

    @Override
    public void execute() {
        List<String> allClasses = getAllClasses(what);
        File fromModuleDirectory = FileUtils.getModuleDirectory(rootDirectory, from);
        File toModuleDirectory = FileUtils.getModuleDirectory(rootDirectory, to);
        List<File> filesToMove = getFileToMoveFromFromDirectory(allClasses, fromModuleDirectory);
        moveFilesToAnotherModule(filesToMove, toModuleDirectory);
    }

    private void moveFilesToAnotherModule(List<File> filesToMove, File toModuleDirectory) {
        for (File fileToMove : filesToMove) {
            String absolutePath = fileToMove.getAbsolutePath();
            String whereToSplit = File.separator + "src" + File.separator + "main" + File.separator + "java";
            String quote = Pattern.quote(whereToSplit);
            String relativePathWithinModule = absolutePath.split(quote)[1];
            File newPlaceOfFile = new File(toModuleDirectory + whereToSplit + relativePathWithinModule);
            try {
                moveFile(fileToMove, newPlaceOfFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> getAllClasses(MavenCoordinates what) {
        String whatCanonical = what.groupId + ":" + what.artifactId + ":" + what.version;
        MavenResolvedArtifact[] mavenResolvedArtifacts = Maven.resolver()
                .resolve(whatCanonical)
                .withTransitivity()
                .asResolvedArtifact();

        List<String> classes = new ArrayList<>();

        for (MavenResolvedArtifact mra : mavenResolvedArtifacts) {
            File jarFile = getJar(mra);
            try (ZipFile zipFile = new ZipFile(jarFile)) {
                try (ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile))) {
                    for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            InputStream inputStream = zipFile.getInputStream(entry);
                            byte[] bytes = IOUtils.toByteArray(inputStream);
                            ClassReader classReader = new ClassReader(bytes);
                            String className = classReader.getClassName();
                            className = className.replaceAll("/", "\\.");
                            classes.add(className);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return classes;
    }

    private static File getJar(MavenResolvedArtifact mra) {
        CloseableHttpClient client = HttpClients.createDefault();
        String groupIdPath = Arrays.asList(mra.getCoordinate().getGroupId().split("\\.")).stream()
                .collect(Collectors.joining("/"));

        String jarUrlToDownload = "http://central.maven.org/maven2/" + groupIdPath + "/" + mra.getCoordinate().getArtifactId() + "/" + mra.getCoordinate().getVersion() + "/" + mra.getCoordinate().getArtifactId() + "-" + mra.getCoordinate().getVersion() + ".jar";
        ByteArrayOutputStream jarOutputStream = new ByteArrayOutputStream();
        try {
            try (CloseableHttpResponse response = client.execute(new HttpGet(jarUrlToDownload))) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    entity.writeTo(jarOutputStream);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        byte[] jarBytes = jarOutputStream.toByteArray();

        File jarFile;
        try {
            jarFile = File.createTempFile("prefix", ".jar");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileOutputStream fos = new FileOutputStream(jarFile)) {
            fos.write(jarBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jarFile;
    }

    private List<File> getFileToMoveFromFromDirectory(List<String> allDirectClasses, File fromModuleDirectory) {
        ArrayList<File> filesToMove = new ArrayList<>();
        File mainJavaDirectory = new File(fromModuleDirectory.getAbsolutePath() + "/src/main/java");
        try {
            Files.walkFileTree(mainJavaDirectory.toPath(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toFile().getName().endsWith(".java")) {
                        CompilationUnit cu;
                        try (FileInputStream in = new FileInputStream(file.toFile())) {
                            try {
                                cu = JavaParser.parse(in);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        List<ImportDeclaration> imports = cu.getImports();
                        for (ImportDeclaration i : imports) {
                            String fullyQualifiedClassName = i.getName().toString();
                            if (allDirectClasses.contains(fullyQualifiedClassName)) {
                                filesToMove.add(file.toFile());
                            }
                        }
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
        return filesToMove;
    }
}
