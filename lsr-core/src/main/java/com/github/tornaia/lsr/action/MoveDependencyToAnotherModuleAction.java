package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.model.MavenCoordinates;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MoveDependencyToAnotherModuleAction implements Action {

    private Multimap<Model, Model> parentChildMap;
    private MavenCoordinates from;
    private MavenCoordinates parentTo;
    private MavenCoordinates as;
    private MavenCoordinates what;

    public MoveDependencyToAnotherModuleAction(Multimap<Model, Model> parentChildMap, MavenCoordinates from, MavenCoordinates parentTo, MavenCoordinates as, MavenCoordinates what) {
        this.parentChildMap = parentChildMap;
        this.from = from;
        this.parentTo = parentTo;
        this.as = as;
        this.what = what;
    }

    public void execute() {
        Model fromModel = findModel(parentChildMap, e -> Objects.equals(from.groupId, e.getGroupId()) && Objects.equals(from.artifactId, e.getArtifactId())).orElseThrow(() -> new RuntimeException("From not found!"));
        List<Dependency> fromModelDependencies = fromModel.getDependencies();
        List<Dependency> dependenciesToMove = fromModelDependencies
                .stream()
                .filter(d -> Objects.equals(d.getGroupId(), what.groupId) && Objects.equals(d.getArtifactId(), what.artifactId))
                .collect(Collectors.toList());
        Preconditions.checkState(dependenciesToMove.size() == 1);
        Dependency dependencyToMove = dependenciesToMove.get(0);
        fromModelDependencies.remove(dependencyToMove);

        Model toParentModel = findModel(parentChildMap, e -> Objects.equals(parentTo.artifactId, e.getArtifactId())).orElseThrow(() -> new RuntimeException("ToParent not found!"));
        Model asModel = findModel(parentChildMap, e -> Objects.equals(as.artifactId, e.getArtifactId())).orElseGet(() -> createNewModule(as, toParentModel));

        List<Dependency> newModuleModelDependencies = asModel.getDependencies();
        newModuleModelDependencies.add(dependencyToMove);

        List<String> toParentModules = toParentModel.getModules();
        boolean subModuleAlreadyExists = toParentModules.contains(as.artifactId);
        if (!subModuleAlreadyExists) {
            toParentModules.add(as.artifactId);
        }

        parentChildMap.put(toParentModel, asModel);
    }

    private Model createNewModule(MavenCoordinates as, Model toParentModel) {
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
        return newModuleModel;
    }

    private static Optional<Model> findModel(Multimap<Model, Model> parentChildMap, Predicate<Model> filter) {
        List<Model> matchingModels = parentChildMap.values().stream().filter(filter).collect(Collectors.toList());
        Preconditions.checkState(matchingModels.size() <= 1);
        if (!matchingModels.isEmpty()) {
            return Optional.of(matchingModels.get(0));
        }

        return Optional.empty();
    }
}