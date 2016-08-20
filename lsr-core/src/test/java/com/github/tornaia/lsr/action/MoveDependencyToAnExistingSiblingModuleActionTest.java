package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.AbstractTest;
import com.github.tornaia.lsr.model.MavenCoordinates;
import com.google.common.collect.Multimap;
import org.apache.maven.model.Model;
import org.junit.Test;

import java.io.File;

public class MoveDependencyToAnExistingSiblingModuleActionTest extends AbstractTest {

    @Test
    public void moveWithoutSourceToExistingModule() throws Exception {
        File workingRootDirectory = createCopy("scenarios/basic/move.wo.touching.source/before");

        Multimap<Model, Model> parentChildMap = explore(workingRootDirectory);
        MavenCoordinates from = new MavenCoordinates("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinates parentTo = new MavenCoordinates("group-id", "parent-artifact-id", "1.0");
        MavenCoordinates as = new MavenCoordinates("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinates what = new MavenCoordinates("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependencyToAnotherModuleAction(parentChildMap, from, parentTo, as, what).execute();
        new WriteToDiskAction(workingRootDirectory, parentChildMap).execute();

        assertPathEqualsRecursively("scenarios/basic/move.wo.touching.source/expected", workingRootDirectory);
    }

    @Test
    public void moveWithSourceDirectDependencyToExistingModule() throws Exception {
        File workingRootDirectory = createCopy("scenarios/basic/move.with.source/before");

        Multimap<Model, Model> parentChildMap = explore(workingRootDirectory);
        MavenCoordinates from = new MavenCoordinates("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinates parentTo = new MavenCoordinates("group-id", "parent-artifact-id", "1.0");
        MavenCoordinates as = new MavenCoordinates("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinates what = new MavenCoordinates("org.apache.commons", "commons-lang3", "3.4");

        new MoveDependencyToAnotherModuleAction(parentChildMap, from, parentTo, as, what).execute();
        new WriteToDiskAction(workingRootDirectory, parentChildMap).execute();
        new MoveJavaSourcesToAnAnotherModuleAction(workingRootDirectory, from, as, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.with.source/expected", workingRootDirectory);
    }

    @Test
    public void moveWithSourceTransitiveDependencyToExistingModule() throws Exception {
        File workingRootDirectory = createCopy("scenarios/basic/move.with.source.transitive.dependency/before");

        MavenCoordinates from = new MavenCoordinates("group-id", "child-artifact-id-1", "1.0");
        MavenCoordinates parentTo = new MavenCoordinates("group-id", "parent-artifact-id", "1.0");
        MavenCoordinates as = new MavenCoordinates("group-id", "child-artifact-id-2", "1.0");
        MavenCoordinates what = new MavenCoordinates("commons-dbcp", "commons-dbcp", "1.4");

        Multimap<Model, Model> parentChildMap = explore(workingRootDirectory);

        new MoveDependencyToAnotherModuleAction(parentChildMap, from, parentTo, as, what).execute();
        new WriteToDiskAction(workingRootDirectory, parentChildMap).execute();
        new MoveJavaSourcesToAnAnotherModuleAction(workingRootDirectory, from, as, what).execute();

        assertPathEqualsRecursively("scenarios/basic/move.with.source.transitive.dependency/expected", workingRootDirectory);
    }

    @Test
    public void complexWoSources() throws Exception {
        File workingRootDirectory = createCopy("scenarios/complex/before");

        Multimap<Model, Model> parentChildMap = explore(workingRootDirectory);
        MavenCoordinates core = new MavenCoordinates("group-id", "core", "1.0");
        MavenCoordinates parent = new MavenCoordinates("group-id", "parent-artifact-id", "1.0");

        // web
        MavenCoordinates web = new MavenCoordinates("group-id", "core-web", "1.0");
        MavenCoordinates servlet = new MavenCoordinates("javax.servlet", "javax.servlet-api", "3.0.1");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, web, servlet).execute();

        // logging
        MavenCoordinates logging = new MavenCoordinates("group-id", "core-logging", "1.0");
        MavenCoordinates logbackCore = new MavenCoordinates("ch.qos.logback", "logback-core", "1.0.13");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, logging, logbackCore).execute();
        MavenCoordinates logbackClassic = new MavenCoordinates("ch.qos.logback", "logback-classic", "1.0.13");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, logging, logbackClassic).execute();
        MavenCoordinates slf4jApi = new MavenCoordinates("org.slf4j", "slf4j-api", "1.7.5");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, logging, slf4jApi).execute();
        MavenCoordinates jclOverSlf4j = new MavenCoordinates("org.slf4j", "jcl-over-slf4j", "1.7.5");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, logging, jclOverSlf4j).execute();
        MavenCoordinates log4jOverSlf4j = new MavenCoordinates("org.slf4j", "log4j-over-slf4j", "1.7.5");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, logging, log4jOverSlf4j).execute();
        MavenCoordinates jbossLogging = new MavenCoordinates("org.jboss.logging", "jboss-logging", "3.1.3.GA");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, logging, jbossLogging).execute();
        MavenCoordinates commonsLogging = new MavenCoordinates("commons-logging", "commons-logging", "1.1.3");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, logging, commonsLogging).execute();
        MavenCoordinates logbackExtSpring = new MavenCoordinates("org.logback-extensions", "logback-ext-spring", "0.1.1");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, logging, logbackExtSpring).execute();

        // persistence
        MavenCoordinates persistence = new MavenCoordinates("group-id", "core-persistence", "1.0");
        MavenCoordinates mysqlConnectorJava = new MavenCoordinates("mysql", "mysql-connector-java", "5.1.30");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, persistence, mysqlConnectorJava).execute();
        MavenCoordinates h2 = new MavenCoordinates("com.h2database", "h2", "1.4.178");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, persistence, h2).execute();
        MavenCoordinates commonsDbcp = new MavenCoordinates("commons-dbcp", "commons-dbcp", "1.4");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, persistence, commonsDbcp).execute();
        MavenCoordinates hibernateCore = new MavenCoordinates("org.hibernate", "hibernate-core", "4.3.5.Final");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, persistence, hibernateCore).execute();
        MavenCoordinates hibernateValidator = new MavenCoordinates("org.hibernate", "hibernate-validator", "5.1.1.Final");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, persistence, hibernateValidator).execute();
        MavenCoordinates hibernateEntitymanager = new MavenCoordinates("org.hibernate", "hibernate-entitymanager", "4.3.5.Final");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, persistence, hibernateEntitymanager).execute();
        MavenCoordinates hibernateValidatorAnnotationProcessor = new MavenCoordinates("org.hibernate", "hibernate-validator-annotation-processor", "5.1.1.Final");
        new MoveDependencyToAnotherModuleAction(parentChildMap, core, parent, persistence, hibernateValidatorAnnotationProcessor).execute();

        new WriteToDiskAction(workingRootDirectory, parentChildMap).execute();

        assertPathEqualsRecursively("scenarios/complex/expected", workingRootDirectory);
    }
}
