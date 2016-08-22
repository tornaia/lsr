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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static File readPomFromFolder(File temporaryFolder) {
        return new File(temporaryFolder.getAbsolutePath() + File.separator + "pom.xml");
    }
}
