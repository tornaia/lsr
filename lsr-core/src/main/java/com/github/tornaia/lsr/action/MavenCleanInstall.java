package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.model.MavenProject;
import org.apache.maven.shared.invoker.*;

import java.util.Arrays;
import java.util.Properties;

public class MavenCleanInstall {

    public MavenCleanInstall(MavenProject mavenProject) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(mavenProject.getRootPom());
        request.setGoals(Arrays.asList("clean", "install"));

        Properties mavenProperty = new Properties();
        mavenProperty.setProperty("skipTests", "");
        mavenProperty.setProperty("maven.test.skip", "true");
        request.setProperties(mavenProperty);

        Invoker invoker = new DefaultInvoker();

        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }
}