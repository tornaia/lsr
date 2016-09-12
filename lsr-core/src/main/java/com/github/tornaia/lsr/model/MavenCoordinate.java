package com.github.tornaia.lsr.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

// TODO what about org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinateImpl
public class MavenCoordinate {

    public final String groupId;
    public final String artifactId;
    public final String version;

    public MavenCoordinate(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = Objects.equals("xerces-impl", artifactId) ? "xercesImpl" : artifactId;
        this.version = Objects.equals("xml-apis", groupId) && Objects.equals("xml-apis", artifactId) && Objects.equals("2.6.2", version) ? "2.0.2" : version;
        if (version.equals("2.6.2")) {
            return;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(groupId).append(artifactId).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MavenCoordinate == false) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        MavenCoordinate otherObject = (MavenCoordinate) obj;

        return new EqualsBuilder()
                .append(this.groupId, otherObject.groupId)
                .append(this.artifactId, otherObject.artifactId)
                .append(this.groupId, otherObject.groupId)
                .isEquals();
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
