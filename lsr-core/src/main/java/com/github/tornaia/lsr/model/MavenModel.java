package com.github.tornaia.lsr.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import java.util.List;

public class MavenModel {

    private final Model model;

    public MavenModel(Model model) {
        this.model = model;
    }

    public String getGroupId() {
        return model.getGroupId();
    }

    public String getArtifactId() {
        return model.getArtifactId();
    }

    public String getVersion() {
        return model.getVersion();
    }

    public List<String> getModules() {
        return model.getModules();
    }

    public List<Dependency> getDependencies() {
        return model.getDependencies();
    }

    public Model getModel() {
        return model;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(model.getGroupId()).append(model.getGroupId()).append(model.getVersion()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        MavenModel rhs = (MavenModel) obj;
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(model.getGroupId(), rhs.model.getGroupId());
        equalsBuilder.append(model.getArtifactId(), rhs.model.getArtifactId());
        equalsBuilder.append(model.getVersion(), rhs.model.getVersion());
        return equalsBuilder.isEquals();
    }

    @Override
    public String toString() {
        return model.toString();
    }
}
