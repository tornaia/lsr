package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.exception.IllegalMavenStateException;
import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.model.MavenModel;
import com.github.tornaia.lsr.model.MavenProject;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class MoveDependencyToAnotherModuleAction implements Action {

    private MavenProject mavenProject;
    private MavenCoordinate from;
    private MavenCoordinate parentTo;
    private MavenCoordinate as;
    private MavenCoordinate what;

    MoveDependencyToAnotherModuleAction(MavenProject mavenProject, MavenCoordinate from, MavenCoordinate as, MavenCoordinate parentTo, MavenCoordinate what) {
        this.mavenProject = mavenProject;
        this.from = from;
        this.parentTo = parentTo;
        this.as = as;
        this.what = what;
    }

    public void execute() {
        MavenModel fromModel = mavenProject.getModel(from).orElseThrow(() -> new RuntimeException("From not found!"));
        Map<String, String> properties = getProperties(mavenProject, fromModel);

        List<Dependency> fromModelDependencies = fromModel.getDependencies();
        List<Dependency> dependenciesToMove = fromModelDependencies
                .stream()
                .filter(d -> Objects.equals(resolveTokens(properties, d.getGroupId()), what.groupId) && Objects.equals(resolveTokens(properties, d.getArtifactId()), what.artifactId))
                .collect(Collectors.toList());
        Preconditions.checkState(dependenciesToMove.size() == 1, "Cannot find dependency to move: " + what);
        Dependency dependencyToMove = dependenciesToMove.get(0);
        fromModelDependencies.remove(dependencyToMove);

        MavenModel toParentModel = parentTo != null ? mavenProject.getModel(parentTo).orElseThrow(() -> new RuntimeException("ToParent not found!")) : null;
        MavenModel asModel = mavenProject.getModel(as).orElseGet(() -> createNewModule(as, toParentModel));

        List<Dependency> newModuleModelDependencies = asModel.getDependencies();


        Optional<Dependency> dependencyWithDifferentVersionMightBeThere = newModuleModelDependencies.stream()
                .filter(d -> Objects.equals(resolveTokens(properties, d.getGroupId()), dependencyToMove.getGroupId()) && Objects.equals(resolveTokens(properties, d.getArtifactId()), dependencyToMove.getArtifactId()) && !Objects.equals(resolveTokens(properties, d.getVersion()), dependencyToMove.getVersion()))
                .findFirst();
        if (dependencyWithDifferentVersionMightBeThere.isPresent()) {
            Dependency whatWithDifferentVersion = dependencyWithDifferentVersionMightBeThere.get();
            throw new IllegalMavenStateException("Conflict! " + from.toString() + " has dependency " + what.toString() + " but " + as.toString() + " already has dependency " + (whatWithDifferentVersion.getGroupId() + ":" + whatWithDifferentVersion.getArtifactId() + ":" + whatWithDifferentVersion.getVersion()));
        }


        Optional<Dependency> dependencyMightBeThere = newModuleModelDependencies.stream()
                .filter(d -> Objects.equals(resolveTokens(properties, d.getGroupId()), dependencyToMove.getGroupId()) && Objects.equals(resolveTokens(properties, d.getArtifactId()), dependencyToMove.getArtifactId()) && Objects.equals(resolveTokens(properties, d.getVersion()), dependencyToMove.getVersion()))
                .findFirst();
        if (!dependencyMightBeThere.isPresent()) {
            Dependency fullyResolvedDependencyToMove = new Dependency();
            fullyResolvedDependencyToMove.setGroupId(what.groupId);
            fullyResolvedDependencyToMove.setArtifactId(what.artifactId);
            fullyResolvedDependencyToMove.setVersion(what.version);
            newModuleModelDependencies.add(fullyResolvedDependencyToMove);
        }


        mavenProject.addModule(toParentModel, asModel);
    }

    // TODO move the a separate class
    private Map<String, String> getProperties(MavenProject mavenProject, MavenModel fromModel) {
        Map<String, String> properties = Maps.newHashMap();
        // TODO Visitor pattern or something like Files.walkFileTree
        Optional<MavenModel> parentMavenModel = mavenProject.getParentOf(fromModel);
        if (parentMavenModel.isPresent()) {
            Map<String, String> parentProperties = getProperties(mavenProject, parentMavenModel.get());
            properties.putAll(parentProperties);
        }

        properties.putAll(new HashMap<>((Map)fromModel.getProperties()));
        return properties;
    }

    private static String resolveTokens(Map<String, String> tokens, String template) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(template);
        // StringBuilder cannot be used here because Matcher expects StringBuffer
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            if (tokens.containsKey(matcher.group(1))) {
                String replacement = tokens.get(matcher.group(1));
                // quote to work properly with $ and {,} signs
                matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "null");
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static MavenModel createNewModule(MavenCoordinate as, MavenModel toParentModel) {
        if (Objects.isNull(toParentModel)) {
            throw new IllegalArgumentException("Cannot create new module where parent is null!");
        }
        Model newModuleModel = new Model();
        newModuleModel.setModelVersion("4.0.0");
        Parent newModuleParent = new Parent();
        newModuleParent.setGroupId(toParentModel.getGroupId());
        newModuleParent.setArtifactId(toParentModel.getArtifactId());
        newModuleParent.setVersion(toParentModel.getVersion());
        newModuleModel.setParent(newModuleParent);
        newModuleModel.setGroupId(as.groupId);
        newModuleModel.setArtifactId(as.artifactId);
        newModuleModel.setVersion(as.version);
        return new MavenModel(newModuleModel);
    }
}
