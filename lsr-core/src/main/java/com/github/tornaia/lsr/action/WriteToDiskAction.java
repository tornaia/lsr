package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.model.MavenModel;
import com.github.tornaia.lsr.model.MavenProject;
import com.google.common.base.Preconditions;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class WriteToDiskAction implements Action {

    private MavenProject mavenProject;

    WriteToDiskAction(MavenProject mavenProject) {
        this.mavenProject = mavenProject;
    }

    public void execute() {
        MavenModel rootModel = mavenProject.getRootModel();
        File rootPom = mavenProject.getRootPom();
        writeModelToDiskRecursively(rootModel, rootPom);
    }

    private void writeModelToDiskRecursively(MavenModel model, File pom) {
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

            List<MavenModel> subModelList = mavenProject.getAllModels()
                    .stream()
                    .filter(m -> Objects.equals(m.getArtifactId(), subModelArtifactId))
                    .collect(Collectors.toList());
            Preconditions.checkState(subModelList.size() == 1);
            MavenModel subModel = subModelList.get(0);
            File subModelPom = new File(subModuleFolder.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

            writeModelToDiskRecursively(subModel, subModelPom);
        }
    }

    private static void writeModelToDisk(MavenModel model, File pom) {
        try (Writer writer = new FileWriter(pom)) {
            MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
            mavenXpp3Writer.write(writer, model.getModel());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
