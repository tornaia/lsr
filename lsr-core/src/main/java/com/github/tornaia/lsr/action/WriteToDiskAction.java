package com.github.tornaia.lsr.action;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WriteToDiskAction implements Action {

    private File rootFolder;
    private Multimap<Model, Model> parentChildMap;

    public WriteToDiskAction(File rootFolder, Multimap<Model, Model> parentChildMap) {
        this.rootFolder = rootFolder;
        this.parentChildMap = parentChildMap;
    }

    public void execute() {
        Model rootModel = parentChildMap.get(null).iterator().next();
        writeModelToDiskRecursively(rootModel, rootFolder);
    }

    private void writeModelToDiskRecursively(Model model, File folder) {
        List<String> modules = model.getModules();
        writeModelToDisk(model, folder);

        boolean hasModules = !Objects.isNull(modules);
        if (!hasModules) {
            return;
        }

        for (String subModelArtifactId : modules) {
            File subModuleFolder = new File(rootFolder.getAbsolutePath() + File.separator + subModelArtifactId);
            if (!subModuleFolder.exists()) {
                subModuleFolder.mkdir();
            }
            List<Model> subModelList = parentChildMap.values()
                    .stream()
                    .filter(m -> Objects.equals(m.getArtifactId(), subModelArtifactId))
                    .collect(Collectors.toList());
            Preconditions.checkState(subModelList.size() == 1);
            Model subModel = subModelList.get(0);

            writeModelToDiskRecursively(subModel, subModuleFolder);
        }
    }

    private void writeModelToDisk(Model model, File folder) {
        try {
            File pom = readPomFromFolder(folder);
            MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
            mavenXpp3Writer.write(new FileWriter(pom), model);
/*
            JAXBContext jaxbContext = JAXBContext.newInstance(Model.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd");

            NamespacePrefixMapper namespacePrefixMapper = new NamespacePrefixMapper() {

                private Map<String, String> prefixes;

                {
                    prefixes = new HashMap<>(3);
                    prefixes.put(XMLConstants.XML_NS_URI, XMLConstants.XML_NS_PREFIX);
                    prefixes.put(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi");
                    prefixes.put(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xs");
                    //prefixes.put(WellKnownNamespace.XML_MIME_URI, "xmime");
                }

                @Override
                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                    String prefix = suggestion == null ? prefixes.get(namespaceUri) : suggestion;
                    return prefix == null ? XMLConstants.DEFAULT_NS_PREFIX : prefix;
                }
            };

            jaxbMarshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", namespacePrefixMapper);

            QName qName = new QName("http://maven.apache.org/POM/4.0.0", "project");
            Object jaxbElement = new JAXBElement(qName, Model.class, model);

            jaxbMarshaller.marshal(jaxbElement, pom);*/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static File readPomFromFolder(File temporaryFolder) {
        return new File(temporaryFolder.getAbsolutePath() + File.separator + "pom.xml");
    }
}
