package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.AbstractTest;
import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.util.ParseUtils;
import com.google.common.collect.Multimap;
import org.apache.maven.model.Model;
import org.junit.Test;

import java.io.File;

public class MoveDependencyToAnExistingSiblingModuleActionTest extends AbstractTest {

    @Test
    public void moveWithoutSourceToExistingModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.wo.touching.source/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + ParseUtils.FILENAME_POM_XML);

        Multimap<Model, Model> parentChildMap = ParseUtils.explore(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependencyToAnotherModuleAction(parentChildMap, from, as, parentTo, what).execute();
        new WriteToDiskAction(rootPom, parentChildMap).execute();

        assertPathEqualsRecursively("scenarios/basic/move.wo.touching.source/expected", rootDirectory);
    }

    @Test
    public void moveWithSourceDirectDependencyToExistingModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.with.source/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + ParseUtils.FILENAME_POM_XML);

        Multimap<Model, Model> parentChildMap = ParseUtils.explore(rootPom);
        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependencyToAnotherModuleAction(parentChildMap, from, as, parentTo, what).execute();
        new WriteToDiskAction(rootPom, parentChildMap).execute();
        new MoveJavaSourcesToAnAnotherModuleAction(rootDirectory, from, as, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.with.source/expected", rootDirectory);
    }

    @Test
    public void moveWithSourceTransitiveDependencyToExistingModule() throws Exception {
        File rootDirectory = createCopy("scenarios/basic/move.with.source.transitive.dependency/before/");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + ParseUtils.FILENAME_POM_XML);

        MavenCoordinate from = new MavenCoordinate("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinate parentTo = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");
        MavenCoordinate as = new MavenCoordinate("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinate what = new MavenCoordinate("commons-dbcp", "commons-dbcp", "1.4");

        Multimap<Model, Model> parentChildMap = ParseUtils.explore(rootPom);

        new MoveDependencyToAnotherModuleAction(parentChildMap, from, as, parentTo, what).execute();
        new WriteToDiskAction(rootPom, parentChildMap).execute();
        new MoveJavaSourcesToAnAnotherModuleAction(rootDirectory, from, as, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.with.source.transitive.dependency/expected", rootDirectory);
    }

    @Test
    public void complexWoSources() throws Exception {
        File rootDirectory = createCopy("scenarios/complex/before");
        File rootPom = new File(rootDirectory.getAbsolutePath() + File.separator + ParseUtils.FILENAME_POM_XML);

        Multimap<Model, Model> parentChildMap = ParseUtils.explore(rootPom);
        MavenCoordinate core = new MavenCoordinate("group-id", "core", "1.0");
        MavenCoordinate parent = new MavenCoordinate("group-id", "parent-artifact-id", "1.0");

        // web
        MavenCoordinate web = new MavenCoordinate("group-id", "core-web", "1.0");
        MavenCoordinate servlet = new MavenCoordinate("javax.servlet", "javax.servlet-api", "3.0.1");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, web, parent, servlet).execute();

        // logging
        MavenCoordinate logging = new MavenCoordinate("group-id", "core-logging", "1.0");
        MavenCoordinate logbackCore = new MavenCoordinate("ch.qos.logback", "logback-core", "1.0.13");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, logging, parent, logbackCore).execute();
        MavenCoordinate logbackClassic = new MavenCoordinate("ch.qos.logback", "logback-classic", "1.0.13");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, logging, parent, logbackClassic).execute();
        MavenCoordinate slf4jApi = new MavenCoordinate("org.slf4j", "slf4j-api", "1.7.5");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, logging, parent, slf4jApi).execute();
        MavenCoordinate jclOverSlf4j = new MavenCoordinate("org.slf4j", "jcl-over-slf4j", "1.7.5");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, logging, parent, jclOverSlf4j).execute();
        MavenCoordinate log4jOverSlf4j = new MavenCoordinate("org.slf4j", "log4j-over-slf4j", "1.7.5");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, logging, parent, log4jOverSlf4j).execute();
        MavenCoordinate jbossLogging = new MavenCoordinate("org.jboss.logging", "jboss-logging", "3.1.3.GA");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, logging, parent, jbossLogging).execute();
        MavenCoordinate commonsLogging = new MavenCoordinate("commons-logging", "commons-logging", "1.1.3");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, logging, parent, commonsLogging).execute();
        MavenCoordinate logbackExtSpring = new MavenCoordinate("org.logback-extensions", "logback-ext-spring", "0.1.1");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, logging, parent, logbackExtSpring).execute();

        // persistence
        MavenCoordinate persistence = new MavenCoordinate("group-id", "core-persistence", "1.0");
        MavenCoordinate mysqlConnectorJava = new MavenCoordinate("mysql", "mysql-connector-java", "5.1.30");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, persistence, parent, mysqlConnectorJava).execute();
        MavenCoordinate h2 = new MavenCoordinate("com.h2database", "h2", "1.4.178");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, persistence, parent, h2).execute();
        MavenCoordinate commonsDbcp = new MavenCoordinate("commons-dbcp", "commons-dbcp", "1.4");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, persistence, parent, commonsDbcp).execute();
        MavenCoordinate hibernateCore = new MavenCoordinate("org.hibernate", "hibernate-core", "4.3.5.Final");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, persistence, parent, hibernateCore).execute();
        MavenCoordinate hibernateValidator = new MavenCoordinate("org.hibernate", "hibernate-validator", "5.1.1.Final");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, persistence, parent, hibernateValidator).execute();
        MavenCoordinate hibernateEntitymanager = new MavenCoordinate("org.hibernate", "hibernate-entitymanager", "4.3.5.Final");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, persistence, parent, hibernateEntitymanager).execute();
        MavenCoordinate hibernateValidatorAnnotationProcessor = new MavenCoordinate("org.hibernate", "hibernate-validator-annotation-processor", "5.1.1.Final");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, persistence, parent, hibernateValidatorAnnotationProcessor).execute();

        new WriteToDiskAction(rootPom, parentChildMap).execute();

        assertPathEqualsRecursively("scenarios/complex/expected", rootDirectory);
    }
}
