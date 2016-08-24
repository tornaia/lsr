package com.github.tornaia.lsr.util;

import com.github.tornaia.lsr.model.MavenCoordinates;
import com.google.common.collect.Multimap;
import org.apache.maven.model.Model;

import java.util.Map;
import java.util.Objects;

// TODO wrap Multimap<Model, Model> and move these logic there
public final class ParentChildMapUtils {

    private ParentChildMapUtils() {
    }

    public static MavenCoordinates getParentTo(Multimap<Model, Model> parentChildMap, MavenCoordinates as) {
        for (Map.Entry<Model, Model> entry : parentChildMap.entries()) {
            boolean found = Objects.equals(as.groupId, entry.getValue().getGroupId()) && Objects.equals(as.artifactId, entry.getValue().getArtifactId()) && Objects.equals(as.version, entry.getValue().getVersion());
            if (found) {
                Model parentModel = entry.getKey();
                return parentModel == null ? null : new MavenCoordinates(parentModel.getGroupId(), parentModel.getArtifactId(), parentModel.getVersion());
            }
        }
        return null;
    }
}
