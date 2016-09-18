package com.github.tornaia.lsr;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;

public class MavenResolverTest {

    @Test
    public void classLoaderIsSet() {
        Maven.resolver()
                .resolve("org.springframework:spring-web:4.3.2.RELEASE")
                .withTransitivity()
                .asResolvedArtifact();
    }
}
