package com.github.tornaia.lsr;

import com.github.tornaia.lsr.util.ParseUtils;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public abstract class AbstractTest {

    // TODO for some reason the directories these temp directories are not deleted after tests have been finished
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected File createCopy(String classpathDirectory) {
        try {
            File original = new File(Thread.currentThread().getContextClassLoader().getResource(classpathDirectory).toURI());

            File copyRoot = temporaryFolder.getRoot();
            FileUtils.copyDirectory(original, copyRoot);

            return copyRoot;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Multimap<Model, Model> explore(File rootFolder) {
        Multimap<Model, Model> parentChildMap = LinkedHashMultimap.create();

        File pom = readPomFromFolder(rootFolder);
        Model model = ParseUtils.parsePom(pom);
        parentChildMap.put(null, model);

        List<String> subModules = model.getModules();
        for (String subModelArtifactId : subModules) {
            File subModuleFolder = new File(rootFolder.getAbsolutePath() + File.separator + subModelArtifactId);
            Multimap<Model, Model> explore = explore(subModuleFolder);
            Collection<Model> subModels = explore.values();
            parentChildMap.putAll(model, subModels);
        }

        return parentChildMap;
    }

    protected void assertPathEqualsRecursively(String expectedClasspathDirectory, File actualRootDirectory) {
        Path expected;
        try {
            expected = new File(Thread.currentThread().getContextClassLoader().getResource(expectedClasspathDirectory).toURI()).toPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        AssertFile.assertPathEqualsRecursively(actualRootDirectory.toPath(), expected);
    }

    private static File readPomFromFolder(File temporaryFolder) {
        return new File(temporaryFolder.getAbsolutePath() + File.separator + "pom.xml");
    }
}
