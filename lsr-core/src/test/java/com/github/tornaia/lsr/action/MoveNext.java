package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.AbstractTest;
import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.model.MavenProject;
import com.github.tornaia.lsr.util.ParseUtils;
import org.junit.Test;

import java.io.File;

public class MoveNext extends AbstractTest {

    @Test
    public void moveWithoutSourceToExistingModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.wo.touching.source/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + ParseUtils.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.wo.touching.source/expected", rootDirectory);
    }

    @Test
    public void moveWithSourceDirectDependencyToExistingModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.with.source/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + ParseUtils.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.with.source/expected", rootDirectory);
    }

    @Test
    public void moveWithSourceTransitiveDependencyToExistingModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.with.source.transitive.dependency/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + ParseUtils.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("commons-dbcp", "commons-dbcp", "1.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.with.source.transitive.dependency/expected", rootDirectory);
    }
}
