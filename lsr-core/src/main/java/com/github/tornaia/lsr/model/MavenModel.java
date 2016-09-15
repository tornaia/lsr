package com.github.tornaia.lsr.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.List;
import java.util.Properties;

public class MavenModel {

    public static final String FILENAME_POM_XML = "pom.xml";

    private final Model model;

    public MavenModel(File pom) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(pom);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.model = parsePom(fileInputStream);
    }

    public MavenModel(Model model) {
        this.model = model;
    }

    private static Model parsePom(InputStream is) {
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            return mavenReader.read(is);
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
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

    // TODO maybe it should return Map<String, String>
    public Properties getProperties() {
        return model.getProperties();
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
