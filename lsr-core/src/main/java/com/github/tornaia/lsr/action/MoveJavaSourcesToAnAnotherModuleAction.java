package com.github.tornaia.lsr.action;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.model.MavenProject;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.shrinkwrap.resolver.api.Resolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
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

class MoveJavaSourcesToAnAnotherModuleAction implements Action {

    private static final String SLASH_SRC_MAIN_JAVA_SLASH = File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;

    private MavenProject mavenProject;
    private MavenCoordinate from;
    private MavenCoordinate to;
    private MavenCoordinate parentTo;
    private MavenCoordinate what;

    MoveJavaSourcesToAnAnotherModuleAction(MavenProject mavenProject, MavenCoordinate from, MavenCoordinate to, MavenCoordinate parentTo, MavenCoordinate what) {
        this.mavenProject = mavenProject;
        this.from = from;
        this.to = to;
        this.parentTo = parentTo;
        this.what = what;
    }

    @Override
    public void execute() {
        boolean dependencyIsInherited = mavenProject.isMavenCoordinateParentOfTheOther(to, from);
        if (dependencyIsInherited) {
            return;
        }
        List<String> allClasses = getAllClasses(what);
        File fromModuleDirectory = mavenProject.getModuleDirectory(from);
        File toModuleDirectory = mavenProject.getModuleDirectory(to);
        List<File> filesToMove = getFileToMoveFromFromDirectory(allClasses, fromModuleDirectory);
        moveFilesToAnotherModule(filesToMove, toModuleDirectory);
    }

    private void moveFilesToAnotherModule(List<File> filesToMove, File toModuleDirectory) {
        if (!filesToMove.isEmpty()) {
            new File(toModuleDirectory.getAbsolutePath() + SLASH_SRC_MAIN_JAVA_SLASH).mkdirs();
        }

        for (File fileToMove : filesToMove) {
            String absolutePath = fileToMove.getAbsolutePath();
            String quote = Pattern.quote(SLASH_SRC_MAIN_JAVA_SLASH);
            String relativePathWithinModule = absolutePath.split(quote)[1];
            File newPlaceOfFile = new File(toModuleDirectory + SLASH_SRC_MAIN_JAVA_SLASH + relativePathWithinModule);
            try {
                moveFile(fileToMove, newPlaceOfFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> getAllClasses(MavenCoordinate what) {
        String whatCanonical = what.groupId + ":" + what.artifactId + ":" + what.version;

        ClassLoader pluginClassLoader = getClass().getClassLoader();
        MavenResolverSystem mavenResolverSystem = Resolvers.use(MavenResolverSystem.class, pluginClassLoader);
        MavenStrategyStage mavenStrategyStage = mavenResolverSystem.resolve(whatCanonical);
        MavenFormatStage mavenFormatStage = mavenStrategyStage.withTransitivity();
        MavenResolvedArtifact[] mavenResolvedArtifacts = mavenFormatStage.asResolvedArtifact();

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
        ByteArrayOutputStream jarOutputStream;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String groupIdPath = Arrays.asList(mra.getCoordinate().getGroupId().split("\\.")).stream()
                    .collect(Collectors.joining("/"));

            String jarUrlToDownload = "http://central.maven.org/maven2/" + groupIdPath + "/" + mra.getCoordinate().getArtifactId() + "/" + mra.getCoordinate().getVersion() + "/" + mra.getCoordinate().getArtifactId() + "-" + mra.getCoordinate().getVersion() + ".jar";
            jarOutputStream = new ByteArrayOutputStream();
            try (CloseableHttpResponse response = client.execute(new HttpGet(jarUrlToDownload))) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    entity.writeTo(jarOutputStream);
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
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
        if (!mainJavaDirectory.exists()) {
            return Lists.newArrayList();
        }
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
