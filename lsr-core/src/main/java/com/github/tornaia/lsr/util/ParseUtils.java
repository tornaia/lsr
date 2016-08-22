package com.github.tornaia.lsr.utils;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.Optional;

public final class ParseUtils {

    private ParseUtils() {
    }

    public static org.apache.maven.model.Model parsePom(File pom) {
        try {
            return parsePom(new FileInputStream(pom));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static org.apache.maven.model.Model parsePom(InputStream is) {
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        try {
            org.apache.maven.model.Model read = mavenreader.read(is);
            return read;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<Dependency> getSelectedDependency(String fileContent, int lineStart, int columnStart, int lineEnd, int columnEnd) {
        
        return Optional.empty();
    }
}
