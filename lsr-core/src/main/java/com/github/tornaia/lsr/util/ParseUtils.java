package com.github.tornaia.lsr.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class ParseUtils {

    public static final String FILENAME_POM_XML = "pom.xml";

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
            return mavenReader.read(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    public static Multimap<Model, Model> explore(File pom) {
        Multimap<Model, Model> parentChildMap = LinkedHashMultimap.create();

        Model model = ParseUtils.parsePom(pom);
        parentChildMap.put(null, model);

        List<String> subModules = model.getModules();
        for (String subModelArtifactId : subModules) {
            File directory = pom.getParentFile();
            File subModuleFolder = new File(directory.getAbsolutePath() + File.separator + subModelArtifactId);
            File subModulePom = new File(subModuleFolder.getAbsolutePath() + File.separator + FILENAME_POM_XML);
            Multimap<Model, Model> explore = explore(subModulePom);
            Collection<Model> subModels = explore.values();
            parentChildMap.putAll(model, subModels);
        }

        return parentChildMap;
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

    public static File getTopLevelPom(String pomPath) {
        File pom = new File(pomPath);
        File pomDirectory = pom.getParentFile();
        File parentDirectory = pomDirectory.getParentFile();
        File parentPom = new File(parentDirectory + File.separator + FILENAME_POM_XML);
        boolean parentIsAvailable = parentPom.exists();
        if (!parentIsAvailable) {
            return pom;
        }
        return getTopLevelPom(parentPom.getAbsolutePath());
    }
}
