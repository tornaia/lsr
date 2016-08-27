package com.github.tornaia.lsr.action;

import com.github.tornaia.lsr.model.MavenCoordinate;
import com.google.common.collect.Multimap;
import org.apache.maven.model.Model;

import java.io.File;

public class MoveDependency implements Action {

    private Multimap<Model, Model> parentChildMap;
    private File rootPom;
    private MavenCoordinate from;
    private MavenCoordinate as;
    private MavenCoordinate parentTo;
    private MavenCoordinate what;

    public MoveDependency(Multimap<Model, Model> parentChildMap, File rootPom, MavenCoordinate from, MavenCoordinate as, MavenCoordinate parentTo, MavenCoordinate what) {
        this.parentChildMap = parentChildMap;
        this.rootPom = rootPom;
        this.from = from;
        this.as = as;
        this.parentTo = parentTo;
        this.what = what;
    }

    @Override
    public void execute() {
        new MoveDependencyToAnotherModuleAction(parentChildMap, from, as, parentTo, what).execute();
        new WriteToDiskAction(rootPom, parentChildMap).execute();
        new MoveJavaSourcesToAnAnotherModuleAction(rootPom, parentChildMap, from, as, parentTo, what).execute();
    }
}
