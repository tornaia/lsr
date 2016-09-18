package com.github.tornaia.lsr.plugin.idea;

import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.model.MavenModel;
import com.github.tornaia.lsr.model.MavenProject;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.maven.model.Dependency;

import java.io.File;
import java.util.Optional;
import java.util.Set;

public class RefactoringMenuAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        Editor editor = fileEditorManager.getSelectedTextEditor();
        PomEditor pomEditor = new PomEditor(editor);

        Optional<Dependency> optionalWhat = pomEditor.getSelectedDependency();

        VirtualFile virtualFile = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        String pomPath = virtualFile.getCanonicalPath();
        MavenModel fromModel = new MavenModel(new File(pomPath));
        MavenCoordinate from = new MavenCoordinate(fromModel.getGroupId(), fromModel.getArtifactId(), fromModel.getVersion());

        MavenProject mavenProject = new MavenProject(new File(pomPath));
        Set<MavenCoordinate> targets = mavenProject.getAllMavenCoordinates();

        optionalWhat.ifPresent(what -> showDialog(what, from, mavenProject.getRootPom(), targets));
    }

    private void showDialog(Dependency what, MavenCoordinate from, File topLevelPom, Set<MavenCoordinate> targets) {
        RefactoringDialog dialog = new RefactoringDialog();
        dialog.setWhat(what);
        dialog.setFrom(from);
        dialog.setRootPom(topLevelPom);
        dialog.setTargets(targets);
        dialog.pack();
        dialog.setVisible(true);
    }
}