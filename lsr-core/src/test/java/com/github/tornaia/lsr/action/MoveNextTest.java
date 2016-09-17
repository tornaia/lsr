package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.AbstractTest;
import com.github.tornaia.lsr.exception.IllegalMavenStateException;
import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.model.MavenModel;
import com.github.tornaia.lsr.model.MavenProject;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MoveNextTest extends AbstractTest {

    @Test
    public void moveWithoutSourceToExistingModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.wo.touching.source/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

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
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.with.source/expected", rootDirectory);
    }

    @Test
    public void moveWithSourceDirectDependencyToNewModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.with.source.to.new/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.with.source.to.new/expected", rootDirectory);
    }

    @Test
    public void moveWithSourceTransitiveDependencyToExistingModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.with.source.transitive.dependency/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("commons-dbcp", "commons-dbcp", "1.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.with.source.transitive.dependency/expected", rootDirectory);
    }

    @Test
    public void moveNextOneTransitiveOneDirectWhenTransitiveIsMoving() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.next.one.transitive.one.direct.when.transitive.is.moving/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("io.appium", "java-client", "4.1.2");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.next.one.transitive.one.direct.when.transitive.is.moving/expected", rootDirectory);
    }

    @Test
    public void moveNextOneTransitiveOneDirectWhenDirectIsMoving() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.next.one.transitive.one.direct.when.direct.is.moving/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.next.one.transitive.one.direct.when.direct.is.moving/expected", rootDirectory);
    }

    @Test
    public void moveNextWhenDependencyAlreadyThere() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.next.when.dependency.already.there/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.next.when.dependency.already.there/expected", rootDirectory);
    }

    @Test
    public void moveNextWhenDependencyAlreadyWithDifferentVersionThere() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.next.when.dependency.already.there.with.different.version/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        try {
            new MoveDependency(mavenProject, from, as, parentTo, what).execute();
            fail();
        } catch (IllegalMavenStateException e) {
            assertTrue(e.getMessage().contains("Conflict! group-id:child-artifact-id-1:1.0 has dependency org.apache.commons:commons-lang3:3.4 but group-id:child-artifact-id-2:1.0 already has dependency org.apache.commons:commons-lang3:3.3.2"));
        }

        assertPathEqualsRecursively("scenarios/basic/move.next.when.dependency.already.there.with.different.version/before", rootDirectory);
    }

    @Test
    public void moveNextTwoTransitive() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.next.two.transitive/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("io.appium", "java-client", "4.1.2");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.next.two.transitive/expected", rootDirectory);
    }

    @Test
    public void moveNextDirectInheritedTransitiveOwnWhenTransitiveIsMoving() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.next.direct.inherited.transitive.own.when.transitive.is.moving/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("io.appium", "java-client", "4.1.2");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.next.direct.inherited.transitive.own.when.transitive.is.moving/expected", rootDirectory);
    }

    @Test
    public void moveNextTransitiveInheritedDirectOwnWhenDirectIsMoving() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.next.transitive.inherited.direct.own.when.direct.is.moving/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + MavenModel.FILENAME_POM_XML);

        MavenProject mavenProject = new MavenProject(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependency(mavenProject, from, as, parentTo, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.next.transitive.inherited.direct.own.when.direct.is.moving/expected", rootDirectory);
    }
}
