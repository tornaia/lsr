package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.model.MavenCoordinate;
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
    private MavenCoordinate from;
    private MavenCoordinate parentTo;
    private MavenCoordinate as;
    private MavenCoordinate what;

    public MoveDependencyToAnotherModuleAction(Multimap<Model, Model> parentChildMap, MavenCoordinate from, MavenCoordinate as, MavenCoordinate parentTo, MavenCoordinate what) {
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
        Preconditions.checkState(dependenciesToMove.size() == 1, "Cannot find dependency to move: " + what);
        Dependency dependencyToMove = dependenciesToMove.get(0);
        fromModelDependencies.remove(dependencyToMove);

        Model toParentModel = parentTo != null ? findModel(parentChildMap, e -> Objects.equals(parentTo.artifactId, e.getArtifactId())).orElseThrow(() -> new RuntimeException("ToParent not found!")) : null;
        Model asModel = findModel(parentChildMap, e -> Objects.equals(as.artifactId, e.getArtifactId())).orElseGet(() -> createNewModule(as, toParentModel));

        List<Dependency> newModuleModelDependencies = asModel.getDependencies();
        newModuleModelDependencies.add(dependencyToMove);

        boolean hasParent = !Objects.isNull(toParentModel);
        if (hasParent) {
            List<String> toParentModules = toParentModel.getModules();
            boolean subModuleAlreadyExists = toParentModules.contains(as.artifactId);
            if (!subModuleAlreadyExists) {
                toParentModules.add(as.artifactId);
            }
            parentChildMap.put(toParentModel, asModel);
        }
    }

    private Model createNewModule(MavenCoordinate as, Model toParentModel) {
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
