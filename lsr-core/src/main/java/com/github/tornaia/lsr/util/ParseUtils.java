package com.github.tornaia.lsr.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Optional;

public final class ParseUtils {

    private ParseUtils() {
    }

    public static Model parsePom(File pom) {
        try {
            return parsePom(new FileInputStream(pom));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Model parsePom(InputStream is) {
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        try {
            Model read = mavenReader.read(is);
            return read;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<Dependency> getSelectedDependency(String fileContent, int lineStart, int lineEnd) {
        int subStringStartPos = StringUtils.ordinalIndexOf(fileContent, "\n", lineStart);
        subStringStartPos = subStringStartPos == -1 ? 0 : subStringStartPos;
        int subStringEndPos = StringUtils.ordinalIndexOf(fileContent, "\n", lineEnd + 1);
        while (true) {
            String substring = fileContent.substring(subStringStartPos, subStringEndPos);
            boolean startsWithDependencyTag = substring.startsWith("<dependency>");
            boolean endsWithDependencyTag = substring.endsWith("</dependency>");
            if (startsWithDependencyTag && endsWithDependencyTag) {
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(Model.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    StringReader reader = new StringReader(substring);
                    JAXBElement<Dependency> root = unmarshaller.unmarshal(new StreamSource(reader), Dependency.class);
                    Dependency dependency = root.getValue();
                    return Optional.of(dependency);
                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
            }
            boolean startPosModified = false;
            if (!startsWithDependencyTag) {
                if (subStringStartPos > 0) {
                    subStringStartPos--;
                    startPosModified = true;
                }
            }

            boolean endPosModified = false;
            if (!endsWithDependencyTag) {
                if (subStringEndPos < fileContent.length()) {
                    subStringEndPos++;
                    endPosModified = true;
                }
            }

            if (!startPosModified && !endPosModified) {
                return Optional.empty();
            }
        }
    }
}
