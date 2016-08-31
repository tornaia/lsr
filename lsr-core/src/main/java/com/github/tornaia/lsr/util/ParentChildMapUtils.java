package com.github.tornaia.lsr.util;

import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.model.MavenProject;
import com.google.common.base.Preconditions;
import org.apache.maven.model.Model;

import java.util.*;
import java.util.stream.Collectors;

// TODO move these logic to MavenProject
public final class ParentChildMapUtils {

    private ParentChildMapUtils() {
    }

    public static boolean isMavenCoordinateParentOfTheOther(MavenProject mavenProject, MavenCoordinate parentCoordinate, MavenCoordinate childCoordinate) {
        Model parentModel = getModel(mavenProject, parentCoordinate);
        Model childModel = getModel(mavenProject, childCoordinate);
        Map<Model, Set<Model>> parentChildMap = mavenProject.getParentChildMap();
        Collection<Model> children = parentChildMap.get(parentModel);
        if (children.contains(childModel)) {
            return true;
        }
        for (Model child : children) {
            MavenCoordinate parentsChildCoordinate = new MavenCoordinate(child.getGroupId(), child.getArtifactId(), child.getVersion());
            return isMavenCoordinateParentOfTheOther(mavenProject, parentsChildCoordinate, childCoordinate);
        }
        return false;
    }

    private static Model getModel(MavenProject mavenProject, MavenCoordinate mavenCoordinate) {
        Set<Model> models = mavenProject.getAllModels();
        List<Model> model = models.stream()
                .filter(m -> Objects.equals(mavenCoordinate.groupId, m.getGroupId()) && Objects.equals(mavenCoordinate.artifactId, m.getArtifactId()) && Objects.equals(mavenCoordinate.version, m.getVersion()))
                .collect(Collectors.toList());
        Preconditions.checkState(model.size() == 1);
        return model.get(0);
    }
}
