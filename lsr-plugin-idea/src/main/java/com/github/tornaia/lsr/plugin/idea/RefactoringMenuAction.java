package com.github.tornaia.lsr.plugin.idea;

import com.github.tornaia.lsr.model.MavenCoordinate;
import com.github.tornaia.lsr.util.ParseUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import java.io.File;
import java.util.Optional;
import java.util.Set;

public class RefactoringMenuAction extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Document currentDoc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
        String fileContent = currentDoc.getText();

        Caret currentCaret = FileEditorManager.getInstance(project).getSelectedTextEditor().getCaretModel().getCurrentCaret();
        VisualPosition start = currentCaret.getSelectionStartPosition();
        VisualPosition end = currentCaret.getSelectionEndPosition();

        Optional<Dependency> optionalWhat = ParseUtils.getSelectedDependency(fileContent, start.getLine(), end.getLine());

        VirtualFile virtualFile = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        String pomPath = virtualFile.getCanonicalPath();
        Model fromModel = ParseUtils.parsePom(new File(pomPath));
        MavenCoordinate from = new MavenCoordinate(fromModel.getGroupId(), fromModel.getArtifactId(), fromModel.getVersion());
        File topLevelPom = ParseUtils.getTopLevelPom(pomPath);
        Set<Model> targets = Sets.newHashSet(ParseUtils.explore(topLevelPom).values());

        optionalWhat.ifPresent(what -> showDialog(what, from, topLevelPom, targets));
    }

    private void showDialog(Dependency what, MavenCoordinate from, File topLevelPom, Set<Model> targets) {
        RefactoringDialog dialog = new RefactoringDialog();
        dialog.setWhat(what);
        dialog.setFrom(from);
        dialog.setTopLevelPom(topLevelPom);
        dialog.setTargets(Lists.newArrayList(targets));
        dialog.pack();
        dialog.setVisible(true);
    }
}