package com.github.tornaia.lsr.model;

import com.github.tornaia.lsr.util.ParseUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.maven.model.Model;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MavenProject {

    public static final String FILENAME_POM_XML = "pom.xml";

    private static final Model NULL_KEY_HAS_ROOT_POM_AS_VALUE = null;

    private final File rootPom;
    private final Multimap<Model, Model> parentChildMap;

    public MavenProject(File rootPom) {
        this.rootPom = rootPom;
        this.parentChildMap = exploreRecursively(rootPom);
    }

    // TODO do not expose the inner structure of this
    public Multimap<Model, Model> getParentChildMap() {
        return parentChildMap;
    }

    private static Multimap<Model, Model> exploreRecursively(File pom) {
        Multimap<Model, Model> parentChildMap = LinkedHashMultimap.create();

        Model model = ParseUtils.parsePom(pom);
        parentChildMap.put(null, model);

        List<String> subModules = model.getModules();
        for (String subModelArtifactId : subModules) {
            File directory = pom.getParentFile();
            File subModuleFolder = new File(directory.getAbsolutePath() + File.separator + subModelArtifactId);
            File subModulePom = new File(subModuleFolder.getAbsolutePath() + File.separator + FILENAME_POM_XML);
            Multimap<Model, Model> explore = exploreRecursively(subModulePom);
            Collection<Model> subModels = explore.values();
            parentChildMap.putAll(model, subModels);
        }

        return parentChildMap;
    }

    public MavenCoordinate getParentTo(MavenCoordinate as) {
        for (Map.Entry<Model, Model> entry : parentChildMap.entries()) {
            boolean found = Objects.equals(as.groupId, entry.getValue().getGroupId()) && Objects.equals(as.artifactId, entry.getValue().getArtifactId()) && Objects.equals(as.version, entry.getValue().getVersion());
            if (found) {
                Model parentModel = entry.getKey();
                return parentModel == null ? null : new MavenCoordinate(parentModel.getGroupId(), parentModel.getArtifactId(), parentModel.getVersion());
            }
        }
        return null;
    }

    public Optional<Model> getModel(MavenCoordinate mavenCoordinate) {
        return findModel(e -> Objects.equals(mavenCoordinate.groupId, e.getGroupId()) && Objects.equals(mavenCoordinate.artifactId, e.getArtifactId()));
    }

    private Optional<Model> findModel(Predicate<Model> filter) {
        List<Model> matchingModels = parentChildMap.values().stream().filter(filter).collect(Collectors.toList());
        Preconditions.checkState(matchingModels.size() <= 1);
        if (!matchingModels.isEmpty()) {
            return Optional.of(matchingModels.get(0));
        }

        return Optional.empty();
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
        return parentChildMap.values()
                .stream()
                .map(model -> new MavenCoordinate(model.getGroupId(), model.getArtifactId(), model.getVersion()))
                .collect(Collectors.toSet());
    }
}
