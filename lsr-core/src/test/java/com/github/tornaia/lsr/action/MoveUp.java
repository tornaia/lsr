package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.AbstractTest;
import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.model.MavenProject;
import com.github.tornaia.lsr.util.ParseUtils;
import org.junit.Test;

import java.io.File;

public class MoveUp extends AbstractTest {

    @Test
    public void srcNotCreatedInParentModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.up/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + ParseUtils.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id", "1.0");
        MavenCoordinate parentTo = null;
        MavenCoordinate as = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.up/expected", rootDirectory);
    }
}
