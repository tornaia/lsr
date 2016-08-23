package com.github.tornaia.lsr.model;

import java.util.Objects;

public class MavenCoordinates {

    public final String groupId;
    public final String artifactId;
    public final String version;

    public MavenCoordinates(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = Objects.equals("xerces-impl", artifactId) ? "xercesImpl" : artifactId;
        this.version = Objects.equals("xml-apis", groupId) && Objects.equals("xml-apis", artifactId) && Objects.equals("2.6.2", version) ? "2.0.2" : version;
        if (version.equals("2.6.2")) {
            return;
        }
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
