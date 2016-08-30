package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.model.MavenProject;

public class MoveDependency implements Action {

    private MavenProject mavenProject;
    private MavenCoordinate from;
    private MavenCoordinate as;
    private MavenCoordinate parentTo;
    private MavenCoordinate what;

    public MoveDependency(MavenProject mavenProject, MavenCoordinate from, MavenCoordinate as, MavenCoordinate parentTo, MavenCoordinate what) {
        this.mavenProject = mavenProject;
        this.from = from;
        this.as = as;
        this.parentTo = parentTo;
        this.what = what;
    }

    @Override
    public void execute() {
        new MoveDependencyToAnotherModuleAction(mavenProject, from, as, parentTo, what).execute();
        new WriteToDiskAction(mavenProject).execute();
        new MoveJavaSourcesToAnAnotherModuleAction(mavenProject, from, as, parentTo, what).execute();
    }
}
