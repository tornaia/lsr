package com.github.tornaia.lsr.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MavenProject {

    private static final MavenModel NULL_KEY_HAS_ROOT_POM_AS_VALUE = null;

    private final File rootPom;
    private final Map<MavenModel, Set<MavenModel>> parentChildMap;

    public MavenProject(File pom) {
        this.rootPom = getRootPom(pom);
        this.parentChildMap = Maps.newHashMap();

        exploreRecursively(rootPom);
    }

    private static File getRootPom(File pom) {
        File pomDirectory = pom.getParentFile();
        File parentDirectory = pomDirectory.getParentFile();
        File parentPom = new File(parentDirectory + File.separator + MavenModel.FILENAME_POM_XML);
        boolean parentIsAvailable = parentPom.exists();
        if (!parentIsAvailable) {
            return pom;
        }
        return getRootPom(parentPom);
    }

    private void exploreRecursively(File pom) {
        MavenModel model = new MavenModel(pom);
        if (!parentChildMap.containsKey(NULL_KEY_HAS_ROOT_POM_AS_VALUE)) {
            parentChildMap.put(NULL_KEY_HAS_ROOT_POM_AS_VALUE, Sets.newHashSet(model));
        }
        parentChildMap.put(model, Sets.newHashSet());

        List<String> subModules = model.getModules();
        for (String subModelArtifactId : subModules) {
            File moduleFolder = pom.getParentFile();
            File subModuleFolder = new File(moduleFolder.getAbsolutePath() + File.separator + subModelArtifactId);
            File subModulePom = new File(subModuleFolder.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);
            MavenModel subModule = new MavenModel(subModulePom);
            parentChildMap.get(model).add(subModule);
            exploreRecursively(subModulePom);
        }
    }

    public MavenCoordinate getParentTo(MavenCoordinate as) {
        for (Map.Entry<MavenModel, Set<MavenModel>> entry : parentChildMap.entrySet()) {
            for (MavenModel e : entry.getValue()) {
                boolean found = Objects.equals(as.groupId, e.getGroupId()) && Objects.equals(as.artifactId, e.getArtifactId()) && Objects.equals(as.version, e.getVersion());
                if (found) {
                    MavenModel parentModel = entry.getKey();
                    return parentModel == null ? null : new MavenCoordinate(parentModel.getGroupId(), parentModel.getArtifactId(), parentModel.getVersion());
                }
            }
        }
        return null;
    }

    public Optional<MavenModel> getModel(MavenCoordinate mavenCoordinate) {
        return findModel(e -> Objects.equals(mavenCoordinate.groupId, e.getGroupId()) && Objects.equals(mavenCoordinate.artifactId, e.getArtifactId()));
    }

    public File getRootPom() {
        return rootPom;
    }

    public File getRootDirectory() {
        return rootPom.getParentFile();
    }

    public MavenModel getRootModel() {
        Set<MavenModel> rootModel = parentChildMap.get(NULL_KEY_HAS_ROOT_POM_AS_VALUE);
        Preconditions.checkState(rootModel.size() == 1);
        return rootModel.iterator().next();
    }

    public Set<MavenCoordinate> getAllMavenCoordinates() {
        return getAllModels()
                .stream()
                .map(model -> new MavenCoordinate(model.getGroupId(), model.getArtifactId(), model.getVersion()))
                .collect(Collectors.toSet());
    }

    public Set<MavenModel> getAllModels() {
        return parentChildMap.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    private Optional<MavenModel> findModel(Predicate<MavenModel> filter) {
        Set<MavenModel> models = getAllModels();
        List<MavenModel> matchingModels = models.stream().filter(filter).collect(Collectors.toList());
        Preconditions.checkState(matchingModels.size() <= 1);
        if (!matchingModels.isEmpty()) {
            return Optional.of(matchingModels.get(0));
        }

        return Optional.empty();
    }

    public boolean isMavenCoordinateParentOfTheOther(MavenCoordinate parentCoordinate, MavenCoordinate childCoordinate) {
        MavenModel parentModel = getModel(parentCoordinate).orElseThrow(() -> new IllegalArgumentException("parentCoordinate is not found: " + parentCoordinate));
        MavenModel childModel = getModel(childCoordinate).orElseThrow(() -> new IllegalArgumentException("childCoordinate is not found: " + parentCoordinate));

        Set<MavenModel> children = parentChildMap.get(parentModel);
        if (children.contains(childModel)) {
            return true;
        }

        for (MavenModel child : children) {
            MavenCoordinate parentsChildCoordinate = new MavenCoordinate(child.getGroupId(), child.getArtifactId(), child.getVersion());
            return isMavenCoordinateParentOfTheOther(parentsChildCoordinate, childCoordinate);
        }
        return false;
    }

    public File getModuleDirectory(MavenCoordinate mavenCoordinate) {
        AtomicReference<File> fromModuleDirectory = new AtomicReference<>();
        try {
            Files.walkFileTree(getRootDirectory().toPath(), new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Objects.equals(MavenModel.FILENAME_POM_XML, file.toFile().getName())) {
                        MavenModel model = new MavenModel(file.toFile());
                        boolean found = Objects.equals(mavenCoordinate.groupId, model.getGroupId()) && Objects.equals(mavenCoordinate.artifactId, model.getArtifactId());
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

    public void addModule(MavenModel parent, MavenModel child) {
        boolean isRoot = Objects.isNull(parent);
        if (!isRoot) {

            List<String> toParentModules = parent.getModules();
            boolean subModuleAlreadyExists = toParentModules.contains(child.getArtifactId());
            if (!subModuleAlreadyExists) {
                toParentModules.add(child.getArtifactId());
                parentChildMap.put(child, Sets.newHashSet());
            }

            Set<MavenModel> models = parentChildMap.get(parent);
            models.add(child);
        }
    }
}
