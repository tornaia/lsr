package com.github.tornaia.lsr.model;

import com.github.tornaia.lsr.util.ParseUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MavenProject {

    public static final String FILENAME_POM_XML = "pom.xml";

    private static final MavenModel NULL_KEY_HAS_ROOT_POM_AS_VALUE = null;

    private final File rootPom;
    private final Map<MavenModel, Set<MavenModel>> parentChildMap;

    public MavenProject(File rootPom) {
        this.rootPom = rootPom;
        this.parentChildMap = Maps.newHashMap();
        exploreRecursively(rootPom);
    }

    private void exploreRecursively(File pom) {
        MavenModel model = ParseUtils.parsePom(pom);
        if (!parentChildMap.containsKey(NULL_KEY_HAS_ROOT_POM_AS_VALUE)) {
            parentChildMap.put(NULL_KEY_HAS_ROOT_POM_AS_VALUE, Sets.newHashSet(model));
        }
        parentChildMap.put(model, Sets.newHashSet());

        List<String> subModules = model.getModules();
        for (String subModelArtifactId : subModules) {
            File moduleFolder = pom.getParentFile();
            File subModuleFolder = new File(moduleFolder.getAbsolutePath() + File.separator + subModelArtifactId);
            File subModulePom = new File(subModuleFolder.getAbsolutePath() + File.separator + FILENAME_POM_XML);
            MavenModel subModule = ParseUtils.parsePom(subModulePom);
            parentChildMap.get(model).add(subModule);
            exploreRecursively(subModulePom);
        }
    }

    // TODO do not expose the inner structure of this
    public Map<MavenModel, Set<MavenModel>> getParentChildMap() {
        return parentChildMap;
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

        Map<MavenModel, Set<MavenModel>> parentChildMap = getParentChildMap();
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
}
