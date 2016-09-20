package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.AbstractTest;
import com.github.tornaia.lsr.exception.IllegalMavenStateException;
import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.model.MavenModel;
import com.github.tornaia.lsr.model.MavenProject;
import org.junit.Test;

import java.io.File;

public class PropertyResolutionTest extends AbstractTest {

    @Test
    public void propertyResolution() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/property.resolution/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/property.resolution/expected", rootDirectory);
    }

    @Test
    public void propertyResolutionInheritedFromParent() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/property.resolution.inherited.from.parent/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/property.resolution.inherited.from.parent/expected", rootDirectory);
    }

    @Test
    public void propertyResolutionCloserIsTheWinner() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/property.resolution.inherited.closer.wins/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/property.resolution.inherited.closer.wins/expected", rootDirectory);
    }
}
