package com.github.tornaia.lsr.model;

import com.github.tornaia.lsr.util.ParseUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.maven.model.Model;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MavenProject {

    public static final String FILENAME_POM_XML = "pom.xml";

    private static final Model NULL_KEY_HAS_ROOT_POM_AS_VALUE = null;

    private final File rootPom;
    private final Map<Model, Set<Model>> parentChildMap;

    public MavenProject(File rootPom) {
        this.rootPom = rootPom;
        this.parentChildMap = Maps.newHashMap();
        exploreRecursively(rootPom);
    }

    private void exploreRecursively(File pom) {
        Model model = ParseUtils.parsePom(pom);
        if (!parentChildMap.containsKey(NULL_KEY_HAS_ROOT_POM_AS_VALUE)) {
            parentChildMap.put(NULL_KEY_HAS_ROOT_POM_AS_VALUE, Sets.newHashSet(model));
        }
        parentChildMap.put(model, Sets.newHashSet());

        List<String> subModules = model.getModules();
        for (String subModelArtifactId : subModules) {
            File moduleFolder = pom.getParentFile();
            File subModuleFolder = new File(moduleFolder.getAbsolutePath() + File.separator + subModelArtifactId);
            File subModulePom = new File(subModuleFolder.getAbsolutePath() + File.separator + FILENAME_POM_XML);
            Model subModule = ParseUtils.parsePom(subModulePom);
            parentChildMap.get(model).add(subModule);
            exploreRecursively(subModulePom);
        }
    }

    // TODO do not expose the inner structure of this
    public Map<Model, Set<Model>> getParentChildMap() {
        return parentChildMap;
    }

    public MavenCoordinate getParentTo(MavenCoordinate as) {
        for (Map.Entry<Model, Set<Model>> entry : parentChildMap.entrySet()) {
            for (Model e : entry.getValue()) {
                boolean found = Objects.equals(as.groupId, e.getGroupId()) && Objects.equals(as.artifactId, e.getArtifactId()) && Objects.equals(as.version, e.getVersion());
                if (found) {
                    Model parentModel = entry.getKey();
                    return parentModel == null ? null : new MavenCoordinate(parentModel.getGroupId(), parentModel.getArtifactId(), parentModel.getVersion());
                }
            }
        }
        return null;
    }

    public Optional<Model> getModel(MavenCoordinate mavenCoordinate) {
        return findModel(e -> Objects.equals(mavenCoordinate.groupId, e.getGroupId()) && Objects.equals(mavenCoordinate.artifactId, e.getArtifactId()));
    }

    public File getRootPom() {
        return rootPom;
    }

    public File getRootDirectory() {
        return rootPom.getParentFile();
    }

    public Model getRootModel() {
        Collection<Model> rootModel = parentChildMap.get(NULL_KEY_HAS_ROOT_POM_AS_VALUE);
        Preconditions.checkState(rootModel.size() == 1);
        return rootModel.iterator().next();
    }

    public Set<MavenCoordinate> getAllMavenCoordinates() {
        return getAllModels()
                .stream()
                .map(model -> new MavenCoordinate(model.getGroupId(), model.getArtifactId(), model.getVersion()))
                .collect(Collectors.toSet());
    }

    public Set<Model> getAllModels() {
        return parentChildMap.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    private Optional<Model> findModel(Predicate<Model> filter) {
        Set<Model> models = getAllModels();
        List<Model> matchingModels = models.stream().filter(filter).collect(Collectors.toList());
        Preconditions.checkState(matchingModels.size() <= 1);
        if (!matchingModels.isEmpty()) {
            return Optional.of(matchingModels.get(0));
        }

        return Optional.empty();
    }
}
