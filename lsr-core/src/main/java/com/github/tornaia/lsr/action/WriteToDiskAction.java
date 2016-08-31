package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.model.MavenProject;
import com.github.tornaia.lsr.util.ParseUtils;
import com.google.common.base.Preconditions;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class WriteToDiskAction implements Action {

    private MavenProject mavenProject;

    WriteToDiskAction(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    public void execute() {
        Model rootModel = mavenProject.getRootModel();
        File rootPom = mavenProject.getRootPom();
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

            List<Model> subModelList = mavenProject.getAllModels()
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
