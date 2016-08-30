package com.github.tornaia.lsr.util;

import com.github.tornaia.lsr.model.MavenCoordinate;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.maven.model.Model;

import java.util.*;
import java.util.stream.Collectors;

// TODO move these logic to MavenProject
public final class ParentChildMapUtils {

    private static final Model NULL_KEY_HAS_ROOT_POM_AS_VALUE = null;

    private ParentChildMapUtils() {
    }

    public static boolean isMavenCoordinateParentOfTheOther(Multimap<Model, Model> parentChildMap, MavenCoordinate parentCoordinate, MavenCoordinate childCoordinate) {
        Model parentModel = getModel(parentChildMap, parentCoordinate);
        Model childModel = getModel(parentChildMap, childCoordinate);
        Collection<Model> children = parentChildMap.get(parentModel);
        if (children.contains(childModel)) {
            return true;
        }
        for (Model child : children) {
            MavenCoordinate parentsChildCoordinate = new MavenCoordinate(child.getGroupId(), child.getArtifactId(), child.getVersion());
            return isMavenCoordinateParentOfTheOther(parentChildMap, parentsChildCoordinate, childCoordinate);
        }
        return false;
    }

    private static Model getModel(Multimap<Model, Model> parentChildMap, MavenCoordinate mavenCoordinate) {
        Set<Model> models = Sets.newHashSet(parentChildMap.keySet());
        models.addAll(parentChildMap.values());
        models.remove(NULL_KEY_HAS_ROOT_POM_AS_VALUE);

        List<Model> model = models.stream()
                .filter(m -> Objects.equals(mavenCoordinate.groupId, m.getGroupId()) && Objects.equals(mavenCoordinate.artifactId, m.getArtifactId()) && Objects.equals(mavenCoordinate.version, m.getVersion()))
                .collect(Collectors.toList());
        Preconditions.checkState(model.size() == 1);
        return model.get(0);
    }
}
