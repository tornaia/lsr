package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.util.ParseUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class WriteToDiskAction implements Action {

    private File rootPom;
    private Multimap<Model, Model> parentChildMap;

    WriteToDiskAction(File rootPom, Multimap<Model, Model> parentChildMap) {
        this.rootPom = rootPom;
        this.parentChildMap = parentChildMap;
    }

    public void execute() {
        Model rootModel = parentChildMap.get(null).iterator().next();
        writeModelToDiskRecursively(rootModel, rootPom);
    }

    private void writeModelToDiskRecursively(Model model, File pom) {
        List<String> modules = model.getModules();
        writeModelToDisk(model, pom);

        boolean hasModules = !Objects.isNull(modules);
        if (!hasModules) {
            return;
        }

        for (String subModelArtifactId : modules) {
            File subModuleFolder = new File(pom.getParentFile().getAbsolutePath() + File.separator + subModelArtifactId);
            if (!subModuleFolder.exists()) {
                subModuleFolder.mkdir();
            }
            List<Model> subModelList = parentChildMap.values()
                    .stream()
                    .filter(m -> Objects.equals(m.getArtifactId(), subModelArtifactId))
                    .collect(Collectors.toList());
            Preconditions.checkState(subModelList.size() == 1);
            Model subModel = subModelList.get(0);
            File subModelPom = new File(subModuleFolder.getAbsolutePath() + File.separator + ParseUtils.FILENAME_POM_XML);

            writeModelToDiskRecursively(subModel, subModelPom);
        }
    }

    private void writeModelToDisk(Model model, File pom) {
        try {
            MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
            mavenXpp3Writer.write(new FileWriter(pom), model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
